package cn.sunline.ltts.gns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.edsp.busi.icore.ap.acdt.namedsql.ApAcdtNSQLDao;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.busi.sys.errors.ApError.Acdt;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;

/**
 * 自动日切，日切规则列表持有对象
 * <li>持有一个日切规则列表，最大长度为3，分别为T-1日、T日和T+1日
 * <li>通过给定时间，获得对应的交易账务日期
 */
public class AcdtHolder {

	// 保留3个日切规则对象
	private final static int _MAX_INDEX_ = 3;
	// 最大日期间隔：获得的日期与生效时间差，不能大于此时间（单位：分钟）
	private static int _MAX_INTERVAL_IN_MINUTE = 24 * 60;
	// 日切规则对象数组：循环使用，即：环状存放
	private static AppAcdt[] acdts = new AppAcdt[_MAX_INDEX_];
	// 当前生效日期位置
	private static Integer index = -1;
	private static final SysLog log = SysLogUtil.getSysLog(AcdtHolder.class);

	/**
	 * 应用启动时，将日切规则加载到此对象
	 * <li>限制：若列表多于三条，只取日期最大的三条
	 */
	public static void init(int interval, List<AppAcdt> acdtList) {
		if (CommUtil.isNull(acdtList)) {
			Sys.E0001("初始化日期列表不能为空！");
		}
		if (interval > 0) {
			_MAX_INTERVAL_IN_MINUTE = interval;
		}
		
		flushDate(acdtList);

		index=0;
		log.debug("index={},0={},1={},2={}", index, acdts[0], acdts[1], acdts[2]);
	}

	/**
	 * 连续跑批测试专用日期刷新服务
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年12月19日-下午5:11:19</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 */
	public static void reflushDate() {
		acdts = new AppAcdt[_MAX_INDEX_];
		index= -1;
		flushDate(getAppAcdt());
	}
	
	/**
	 * @Author os_cl_zhaodongliang
	 *         <p>
	 *         <li>2020年12月19日-下午4:55:36</li>
	 *         <li>功能说明：使用list的循环</li>
	 *         </p>
	 * @param acdtList
	 */
	private static void flushDate(List<AppAcdt> acdtList) {
		
		acdtList.stream()
		.sorted((x,y)->y.getValidt().compareTo(x.getValidt()))
		.limit(_MAX_INDEX_).forEach(it->{
			acdts[++index]=it;
		});
		
	}
	
	public static List<AppAcdt> getAppAcdt() {
		List<AppAcdt> list = DaoUtil.selectAll(AppAcdt.class);
		return list;
	}
	
	/**
	 * 增加一个日切规则。
	 * <li>添加后，将当前位置+1
	 * <li>限制：不允许并行执行，一般来说，仅日切规则加载线程调用
	 * 
	 * @param acdt
	 */
	public static synchronized void append(AppAcdt acdt) {
		int newIndex = reIndex(index - 1);
		acdts[newIndex] = copy(acdt);
		index = newIndex;
	}

	// 获取指定生效时间的日期
	public static AppAcdt getAcdt(String currentTime) {
		AppAcdt acdt = getAcdt(currentTime, index, 1);
		if (acdt != null) {
			checkDate(currentTime, acdt.getValidt());
		}
		return acdt;
	}

	private static AppAcdt getAcdt(String currentTime, int currentIndex, int times) {
		log.debug("getAcdt: currentTime={},currentIndex={},times={}", currentTime, currentIndex, times);
		if (times > _MAX_INDEX_) {
			return null; // 找了一圈，未找到匹配的日期
		}
		if (checkAcdt(currentTime, acdts[currentIndex])) {
			return acdts[currentIndex];
		}
		times++;
		int lastIndex = reIndex(currentIndex + 1);
		return getAcdt(currentTime, lastIndex, times);
	}

	private static boolean checkAcdt(String currentTime, AppAcdt acdt) {
		if (CommUtil.isNull(acdt) || CommUtil.isNull(acdt.getValidt()))
			return false;
		return CommUtil.compare(currentTime, acdt.getValidt()) >= 0 ? true : false;
	}

	// 指向上一或下一位置
	private static int reIndex(int newIndex) {
		return (_MAX_INDEX_ + newIndex) % _MAX_INDEX_; // 防止结果为负
	}

	// 复制对象，防止缓存引用
	private static AppAcdt copy(AppAcdt acdt) {
		AppAcdt newAcdt = SysUtil.getInstance(AppAcdt.class);
		newAcdt.setValidt(acdt.getValidt());
		newAcdt.setLastdt(acdt.getLastdt());
		newAcdt.setSystdt(acdt.getSystdt());
		newAcdt.setNextdt(acdt.getNextdt());
		newAcdt.setUpdttm(acdt.getUpdttm());
		newAcdt.setBflsdt(acdt.getBflsdt());
		newAcdt.setAfnxdt(acdt.getAfnxdt());
		newAcdt.setYreddt(acdt.getYreddt());
		return newAcdt;
	}

	//private static final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final long _MINUTE_DIV_ = 1000 * 60;

	// 检查获得的日期其生效时间与指定的时间的间隔不能超过指定参数，防止重复日切和忘记日切情况。
	private static void checkDate(String currentTime, String validt) {
		if (CommUtil.isNull(currentTime) || CommUtil.isNull(validt)) {
			return;
		}
		Date d1 = null, d2 = null;
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			d1 = formatter.parse(currentTime);
			d2 = formatter.parse(validt);
		} catch (ParseException e) {
			Sys.E0001("时间格式转换错误！");
		}
		long minute = Math.abs((d1.getTime() - d2.getTime()) / _MINUTE_DIV_);
		log.debug("checkDate={},INTERVAL={}", minute, _MAX_INTERVAL_IN_MINUTE);
		if (minute >= _MAX_INTERVAL_IN_MINUTE) {
			Acdt.E0004(validt, currentTime, minute);
		}

	}
	
	/**
	 * 
	 * 功能说明：根据数据更新日切规则对象数组
	 * @author zhoujiawen
	 * 2020年10月20日 下午2:32:07
	 * @param acdts
	 */
	public static synchronized void updateAcdt(List<AppAcdt> acdtlist) {
		AppAcdt sys = acdts[index];
		List<AppAcdt> res =acdtlist.stream()
				.filter(x->x.getValidt().compareTo(sys.getValidt())>0)
				.collect(Collectors.toList());
		if(res.size()==1) {
			append(res.get(0));
		}else if(res.size()>1) {
			throw Sys.E0001("存在超过1条大于当前的日期的日切规则！");
		}
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年12月20日-下午5:19:23</li>
	 *         <li>功能说明：获得最新日期</li>
	 *         </p>
	 * @return
	 */
	public static AppAcdt getNewAcdt() {
		return ApAcdtNSQLDao.selNewApAcdt(true);
	}

	
}
