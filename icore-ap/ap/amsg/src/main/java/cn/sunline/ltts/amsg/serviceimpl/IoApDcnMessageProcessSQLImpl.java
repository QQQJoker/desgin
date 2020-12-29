package cn.sunline.ltts.amsg.serviceimpl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessData;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsMessDataDao;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiMess;
import cn.sunline.ltts.busi.amsg.tables.ApAmsg.ApsTopiMessDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SQLCType;
import cn.sunline.adp.metadata.base.dao.CommonDao;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.adp.vine.base.util.lang.StringUtils;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageTotalInfo;

/**
 * SQL方式同步数据实现
 * 
 */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value = "IoApDcnMessageProcessSQLImpl", longname = "SQL方式同步数据实现", type = cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApDcnMessageProcessSQLImpl implements cn.sunline.ltts.amsg.servicetype.IoApDcnMessageProcess {
	
	CommonDao dao = EdspCoreBeanUtil.getCommonDaoFactory().getInstance();
	
	/**
	 * 消息处理服务
	 * 
	 */
	public void process(final MessageTotalInfo mtinfo) {
		String affaid = mtinfo.getRealInfo().getAffaid();//事务ID
		//int affcnt = mtinfo.getAffcnt();//事务内消息总数
		
		String trandt = CommTools.prcRunEnvs().getTrandt();
		
		//已经接收的条数
		//int reciveCnt = ApMsgNsqlDao.selTopiMessCountByAffaid(affaid, true);
		//判断是否接收完成
		//if (affcnt == reciveCnt) {
		List<ApsTopiMess> list = ApsTopiMessDao.selectAll_odb1(affaid, true);
		Collections.sort(list, new Comparator<ApsTopiMess>() {
			@Override
			public int compare(ApsTopiMess o1, ApsTopiMess o2) {
				if (o1.getPumesq() > o2.getPumesq()) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		int cursq = 1;
		for (ApsTopiMess apsTopiMess : list) {
			if (apsTopiMess.getPumesq() != cursq) {
				throw Aplt.E0000("事务ID: " + affaid + " 的消息序号出现中断或者重复没有连上");
			}
			String messid = apsTopiMess.getMessid();

			ApsMessData apsMessData = ApsMessDataDao.selectOne_odb1(messid,
					trandt, true);
			String medata = apsMessData.getMedata();
			SQLCType sqlcType = SysUtil.deserialize(medata, SQLCType.class);
			executeSQL(sqlcType);
			cursq++;
		}
		//}
		
	}
	
	
	/**
	 * 执行SQL语句
	 */
	@SuppressWarnings("unchecked")
	private void executeSQL(SQLCType sqlcType){
		String sqlfid = sqlcType.getSqlfid();//SQLID
		Map<String, Object> sqlpam = (Map<String, Object>) sqlcType.getSqlpam();//SQL 参数
		String sqlstr = sqlcType.getSqlstr();//SQL
		
		if (!StringUtils.isEmpty(sqlfid)) {
			DaoUtil.executeProc(sqlfid, sqlpam);
		} else if (!StringUtils.isEmpty(sqlstr)) {
			dao.executeSql(sqlstr, sqlpam);
		}
		
	}


	@Override
	public void processMri(MessageRealInfo mrinfo) {
		// TODO Auto-generated method stub
		
	}
}
