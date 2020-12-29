package cn.sunline.ltts.busi.aplt.tools;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppChannel;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppChannelDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSystem;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AppSystemDao;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;

/**
 * @author zhoujiawen:
 * @version 创建时间：2020年8月20日 下午3:27:20 应用平台参数表工具类
 */
public class ParamUtil {

	// 参数通配符
	private static final String WILDCARD = ApConstants.WILDCARD;

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:32:05 功能说明：根据系统编号获取系统信息
	 * @param systemId
	 *            系统编号
	 * @param isThrow
	 *            是否抛错，true则没找到相关信息则抛错
	 * @return 系统信息
	 */
	public static AppSystem getSystemInfo(String systemId, boolean isThrow) {
		AppSystem sysInfo = AppSystemDao.selectOne_odb1(systemId, false);
		if (sysInfo == null && isThrow) {
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppSystem.class).getLongname(),
					ApDict.Aplt.xitongbh.getLongName(), systemId);
		}
		return sysInfo;
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:36:27 功能说明：检查系统编号是否存在，不存在则抛错
	 * @param systemId
	 *            系统编号
	 */
	public static void checkSystemValid(String systemId) {
		AppSystem sysInfo = AppSystemDao.selectOne_odb1(systemId, false);
		if (sysInfo == null)
			throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppSystem.class).getLongname(),
					ApDict.Aplt.xitongbh.getLongName(), systemId);
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:41:37 功能说明：判断渠道编号是不是柜面渠道
	 * @param channelId
	 *            渠道编号
	 * @return 是默认渠道返回true，不是则返回false
	 */
	public static boolean channelIsCounter(String channelId) {

		AppChannel channel = getChannel(channelId);

		return (channel.getIsdefu() == E_YES___.YES ? true : false);
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:58:44 功能说明：判断渠道是否需要登记冲正事件
	 * @param channelId
	 *            渠道编号
	 * @return 需要登记则返回true，不需要则返回false
	 */
	public static boolean isRegisterReversalEvent(String channelId) {

		AppChannel channel = getChannel(channelId);

		return (channel.getRereei() == E_YES___.YES ? true : false);
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:55:08 功能说明：判断渠道是否存在
	 * @param channelId
	 *            渠道编号
	 * @return 存在则返回true，不存在返回false
	 */
	public static boolean channelExists(String channelId) {

		AppChannel channel = AppChannelDao.selectOne_odb1(channelId, false);

		return (channel == null) ? false : true;
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:53:19 功能说明：判断渠道是否启用
	 * @param channelId
	 *            渠道编号
	 * @return 启用则返回true，未启用则返回false
	 */
	public static boolean channelIsValid(String channelId) {

		AppChannel channel = AppChannelDao.selectOne_odb1(channelId, false);

		boolean ret = false;

		if (channel != null && channel.getChruin() == E_YES___.YES) {
			ret = true;
		}

		return ret;
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:50:18 功能说明：根据渠道编号获取渠道信息，不存在则抛错
	 * @param channelId
	 *            渠道编号
	 * @return
	 */
	public static AppChannel getChannel(String channelId) {
		return getChannel(channelId, true);
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月20日 下午3:47:36 功能说明：根据渠道编号获取渠道信息
	 * @param channelId
	 *            渠道编号
	 * @param isThrow
	 *            是否抛错，true，不存在则抛错；false，不抛错。
	 * @return 渠道信息
	 */
	public static AppChannel getChannel(String channelId, boolean isThrow) {
		// 根据渠道ID获取渠道数据
		AppChannel channel = AppChannelDao.selectOne_odb1(channelId, false);
		if (channel == null) {
			if (isThrow) {
				throw ApPubErr.APPUB.E0005(OdbFactory.getTable(AppChannel.class).getLongname(),
						ApDict.Aplt.systid.getLongName(), channelId);
			} else {
				return null;
			}
		}
		return channel;
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月21日 下午2:02:42
	 *         功能说明：根据法人代码，参数编号，参数主键1，参数主键2，参数主键3获取公共参数，获取不到则抛错
	 * @param corpno
	 *            法人代码
	 * @param parmcd
	 *            参数编号
	 * @param pmkey1
	 *            参数主键1
	 * @param pmkey2
	 *            参数主键2
	 * @param pmkey3
	 *            参数主键3
	 * @return 返回获取到的公共参数对象
	 */
	public static KnpPara getPublicParm(String corpno, String parmcd, String pmkey1, String pmkey2, String pmkey3) {
		
		KnpPara res = KnpParaDao.selectOne_odb1(parmcd, pmkey1, pmkey2, pmkey3, corpno,false);

		return res;
	}

	public static KnpPara getPublicParm(String corpno, String parmcd, String pmkey1, String pmkey2) {
		return getPublicParm(corpno, parmcd, pmkey1, pmkey2, WILDCARD);
	}

	public static KnpPara getPublicParm(String corpno, String parmcd, String pmkey1) {
		return getPublicParm(corpno, parmcd, pmkey1, WILDCARD, WILDCARD);
	}

	public static KnpPara getPublicParm(String corpno, String parmcd) {
		return getPublicParm(corpno, parmcd, WILDCARD, WILDCARD, WILDCARD);
	}

	/**
	 * 
	 * @author zhoujiawen 2020年8月24日 上午9:36:33
	 *         功能说明：根据当前交易法人和传入的参数编号、参数主键1、参数主键2、参数主键3获取公共参数
	 * @param parmcd
	 *            参数编号
	 * @param pmkey1
	 *            参数主键1
	 * @param pmkey2
	 *            参数主键2
	 * @param pmkey3
	 *            参数主键3
	 * @return 返回获取到的公共参数对象
	 */
	public static KnpPara getPublicParmWithoutCorpno(String parmcd, String pmkey1, String pmkey2) {
		String corpno = CommTools.getTranCorpno();
		return getPublicParm(corpno, parmcd, pmkey1, pmkey2, WILDCARD);
	}

	public static KnpPara getPublicParmWithoutCorpno(String parmcd, String pmkey1) {
		String corpno = CommTools.getTranCorpno();
		return getPublicParm(corpno, parmcd, pmkey1, WILDCARD, WILDCARD);
	}

	public static KnpPara getPublicParmWithoutCorpno(String parmcd) {
		String corpno = CommTools.getTranCorpno();
		return getPublicParm(corpno, parmcd, WILDCARD, WILDCARD, WILDCARD);
	}

	/**
	 * 
	 * @author zhoujiawen
	 * 2020年8月24日 上午9:59:33
	 * 功能说明：判断公共参数是否存在
	 * @param corpno 法人编号
	 * @param parmcd 参数编号
	 * @param pmkey1 参数主键1 
	 * @param pmkey2 参数主键2
	 * @param pmkey3 参数主键3
	 * @return 存在则返回true，不存在则返回false
	 */
	public static boolean paraExists(String corpno, String parmcd, String pmkey1, String pmkey2, String pmkey3) {
		KnpPara knpPara = KnpParaDao.selectOne_odb1(parmcd, pmkey1, pmkey2, pmkey3, corpno, false);
		return CommUtil.isNull(knpPara) ? false : true;
	}
	
	public static boolean paraExists(String corpno, String parmcd, String pmkey1, String pmkey2) {
		KnpPara knpPara = KnpParaDao.selectOne_odb1(parmcd, pmkey1, pmkey2, WILDCARD, corpno, false);
		return CommUtil.isNull(knpPara) ? false : true;
	}
	
	public static boolean paraExists(String corpno, String parmcd, String pmkey1) {
		KnpPara knpPara = KnpParaDao.selectOne_odb1(parmcd, pmkey1, WILDCARD, WILDCARD, corpno, false);
		return CommUtil.isNull(knpPara) ? false : true;
	}
	
	public static boolean paraExists(String corpno, String parmcd) {
		KnpPara knpPara = KnpParaDao.selectOne_odb1(parmcd, WILDCARD, WILDCARD, WILDCARD, corpno, false);
		return CommUtil.isNull(knpPara) ? false : true;
	}
}
