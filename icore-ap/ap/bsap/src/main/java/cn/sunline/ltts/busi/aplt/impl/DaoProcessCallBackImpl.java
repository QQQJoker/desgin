package cn.sunline.ltts.busi.aplt.impl;

import java.util.List;
import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.tables.KSysCommFieldTable;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.base.util.CoreUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.metadata.base.dao.Operator;
import cn.sunline.adp.metadata.base.dao.callback.EntityDaoProcessCallBackSupport;
import cn.sunline.adp.metadata.base.dao.clog.Clog;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.tables.SysCommFieldTable;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpPmct;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpPmctDao;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApError.Sys;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.RunEnvsComm;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_PMCTRL;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_VALID_;

/**
 * dao参数处理回调类
 * 
 */
@SPIMeta
@Order(1001)
public class DaoProcessCallBackImpl extends EntityDaoProcessCallBackSupport {

	private static final BizLog bizlog = BizLogUtil.getBizLog(DaoProcessCallBackImpl.class);
	
	@Override
	public void beforeDaoProcess(Class<?> intfClass, Operator operator, Object parameters) {
		beforeDaoProcess(intfClass, parameters);	
		if (intfClass == null) {
			return;
		}
		//继承公共字段表，公共数据字段处理
		if(SysCommFieldTable.KapComm.class.isAssignableFrom(intfClass)) {
			Map<String, Object> mapCommFld = CommUtil.toMap(parameters);
			if (operator.equals(Operator.INSERT)) {
				mapCommFld.put(ApDict.Aplt.userid.getId(), CommTools.getTrxRunEnvsValue("tranus"));
				mapCommFld.put(ApDict.Aplt.tmstmp.getId(), DateTools.getComputerDateTime());
			}else if(operator.equals(Operator.UPDATE)) {
				mapCommFld.put(ApDict.Aplt.updttm.getId(), DateTools.getComputerDateTime());
				mapCommFld.put(ApDict.Aplt.updtus.getId(), CommTools.getTrxRunEnvsValue("tranus"));			
			}
			
		}else if(SysCommFieldTable.KapCommSharid.class.isAssignableFrom(intfClass)) {
			Map<String, Object> mapCommFld = CommUtil.toMap(parameters);
			if (operator.equals(Operator.INSERT)) {
				mapCommFld.put(ApDict.Aplt.userid.getId(), CommTools.getTrxRunEnvsValue("tranus"));
				mapCommFld.put(ApDict.Aplt.tmstmp.getId(), DateTools.getComputerDateTime());
				mapCommFld.put(ApDict.Aplt.sharid.getId(), CoreUtil.getCurrentShardingId());  // 新增，使用当前客户号
			}else if(operator.equals(Operator.UPDATE)) {
				mapCommFld.put(ApDict.Aplt.updttm.getId(), DateTools.getComputerDateTime());
				mapCommFld.put(ApDict.Aplt.updtus.getId(), CommTools.getTrxRunEnvsValue("tranus"));			
			}
			
			
			String shardId = (String) mapCommFld.get(ApDict.Aplt.sharid.getId());
			if (CommUtil.isNotNull(shardId) ) {   
				if(!shardId.equals(CoreUtil.getCurrentShardingId())) {
				    throw new RuntimeException("表对象[%s]的当前客户号值[%s]与当前上下文客户号值[%s]不一致！");
				}
			}else{
				mapCommFld.put(ApDict.Aplt.sharid.getId(), CoreUtil.getCurrentShardingId());
			}	
		}	else if(SysCommFieldTable.KapCommCif.class.isAssignableFrom(intfClass)) {
			Map<String, Object> mapCommFld = CommUtil.toMap(parameters);
			if (operator.equals(Operator.INSERT)) {
				mapCommFld.put(BaseDict.Comm.cretus.getId(), CommTools.getTrxRunEnvsValue("tranus"));
				mapCommFld.put(ApDict.Aplt.tmstmp.getId(), DateTools.getComputerDateTime());
			}else if(operator.equals(Operator.UPDATE)) {
				mapCommFld.put(ApDict.Aplt.updttm.getId(), DateTools.getComputerDateTime());
				mapCommFld.put(ApDict.Aplt.updtus.getId(), CommTools.getTrxRunEnvsValue("tranus"));			
			}
			
		}
	}

	@Override
	public void beforeDaoProcess(Class<?> intfClass, Object parameters) {

		if (intfClass == null) {
			return;
		}

		String corpno = null;
		if (SysCommFieldTable.KapComm.class.isAssignableFrom(intfClass)) {
			corpno = this.getCorpnoByPmctl(intfClass);
		} else if (SysCommFieldTable.KapCommSpec.class.isAssignableFrom(intfClass)) {
			corpno = CommTools.getSpecCorpno(); // 专用法人代码
		} else if (SysCommFieldTable.KapCommSync.class.isAssignableFrom(intfClass)) {
			corpno = this.getCorpnoByPmctl(intfClass);
		} else if (KSysCommFieldTable.tsp_comm_filed.class.isAssignableFrom(intfClass)) {
			// 平台基础公共表时，也要由应用平台设置法人代码
			if (EngineContext.isEmpty() || CommUtil.isNull(EngineContext.getRequestData()))
				corpno = SysUtil.getDefaultTenantId(); // 平台公共表加载时，若未初始化上下文对象，则按配置的默认法人代码
			else
				corpno = this.getCorpnoByPmctl(intfClass);
		}else if (SysCommFieldTable.KapCommSharid.class.isAssignableFrom(intfClass)) {
			corpno = this.getCorpnoByPmctl(intfClass);
		}

		if (corpno != null) {
			Map<String, Object> mapCommFld = CommUtil.toMap(parameters);
			String oldno = (String) mapCommFld.get(ApDict.Aplt.corpno.getId());
			if (CommUtil.isNotNull(oldno)) {
				if (bizlog.isDebugEnabled()) {
					bizlog.debug("表对象[%s]的当前法人值[%s],不为空不需处理，按原法人号进行！", OdbFactory.getTable(intfClass).getName(), oldno);
				}
			} else {
				if (bizlog.isDebugEnabled()) {
					bizlog.debug("表对象[%s]的当前法人值为空，按法人[%s]处理！", OdbFactory.getTable(intfClass).getName(), corpno);
				}
				mapCommFld.put(ApDict.Aplt.corpno.getId(), corpno);
			}
		}
	}

	// 根据控制表获取法人代码
	private String getCorpnoByPmctl(Class<?> intfClass) {
		
		//控制是否需要检查knp_pmct表
		boolean chkPmct = false;
		KnpPara corpPara = CommTools.KnpParaQryByCorpno("system.corpno", SysUtil.getSystemId(), "%", "%",false);
		if (corpPara != null) {
			chkPmct = "1".equals(corpPara.getPmval2());
		}
		
		// 检查参数控制表:只有当前表不是knp_pmct表时才能查找，避免引起死循环
		if (chkPmct && !SysParmTable.KnpPmct.class.isAssignableFrom(intfClass)) {
			Table t = OdbFactory.getTable(intfClass);
			if (t != null) {
				String tablcd = t.getName();
				KnpPmct pmct = KnpPmctDao.selectOne_odb1(CoreUtil.getSystemId(), tablcd, false);
				if (pmct != null && pmct.getStatus() == E_VALID_.VALID) {
					if (bizlog.isDebugEnabled()) {
						bizlog.debug("表[%s]按[%s]获得查询法人！", tablcd, pmct.getPmctrl());
					}
					if (pmct.getPmctrl() == E_PMCTRL.TRAN) {
						return CommTools.getTranCorpno();
					} else if (pmct.getPmctrl() == E_PMCTRL.CENTER) {
						return CommTools.getCenterCorpno();
					} else if (pmct.getPmctrl() == E_PMCTRL.STEP) {
						throw Sys.E0001("[" + tablcd + "]暂不支持[逐级]方式查找法人参数！");
					} else {
						return CommTools.getTranCorpno();
					}
				}
			}
		}
		
		return CommTools.getTranCorpno();
	}

	/**
	 * @author Cuijia
	 * remark 修改变量名称，明确使用的变量语意，避免读取代码时造成错误理解
	 */
	@Override
	public boolean checkParm(Object databaseEntity, Object updateEntity) {

		Map<String, Object> mapOldData = CommUtil.toMap(databaseEntity);
		Map<String, Object> mapNewData = CommUtil.toMap(updateEntity);

		if (null == mapOldData || null == mapNewData) {
			return true;
		}

		Object oTimetm = mapOldData.get(ApDict.Aplt.tmstmp.getId());
		Object nTimetm = mapNewData.get(ApDict.Aplt.tmstmp.getId());

		if (null == oTimetm && null == nTimetm) {
			if (bizlog.isDebugEnabled())
				bizlog.debug("数据库与内存信息一致且均为空");
			return true;
		}
		if (null == oTimetm || null == nTimetm) {
			if (bizlog.isErrorEnabled())
				bizlog.error("数据库与内存信息不一致[%s]-[%s]", oTimetm, nTimetm);
			throw Sys.E0001("数据库记录已被修改，数据不一致");
		}
		if (!oTimetm.equals(nTimetm)) {
			if (bizlog.isErrorEnabled())
				bizlog.error("数据库与内存信息不一致[%s]-[%s]", oTimetm, nTimetm);
			throw Sys.E0001("数据库记录已被修改，数据不一致");
		} else {
			if (bizlog.isDebugEnabled())
				bizlog.debug("数据库与内存信息一致[%s]-[%s]", oTimetm, nTimetm);
			return true;
		}

	}

	@Override
	public void parameterProcess(Class<?> type, Object newEntity) {
		// 非业务公共表直接返回
		if (!isBizCommonTable(type))
			return;

		Map<String, Object> mapNewData = CommUtil.toMap(newEntity);
		/**
		 * 统一使用当前交易RunEnvs中的时间戳，好处是：同一个交易更新或插入表的记录，时间戳一致的；
		 * 不好：是否存在本来时间戳应该不同的，但实际却相同（并发交易导致）。
		 */
		RunEnvsComm runEnvs = SysUtil.getTrxRunEnvs();
		// 查询某些表时，可能还没有初始化RunEnvs
		if (CommUtil.isNull(runEnvs.getTmstmp()))
			runEnvs.setTmstmp(DateTools.getTransTimestamp());
		mapNewData.put(ApDict.Aplt.tmstmp.getId(), runEnvs.getTmstmp());
	}

	@Override
	public int registerChangeLog(Clog entity) {

		return 0;
	}

	@Override
	public int[] registerChangeLogs(List<Clog> entitys) {

		return new int[0];
	}

	// 判断是否业务公共表
	private boolean isBizCommonTable(Class<?> type) {
		if (SysCommFieldTable.KapComm.class.isAssignableFrom(type)
				|| SysCommFieldTable.KapCommSync.class.isAssignableFrom(type)) {
			return true;
		}
		return false;
	}
}
