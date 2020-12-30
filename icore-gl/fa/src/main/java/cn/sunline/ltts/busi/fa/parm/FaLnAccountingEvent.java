package cn.sunline.ltts.busi.fa.parm;

import java.math.BigDecimal;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_prod_scene;
import cn.sunline.ltts.busi.fa.type.ComFaLnAccounting.FaLnAccountingSceneInfo;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.sys.dict.GlDict;

/**
 * 
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author 
 *         <p>
 *         <li>2020年10月20日-下午4:36:44</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2020年10月20日：场景核算事件管理配套方法</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaLnAccountingEvent {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaLnAccountingEventMnt.class);

	/**
	 * 
	 * @Author
	 *         <p>
	 *         <li>2020年10月20日-下午4:37:49</li>
	 *         <li>功能说明：基本非空校验</li>
	 *         </p>
	 * @param scene
	 */
	public static void checkNull(fap_accounting_prod_scene scene) {
		bizlog.method(" FaLnAccountingEvent.checkNull begin >>>>>>>>>>>>>>>>");
		CommTools.fieldNotNull(scene.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		CommTools.fieldNotNull(scene.getScene_code(), GlDict.A.scene_code.getId(), GlDict.A.scene_code.getLongName());
		CommTools.fieldNotNull(scene.getProduct_code(), GlDict.A.product_code.getId(), GlDict.A.product_code.getLongName());
		CommTools.fieldNotNull(scene.getBal_type(), GlDict.B.bal_type.getId(), GlDict.A.bal_type.getLongName());
		CommTools.fieldNotNull(scene.getData_sort(), GlDict.A.data_sort.getId(), GlDict.A.data_sort.getLongName());
		CommTools.fieldNotNull(scene.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		bizlog.method(" FaLnAccountingEvent.checkNull end <<<<<<<<<<<<<<<<");
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月22日-下午7:15:03</li>
	 *         <li>功能说明：场景事件解析非空入账</li>
	 *         </p>
	 * @param info
	 */
	public static void checkNull(FaLnAccountingSceneInfo info) {
		bizlog.method(" FaLnAccountingEvent.checkNull begin >>>>>>>>>>>>>>>>");
		CommTools.fieldNotNull(info.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName()); // 系统编号
		CommTools.fieldNotNull(info.getScene_code(), GlDict.A.scene_code.getId(), GlDict.A.scene_code.getLongName()); // 场景编号
		CommTools.fieldNotNull(info.getProduct_code(), GlDict.A.product_code.getId(), GlDict.A.product_code.getLongName()); // 产品编号
		CommTools.fieldNotNull(info.getBal_type(), GlDict.A.bal_type.getId(), GlDict.A.bal_type.getLongName()); // 金额类别
		CommTools.fieldNotNull(info.getLoan_term(), GlDict.A.loan_term.getId(), GlDict.A.loan_term.getLongName()); // 金额类别
		CommTools.fieldNotNull(info.getTrxn_date(), GlDict.A.trxn_date.getId(), GlDict.A.trxn_date.getLongName()); // 交易日期
		CommTools.fieldNotNull(info.getTrxn_ccy(), GlDict.A.trxn_ccy.getId(), GlDict.A.trxn_ccy.getLongName()); // 币种
		CommTools.fieldNotNull(info.getTrxn_amt(), GlDict.A.tran_amount.getId(), GlDict.A.tran_amount.getLongName());  // 交易金额
		if(CommUtil.compare(info.getTrxn_amt(), BigDecimal.ZERO) <= 0) {
			throw GlError.GL.E0041();
		}
		bizlog.method(" FaLnAccountingEvent.checkNull end <<<<<<<<<<<<<<<<");
	}
	
	
	
}
