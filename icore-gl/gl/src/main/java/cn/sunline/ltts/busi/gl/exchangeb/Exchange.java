package cn.sunline.ltts.busi.gl.exchangeb;

import java.util.List;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.gl.namedsql.GlExchangeDao;
import cn.sunline.ltts.busi.gl.regBook.GlRegBook;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.Glb_exchange_rateDao;
import cn.sunline.ltts.busi.gl.tables.TabGLBasic.glb_exchange_rate;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRate;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRateIn;
import cn.sunline.ltts.busi.gl.type.GlExchange.GlExchangeRateInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.GlEnumType.E_DATAOPERATE;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;

/**
 * <p>
 * 文件功能说明：
 * </p>
 * 
 * @Author pc
 *         <p>
 *         <li>2017年5月2日-下午4:11:49</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228 pc：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class Exchange {

	private static final BizLog BIZLOG = BizLogUtil.getBizLog(GlRegBook.class);

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月2日-下午6:29:18</li>
	 *         <li>功能说明：折算汇率添加</li>
	 *         </p>
	 * @param addIn
	 */
	public static void addExchangeRateInfo(GlExchangeRate addIn) {

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		// 检查数据的合法性
		ExchangeUtials.checkBusinessValidity(addIn);
		// 数据库存在则报错
		if (Glb_exchange_rateDao.selectOne_odb1(addIn.getTrxn_date(), addIn.getCcy_code(), addIn.getExchange_ccy_code(), false) != null) {
			throw GlError.GL.E0037();
		}
		// 日期是否是大于等于今天
		if (addIn.getTrxn_date().compareTo(runEnvs.getTrandt()) == 0) {
			throw GlError.GL.E0110();
		}
		// 币种是否在参数表中，不在则报错
		// if (!ApCurrency.exists(addIn.getCcy_code())) {
		// throw
		// ApPubErr.APPUB.E0005(OdbFactory.getTable(app_currency.class).getLongname(),
		// ApDict.A.ccy_code.getLongName(), addIn.getCcy_code());
		// }
		glb_exchange_rate tabExchangeRate = SysUtil.getInstance(glb_exchange_rate.class);
		tabExchangeRate.setCcy_code(addIn.getCcy_code());
		tabExchangeRate.setExchange_ccy_code(addIn.getExchange_ccy_code());
		tabExchangeRate.setExchange_rate(addIn.getExchange_rate());
		tabExchangeRate.setTrxn_date(addIn.getTrxn_date());
		tabExchangeRate.setExchange_rate_unit(addIn.getExchange_rate_unit());
		Glb_exchange_rateDao.insert(tabExchangeRate);
		// ApDataAudit.regLogOnInsertParameter(addIn);
		ApDataAudit.regLogOnInsertParameter(tabExchangeRate);
	}

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月2日-下午4:12:01</li>
	 *         <li>功能说明：折算汇率信息维护</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<GlExchangeRate> getExchangeRateList(Options<GlExchangeRate> queryIn) {
		BIZLOG.method("ExchangeB.upd>>begin>>>>>>>>>>>>>>>");
		BIZLOG.debug("queryIn[%s]", queryIn);

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		List<GlExchangeRate> queryInList = queryIn.getValues();

		for (GlExchangeRate queryInfo : queryInList) {
			if (CommUtil.isNull(queryInfo.getOperater_ind())) {
				continue;
			} else if (queryInfo.getOperater_ind() == E_DATAOPERATE.ADD) { // 新增一条记录
				addExchangeRateInfo(queryInfo);
			} else if (queryInfo.getOperater_ind() == E_DATAOPERATE.DELETE) {// 删除一条记录
				// 是否存在，不存在则报错
				// glb_exchange_rate exgRateTable =
				// Glb_exchange_rateDao.selectOne_odb1(queryInfo.getTrxn_date(),
				// queryInfo.getCcy_code(), queryInfo.getExchange_ccy_code(),
				// false);
				// 日期是否是等于今天
				if (queryInfo.getTrxn_date().compareTo(runEnvs.getTrandt()) < 0) {
					throw GlError.GL.E0110();
				}
				Glb_exchange_rateDao.deleteOne_odb1(queryInfo.getTrxn_date(), queryInfo.getCcy_code(), queryInfo.getExchange_ccy_code());
				// queryIn.remove(queryInfo); //rambo delete

			} else if (queryInfo.getOperater_ind() == E_DATAOPERATE.MODIFY) {// 修改一条记录
				// 判断非空字段是否为空
				ExchangeUtials.checkBusinessValidity(queryInfo);
				// 判断主键是否存在数据库，B不存在则报错
				glb_exchange_rate tabExgRate = Glb_exchange_rateDao.selectOne_odb1(queryInfo.getTrxn_date(), queryInfo.getCcy_code(), queryInfo.getExchange_ccy_code(), false);
				// 日期是否是等于今天
				if (queryInfo.getTrxn_date().compareTo(runEnvs.getTrandt()) == 0) {
					throw GlError.GL.E0110();
				}
				// 币种是否在参数表中，不在则报错
				// if (!ApCurrency.exists(queryInfo.getCcy_code())) {
				// throw
				// ApPubErr.APPUB.E0005(OdbFactory.getTable(app_currency.class).getLongname(),
				// ApDict.A.ccy_code.getLongName(), queryInfo.getCcy_code());
				// }
				tabExgRate.setCcy_code(queryInfo.getCcy_code());
				tabExgRate.setExchange_ccy_code(queryInfo.getExchange_ccy_code());
				tabExgRate.setExchange_rate(queryInfo.getExchange_rate());
				tabExgRate.setTrxn_date(queryInfo.getTrxn_date());
				tabExgRate.setExchange_rate_unit(queryInfo.getExchange_rate_unit());
				// tabExgRate.setRecdver(tabExgRate.getRecdver+1);
				ExchangeUtials.checkUpdateValidity(tabExgRate);
				Glb_exchange_rateDao.updateOne_odb1(tabExgRate);
				// 登记审计日志
				ApDataAudit.regLogOnInsertParameter(tabExgRate);
			}
		}

		BIZLOG.debug("queryOut[%s]", queryIn);
		BIZLOG.method("ExchangeB.upd>>begin>>>>>>>>>>>>>>>");
		return queryIn;
	}

	/**
	 * @Author pc
	 *         <p>
	 *         <li>2017年5月3日-下午1:39:17</li>
	 *         <li>功能说明：折算汇率查询</li>
	 *         </p>
	 * @param queryIn
	 * @return
	 */
	public static Options<GlExchangeRateInfo> getExchangeRateInfo(GlExchangeRateIn queryIn) {
		// 折算信息查询
		BIZLOG.method("ExchangeB.getExchangeRateInfo>>begin>>>>>>>>>>>>");
		BIZLOG.debug("queryIn[%s]", queryIn);

		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		Page<GlExchangeRateInfo> exhgRateList = GlExchangeDao.lstExchangeRateInfo(queryIn.getTrxn_date(), queryIn.getCcy_code(), queryIn.getExchange_ccy_code(), queryIn.getExchange_rate_unit(),
				runEnvs.getCorpno(), runEnvs.getPageno(), runEnvs.getPgsize(), runEnvs.getCounts(), false);

		runEnvs.setCounts(exhgRateList.getRecordCount());

		Options<GlExchangeRateInfo> output = new DefaultOptions<GlExchangeRateInfo>();
		output.setValues(exhgRateList.getRecords());
		runEnvs.setCounts(exhgRateList.getRecordCount());

		BIZLOG.method("ExchangeB.getExchangeRateInfo>>end>>>>>>>>>>>>");
		BIZLOG.debug("output[%s]", output);

		return output;
	}

}
