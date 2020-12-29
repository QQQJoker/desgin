package cn.sunline.ltts.busi.aplt.audit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplBusiAudt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplBusiAudtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplBusiAudtDetl;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplParaAudt;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplParaAudtDao;
import cn.sunline.ltts.busi.aplt.tables.SysPublicTable.AplParaAudtDetl;
import cn.sunline.ltts.busi.aplt.tools.ApSeq;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DMLTYP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_SERVTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_TABLETYPE;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.adp.metadata.model.database.Table;

/**
 * <p>
 * 文件功能说明：参数和业务数据审计
 * </p>
 * 
 * @Author lidi
 *         <p>
 *         <li>2016年12月5日-下午10:04:29</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20161205 jollyja：创建</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApDataAudit {

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApDataAudit.class);

	/**
	 * @Author lidi
	 *         <p>
	 *         <li>2016年12月6日-下午1:29:11</li>
	 *         <li>功能说明：新增参数审计登记</li>
	 *         </p>
	 * @param record
	 */
	public static void regLogOnInsertParameter(Object record) {

		bizlog.method(" ApDataAudit.regLogOnInsertParameter begin >>>>>>>>>>>>>>>>");

		regLogOnInsAndDelParm(record, E_DMLTYP.ADD);

		bizlog.method(" ApDataAudit.regLogOnInsertParameter end <<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author lidi
	 *         <p>
	 *         <li>2016年12月6日-下午1:29:11</li>
	 *         <li>功能说明：删除参数审计登记</li>
	 *         </p>
	 * @param record
	 */
	public static void regLogOnDeleteParameter(Object record) {

		bizlog.method(" ApDataAudit.regLogOnDeleteParameter begin >>>>>>>>>>>>>>>>");

		regLogOnInsAndDelParm(record, E_DMLTYP.DELETE);

		bizlog.method(" ApDataAudit.regLogOnDeleteParameter end <<<<<<<<<<<<<<<<");
	}
	
	//add poc审计日志
	public static void regLogOnInsertBusiPoc(String cardno) {

		bizlog.method(" ApDataAudit.regLogOnInsertBusiPoc begin >>>>>>>>>>>>>>>>");
		regBusiInsAndDelParmPoc(E_DMLTYP.ADD,cardno);
		bizlog.method(" ApDataAudit.regLogOnInsertBusiPoc end <<<<<<<<<<<<<<<<");
	}

	/**
	 * @Author lidi
	 *         <p>
	 *         <li>2016年12月6日-下午3:48:32</li>
	 *         <li>功能说明：修改参数审计登记，返回本次修改的字段数，为0表示没有维护</li>
	 *         </p>
	 * @param oldRecord
	 * @param newRecord
	 * @return int 登记记录数
	 */
	public static int regLogOnUpdateParameter(Object oldRecord, Object newRecord) {

		return regLogOnUpdate(oldRecord, newRecord, E_TABLETYPE.PARAMETER);

	}

	/**
	 * @Author lidi
	 *         <p>
	 *         <li>2016年12月8日-下午3:52:52</li>
	 *         <li>功能说明：修改业务审计登记，返回本次修改的字段数，为0表示没有维护</li>
	 *         </p>
	 * @param oldRecord
	 * @param newRecord
	 * @param tableType
	 * @return
	 */
	public static int regLogOnUpdateBusiness(Object oldRecord, Object newRecord) {

		return regLogOnUpdate(oldRecord, newRecord, E_TABLETYPE.BUSINESS);

	}

	/**
	 * 由于新增跟删除处理差不多，因此内部放入同一个方法处理
	 */
	private static void regLogOnInsAndDelParm(Object record, E_DMLTYP operateType) {

		Table table = OdbFactory.getTable(record.getClass());// 表对象,如果实体类没有对应的表定义，先抛出异常
		String auditSeq = getAuditSeq();// 审计序号

		regAuditParm(record, operateType, table, auditSeq);
	}

	
	//add poc 
	private static void regBusiInsAndDelParmPoc(E_DMLTYP operateType,String cardno) {
		String auditSeq = getAuditSeq();// 审计序号
		regAuditBusiPoc(operateType, auditSeq,cardno);
	}
	/**
	 * 内部使用修改操作
	 */
	private static int regLogOnUpdate(Object oldRecord, Object newRecord, E_TABLETYPE tableType) {
		bizlog.method(" ApDataAudit.regLogOnUpdate begin >>>>>>>>>>>>>>>>");
		bizlog.debug("table type is " + tableType.toString());

		 cn.sunline.adp.metadata.model.database.Table table = cn.sunline.adp.metadata.base.odb.OdbFactory.getTable(oldRecord.getClass());// 表对象,如果实体类没有对应的表定义，先抛出异常
		String tableName = SysUtil.getTableName(oldRecord.getClass());// 表名
		String newTableName = SysUtil.getTableName(newRecord.getClass());

		// 如果old实体类跟new实体类不是一个表，抛出异常
		if (!(tableName.equals(newTableName))) {
			// throw 登记审计日志出错 ，修改前表是[oldTableName] 而修改后表是[newTableName]
			throw ApError.Aplt.E0038(tableName, newTableName);
		}

		Map<String, Object> beforeData = CommUtil.toMap(oldRecord); // 修改前数据
		Map<String, Object> afterData = CommUtil.toMap(newRecord); // 修改后数据
		String auditSeq = getAuditSeq();// 审计序号

		// 审计子表处理
		int n = regAuditSub(beforeData, afterData, tableType, table, auditSeq);
		
		if(n > 0){
			// 审计主表处理,没有字段变化，不插入主表
			regAudit(newRecord, E_DMLTYP.MODIFY, tableType, table, auditSeq);
		}

		bizlog.method(" ApDataAudit.regLogOnUpdate end <<<<<<<<<<<<<<<<");
		return n;
	}

	/**
	 *获取参数序号
	 */
	private static String getAuditSeq() {
		//modify by wuwei 2071201 增加dcn
		return CommTools.prcRunEnvs().getCdcnno()+ApSeq.genSeq("AUDIT_SEQ");
	}

	/**
	 *通用主表处理
	 */
	private static void regAudit(Object record, E_DMLTYP operateType, E_TABLETYPE tableType, Table table, String auditSeq) {
        //参数表审计处理
		if (E_TABLETYPE.PARAMETER == tableType) {
			regAuditParm(record, operateType, table, auditSeq);// 参数表审计主表处理
		}
		//业务表审计处理
		if(E_TABLETYPE.BUSINESS == tableType){
			regAuditBusi(record, operateType, table, auditSeq);// 业务表审计主表处理
		}
	}

	/**
	 * 通用子表处理
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static int regAuditSub(Map<String,Object> beforeData, Map<String,Object> afterData, 
			                     E_TABLETYPE tableType, Table table, String auditSeq){
		
		List<Object> auditSubInfo = new ArrayList<Object>();//子表信息
		
		// 所有字段信息
		List<Field> fields = table.getFields();
		// 公共字段列表

		int n = 0;
		// 明细表信息录入
		for (Field field : fields) {
			String fieldName = field.getId();

			// 判断修改前和修改后的数据是否相等，相等就不插入审计明细表
			Object o1 = beforeData.get(fieldName);
			Object o2 = afterData.get(fieldName);
			String beforeValue = CommUtil.isNull(o1) ? "" : o1.toString();
			String afterValue = CommUtil.isNull(o2) ? "" : o2.toString();
			if (beforeValue.equals(afterValue))
				continue;

			Object auditSub = null;
			if (E_TABLETYPE.PARAMETER == tableType) {
				auditSub = regAuditSubParm(field, beforeValue, afterValue, auditSeq, (long)n);
			}
			if(E_TABLETYPE.BUSINESS == tableType){
				auditSub = regAuditSubBusi(field, beforeValue, afterValue, auditSeq, (long)n);;
			}
			
			auditSubInfo.add(auditSub);
			n++;
		}
		
		Class auditSub = (E_TABLETYPE.PARAMETER == tableType) ? AplParaAudtDetl.class : AplBusiAudtDetl.class;
		
		if(n > 0){
			if(n == 1){
				DaoUtil.insert(auditSub, auditSubInfo.get(0));//单记录插入
			}else{
				DaoUtil.insertBatch(auditSub, auditSubInfo);//批量插入
			}
		}
		
		return n;

	}

	/**
	 * 参数审计主表处理
	 */
	private static void regAuditParm(Object record, E_DMLTYP operateType, Table table, String auditSeq) {

		AplParaAudt auditInfo = SysUtil.getInstance(AplParaAudt.class);// 参数表审计主表
		// 主表信息录入
		auditInfo.setAudseq(auditSeq); // 参数审计序号
		auditInfo.setTrandt(CommTools.prcRunEnvs().getTrandt());// 交易日期
		auditInfo.setTransq(CommTools.prcRunEnvs().getTransq()); // 交易流水
		auditInfo.setBusisq(CommTools.prcRunEnvs().getBusisq()); // 业务流水
		auditInfo.setPrcscd(CommTools.prcRunEnvs().getPrcscd()); // 交易码
		auditInfo.setPrcsna(CommTools.prcRunEnvs().getPrcsna()); // 交易描述
		auditInfo.setDatatp(operateType); // 操作类型
		auditInfo.setTablna(table.getName()); // 表名
		auditInfo.setTablds(table.getLongname()); // 表描述
		auditInfo.setPkyval(CommTools.getTablePkValue(record)); // 主键
		auditInfo.setUserid(CommTools.prcRunEnvs().getTranus()); //交易柜员
		AplParaAudtDao.insert(auditInfo);
	}

	/**
	 *业务审计主表处理
	 */
	private static void regAuditBusi(Object record, E_DMLTYP operateType, Table table, String auditSeq) {
		AplBusiAudt auditBusiInfo = SysUtil.getInstance(AplBusiAudt.class);
		auditBusiInfo.setAudseq(auditSeq); // 参数审计序号
		auditBusiInfo.setTrandt(CommTools.prcRunEnvs().getTrandt());// 交易日期
		auditBusiInfo.setTransq(CommTools.prcRunEnvs().getTransq()); // 交易流水
		auditBusiInfo.setBusisq(CommTools.prcRunEnvs().getBusisq()); // 业务流水
		auditBusiInfo.setPrcscd(CommTools.prcRunEnvs().getPrcscd()); // 交易码
		auditBusiInfo.setPrcsna(CommTools.prcRunEnvs().getPrcsna()); // 交易描述
		auditBusiInfo.setTablna(table.getName()); // 表名
		auditBusiInfo.setTablds(table.getLongname()); // 表描述
		auditBusiInfo.setPkyval(CommTools.getTablePkValue(record)); // 主键
		auditBusiInfo.setServtp(CommTools.prcRunEnvs().getServtp());//交易渠道
		auditBusiInfo.setUserid(CommTools.prcRunEnvs().getTranus());//操作人员
		AplBusiAudtDao.insert(auditBusiInfo);
	}
	
	
	/**
	 *业务审计主表处理
	 */
	private static void regAuditBusiPoc(E_DMLTYP operateType,String auditSeq,String cardno) {
		AplBusiAudt auditBusiInfo = SysUtil.getInstance(AplBusiAudt.class);
		auditBusiInfo.setAudseq(auditSeq); // 参数审计序号
		auditBusiInfo.setTrandt(CommTools.prcRunEnvs().getTrandt());// 交易日期
		auditBusiInfo.setTransq(CommTools.prcRunEnvs().getTransq()); // 交易流水
		auditBusiInfo.setBusisq(CommTools.prcRunEnvs().getBusisq()); // 业务流水
		auditBusiInfo.setPrcscd(CommTools.prcRunEnvs().getPrcscd()); // 交易码
		auditBusiInfo.setPrcsna(CommTools.prcRunEnvs().getPrcsna()); // 交易描述
		//auditBusiInfo.setTablna(table.getName()); // 表名
		//auditBusiInfo.setTablds(table.getLongname()); // 表描述
		//auditBusiInfo.setPkyval(CommTools.getTablePkValue(record)); // 主键
		//poc 新增
		if(CommUtil.isNotNull(CommTools.prcRunEnvs().getServtp())&&CommTools.prcRunEnvs().getServtp()!=E_SERVTP.EB ){
			auditBusiInfo.setServtp(CommTools.prcRunEnvs().getServtp());//交易渠道
			auditBusiInfo.setUserid(cardno);//操作人员
		}else{	
			auditBusiInfo.setServtp(E_SERVTP.EB);//交易渠道
			auditBusiInfo.setUserid(CommTools.prcRunEnvs().getTranus());//操作人员
		}
		AplBusiAudtDao.insert(auditBusiInfo);
	}
	
	/**
	 *登记参数审计子表
	 */
	private static Object regAuditSubParm(Field field, String beforeValue, String afterValue, String auditSeq, long dataSort){
		// 参数表审计明细表信息录入
		AplParaAudtDetl paramAuditDetail = SysUtil.getInstance(AplParaAudtDetl.class);
		paramAuditDetail.setAudseq(auditSeq); // 参数审计序号
		paramAuditDetail.setDatast(dataSort); // 数据序号
		paramAuditDetail.setTrandt(CommTools.prcRunEnvs().getTrandt());  // 交易日期
		paramAuditDetail.setFildna(field.getId()); // 字段名
		paramAuditDetail.setFildds(field.getLongname()); // 字段描述
		paramAuditDetail.setBefval(beforeValue); // 变动前数据
		paramAuditDetail.setAftval(afterValue); // 变动后数据 
		
		return paramAuditDetail;
	}
	
	/**
	 *登记业务审计子表
	 */
	private static Object regAuditSubBusi(Field field, String beforeValue, String afterValue, String auditSeq, long dataSort){
		AplBusiAudtDetl busiAudtDetail = SysUtil.getInstance(AplBusiAudtDetl.class);
		busiAudtDetail.setAudseq(auditSeq); // 业务审计序号
		busiAudtDetail.setDatast(dataSort); // 数据序号
		busiAudtDetail.setTrandt(CommTools.prcRunEnvs().getTrandt()); // 交易日期
		busiAudtDetail.setFildna(field.getId()); // 字段名
		busiAudtDetail.setFildds(field.getLongname()); // 字段描述
		busiAudtDetail.setBefval(beforeValue); // 变动前数据
		busiAudtDetail.setAftval(afterValue); // 变动后数据 
		
		return busiAudtDetail;
	}


}
