package cn.sunline.ltts.busi.aplt.para;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sunline.adp.cedar.busi.sdk.biz.global.ClassUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.PkgUTil;
import cn.sunline.adp.cedar.base.util.CommUtil;
//import cn.sunline.adp.cedar.busi.sdk.biz.global.PkgUTil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.param.ParamTableProcessor;
import cn.sunline.adp.cedar.busi.sdk.biz.param.ParamTableUtil;
//import cn.sunline.adp.cedar.busi.sdk.biz.param.ParamTableProcessor;
//import cn.sunline.adp.cedar.busi.sdk.biz.param.ParamTableUtil;
import cn.sunline.ltts.busi.aplt.type.ApDefineType.ApParaMatain;
import cn.sunline.adp.metadata.loader.db.util.DaoUtil;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_CARDCZBZ;
import cn.sunline.adp.core.exception.AdpDaoDuplicateException;
import cn.sunline.adp.core.exception.AdpDaoNoDataFoundException;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;

/**
 * <p>
 * 文件功能说明：参数管理
 *       			
 * </p>
 * 
 * @Author Madong
 *         <p>
 *         <li>2016年1月5日-下午3:55:48</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228Madong：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApParaManage {
	public static final String ODB1_CLASS_NAME = "odb1";
	private static final String ODB2_CLASS_NAME = "odb2";
	private static final String RESULT_KEY = "result";

	private static final BizLog bizlog = BizLogUtil.getBizLog(ApParaManage.class);

	/**
	 * @Author Madong
	 *         <p>
	 *         <li>2016年1月5日-下午3:56:27</li>
	 *         <li>功能说明：参数流程维护</li>
	 *         </p>
	 * @param cplApParaMatainIn
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String prcParaMatain(ApParaMatain cplApParaMatainIn) {
		bizlog.method("prcParaMatain begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("cplApParaMatainIn [%s]", cplApParaMatainIn);
		
		Map<String, Object> dataMap = PkgUTil.deserialize(cplApParaMatainIn.getShujubao());
		Map<String, Object> newdataMap = PkgUTil.deserialize(cplApParaMatainIn.getShujubao());
		Class<Object> tableClass = (Class<Object>) ClassUtil.getTableClass(cplApParaMatainIn.getCanshubm());
		Object entity = SysUtil.getInstance(tableClass);
		Object entity2 = SysUtil.getInstance(tableClass);
		// 此处的属性拷贝需要做类型转换
		CommUtil.copyPropertiesWithTypeConvert(entity, dataMap);
		CommUtil.copyPropertiesWithTypeConvert(entity2, newdataMap);
		Map<String, Object> result = new HashMap<String, Object>();

		// 获取参数处理器
		ParamTableProcessor processor = ParamTableUtil.getParamTableProcessor(cplApParaMatainIn.getCanshubm());

		String operator = cplApParaMatainIn.getCaozuolx() == null ? null : cplApParaMatainIn.getCaozuolx().toString();

		// 如果存在参数处理器，则调用前处理
		if (processor != null)
			processor.before(operator, entity);

		if (E_CARDCZBZ.ADD == cplApParaMatainIn.getCaozuolx()) {
			try {
				result.put(RESULT_KEY, DaoUtil.insert(tableClass, entity));
			}
			catch (AdpDaoDuplicateException e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("插入失败，记录已存在!");
			}
			catch (Exception e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("数据库异常，请联系技术人员！", e);
			}

			// 如果存在参数处理器，则调用后处理
			if (processor != null)
				processor.after(operator, entity);

			//TODO: 登记变更信息表
//			BusiTools.registBgxx(E_CARDCZBZ.ADD, null, entity2, cplApParaMatainIn.getCanshubm());

		}
		else if (E_CARDCZBZ.SEL == cplApParaMatainIn.getCaozuolx()) {

			int qishibis = cplApParaMatainIn.getQishibis() != null ? cplApParaMatainIn.getQishibis().intValue() : 0;
			int chxunbis = (cplApParaMatainIn.getChxunbis() == null || cplApParaMatainIn.getChxunbis() == 0) ? Integer.MAX_VALUE : cplApParaMatainIn.getChxunbis().intValue();

			try {
				
				
				 List<Object> results = DaoUtil.selectPageByIndex(tableClass,
				 ClassUtil.getOdbClass(tableClass, ODB2_CLASS_NAME), true,qishibis, chxunbis, false, entity);
				 
				 result.put(RESULT_KEY, results);
				 
				/*Page<Object> results = DaoUtil.selectPageWithCountByIndex(tableClass, ClassUtil.getOdbClass(tableClass, ODB2_CLASS_NAME), true, qishibis, chxunbis, CommTools
						.prcRunEnvs().getZongbshu().intValue(), false, entity);

				result.put(RESULT_KEY, results.getRecords());
				CommToolsAplt.prcRunEnvs().setZongbshu(results.getRecordCount());*/
			}
			catch (AdpDaoNoDataFoundException e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("查询数据记录不存在!");
			}
			catch (Exception e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("数据库异常，请联系技术人员！", e);
			}

		}
		else if (E_CARDCZBZ.UPD == cplApParaMatainIn.getCaozuolx()) {
			Object objFromDb = null;
			Object objFromDb2 = SysUtil.getInstance(tableClass);
			try {
				objFromDb = DaoUtil.selectOneByIndex(tableClass, ClassUtil.getOdbClass(tableClass, ODB1_CLASS_NAME), entity);
				CommUtil.copyPropertiesWithTypeConvert(objFromDb2, objFromDb, false);
				CommUtil.copyPropertiesWithTypeConvert(objFromDb, dataMap, false);
				result.put(RESULT_KEY, DaoUtil.updateByIndex(tableClass, ClassUtil.getOdbClass(tableClass, ODB1_CLASS_NAME), objFromDb));
			}
			catch (AdpDaoDuplicateException e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("数据更新失败，存在多条记录!");
			}
			catch (AdpDaoNoDataFoundException e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("查询数据记录不存在!");
			}
			catch (Exception e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("数据库异常，请联系技术人员！", e);
			}

			// 如果存在参数处理器，则调用后处理
			if (processor != null){
				processor.after(operator, objFromDb);
			}
			//TODO: 登记变更信息表
//			BusiTools.registBgxx(E_CARDCZBZ.UPD, objFromDb2, entity2, cplApParaMatainIn.getCanshubm());

		}
		else if (E_CARDCZBZ.DEL == cplApParaMatainIn.getCaozuolx()) {
			Object objFromDb = null;
			Object objFromDb2 = SysUtil.getInstance(tableClass);
			try {
				objFromDb = DaoUtil.selectOneByIndex(tableClass, ClassUtil.getOdbClass(tableClass, ODB1_CLASS_NAME), entity);
				CommUtil.copyPropertiesWithTypeConvert(objFromDb2, objFromDb, false);
				CommUtil.copyPropertiesWithTypeConvert(objFromDb, dataMap);
				result.put(RESULT_KEY, DaoUtil.deleteByIndex(tableClass, ClassUtil.getOdbClass(tableClass, ODB1_CLASS_NAME), objFromDb));
			}
			catch (AdpDaoNoDataFoundException e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("删除数据失败，记录不存在!");
			}
			catch (Exception e) {
				bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
				throw Aplt.E0000("数据库异常，请联系技术人员！", e);
			}

			// 如果存在参数处理器，则调用后处理
			if (processor != null)
				processor.after(operator, objFromDb);

			//TODO: 登记变更信息表
//			BusiTools.registBgxx(E_CARDCZBZ.DEL, objFromDb2, null, cplApParaMatainIn.getCanshubm());

		}
		else {
			bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");
			throw new IllegalArgumentException("不支持的参数操作类型[" + cplApParaMatainIn.getCaozuolx() + "]");
		}
		
		bizlog.parm("var_out [%s]", PkgUTil.serialize(result));
		bizlog.method("prcParaMatain end <<<<<<<<<<<<<<<<<<<<");

		return PkgUTil.serialize(result);

	}
}