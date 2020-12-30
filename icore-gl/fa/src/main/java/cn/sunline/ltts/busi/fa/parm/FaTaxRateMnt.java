package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaLoanAccountingDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_tax_rateDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_tax_rate;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_VALID_STATUS;

public class FaTaxRateMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaTaxRateMnt.class);

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月18日-下午4:24:24</li>
	 *         <li>功能说明：税率新增</li>
	 *         </p>
	 * @param addIn
	 * @return
	 */
	public static String addTaxRate(Options<fap_tax_rate> addIn) {
		
		bizlog.method(" FaTaxRateMnt.addTaxRate begin >>>>>>>>>>>>>>>>");
		
		// 输入集合校验
		if(addIn.isEmpty()) {
			throw GlError.GL.E0219();
		}
		
		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String tax_seq = getTaxSeq(runEnvs.getTranbr(), runEnvs.getTrandt());
		
		// 遍历处理每一项
		for (fap_tax_rate rateTmp : addIn) {
				
			// 基本非空校验
			FaTaxRate.checkRateNull(rateTmp);
			
			//判断是否存在生效记录
			fap_tax_rate rate = FaLoanAccountingDao.selFapTaxRateByStatus(runEnvs.getCorpno(), 
					rateTmp.getBranch_code(), rateTmp.getBusiness_code(), E_VALID_STATUS.VALID, false);
			
			//比较新增税率的生效日期是否大于生效税率的失效日期
			if(CommUtil.isNotNull(rate)){
				if(CommUtil.compare(rateTmp.getEffect_date(),rate.getInvalid_date())<=0){
					throw GlError.GL.E0223(rateTmp.getEffect_date(), rate.getInvalid_date());
				}
			}
			
			// 机构校验
			FaLoanAccountingEvent.existsFapSysDefine(runEnvs.getInpucd());
			
			// 查询记录是否存在
			fap_tax_rate fapTaxRate = FaLoanAccountingDao.selFapTaxRateByEffectDate(runEnvs.getCorpno(), rateTmp.getBranch_code(), 
					rateTmp.getBranch_code(), rateTmp.getCal_tax_method_code(), runEnvs.getTrandt(), E_VALID_STATUS.CHECK, false);
			if(CommUtil.isNotNull(fapTaxRate)) {
				throw GlError.GL.E0224();
			}
				
			// 税率新增
			rateTmp.setTax_seq(tax_seq);  					// 套号
			rateTmp.setCal_tax_method_name(rateTmp.getCal_tax_method_code().getLocalLongName());	// 计算方法名称
			rateTmp.setInit_date(runEnvs.getTrandt());		// 数据初始化日期
			rateTmp.setRecdver(1l);							// 数据版本号
			rateTmp.setTran_user(runEnvs.getTranus());  	// 新增柜员
			rateTmp.setValid_status(E_VALID_STATUS.VALID);	// 有效
			rateTmp.setAuth_user(runEnvs.getCkbsus());   	// 授权柜员
			Fap_tax_rateDao.insert(rateTmp);
	
		}
		
		bizlog.method(" FaTaxRateMnt.addTaxRate end <<<<<<<<<<<<<<<<");
		
		return tax_seq;
	}
	
	public static String getTaxSeq(String brchno, String trandt){
		//组成规则 W+机构号+交易日期+seq(三位顺序号)
		String vatxno = "W".concat(brchno).concat(trandt).concat("%");
		String taxSeq = FaLoanAccountingDao.selMaxTaxSeq(vatxno, CommTools.prcRunEnvs().getCorpno(), false);
		int seq = 1;
		if(CommUtil.isNull(taxSeq)){
			taxSeq = "W".concat(brchno).concat(trandt).concat(CommUtil.lpad(String.valueOf(seq), 3, "0"));
			bizlog.debug("税率套号:[%s]", taxSeq);
		}else{
			
			bizlog.debug("税率套号:[%s]", taxSeq);
			int temp = Integer.valueOf(taxSeq.substring(taxSeq.length() - 3, taxSeq.length()));
			bizlog.debug("税率套号后三位:[%s]", temp);
			seq = temp+1;
			taxSeq = "W".concat(brchno).concat(trandt).concat(CommUtil.lpad(String.valueOf(seq), 3, "0"));
			bizlog.debug("税率套号:[%s]", taxSeq);
		}
		return taxSeq;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月18日-下午4:24:37</li>
	 *         <li>功能说明：税率维护</li>
	 *         </p>
	 * @param modifyIn
	 * @return
	 */
	public static void mntTaxRate(Options<fap_tax_rate> modifyIn) {
		
		bizlog.method(" FaTaxRateMnt.mntTaxRate begin >>>>>>>>>>>>>>>>");
		
		// 集合非空校验
		if(modifyIn.isEmpty()) {
			throw GlError.GL.E0219();
		}
		
		// 获取公共运行变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();

		// 遍历逐个处理
		for (fap_tax_rate rateTmp : modifyIn) {
			
			// 基本非空校验
			FaTaxRate.checkRateNull(rateTmp);	
			
			// 机构校验
			FaLoanAccountingEvent.existsFapSysDefine(runEnvs.getInpucd());
			
			// 查询记录是否存在
			fap_tax_rate rate = Fap_tax_rateDao.selectOne_odb1(rateTmp.getBranch_code(), rateTmp.getBusiness_code(), 
					rateTmp.getCal_tax_method_code(), rateTmp.getEffect_date(), CommTools.prcRunEnvs().getCorpno(), false);
			if(CommUtil.isNull(rate)) {
				throw GlError.GL.E0225(rateTmp.getBranch_code(),rateTmp.getBusiness_code(), 
						rateTmp.getCal_tax_method_code(), rateTmp.getEffect_date());
			}
			
			// 数据准确性校验
			Long recdver = rate.getRecdver();
			Long recdver2 = rateTmp.getRecdver();
			System.out.println(recdver != recdver2);
			System.out.println( CommUtil.compare(recdver, recdver2)  !=0 );
			if(!CommUtil.equals(rate.getBusiness_name(), rateTmp.getBusiness_name())
				|| !CommUtil.equals(rate.getCal_tax_method_name(), rateTmp.getCal_tax_method_name())
				|| CommUtil.compare(rate.getTax_rate(), rateTmp.getTax_rate()) != 0
				|| CommUtil.compare(recdver, recdver2)  !=0 ){
				throw GlError.GL.E0226();
			}
			
			// 生效记录只能做停用（修改失效日期）
			if (CommUtil.compare(rate.getInvalid_date(), runEnvs.getTrandt()) <= 0) {
				throw GlError.GL.E0227();
			}
			
			
			// 查询已生效记录
			/*List<fap_tax_rate> rateList = FaLoanAccountingDao.lstFapTaxRate(runEnvs.getCorpno(), rateTmp.getBranch_code(), 
					rateTmp.getBusiness_code(), rateTmp.getCal_tax_method_code(), null, null, E_VALID_STATUS.VALID, false);*/
				
			// 修改当前记录的失效日期
			rate.setRecdver(rate.getRecdver() + 1);             // 记录版本号
			rate.setInvalid_date(rateTmp.getInvalid_date());    // 失效日期
			rate.setLast_tran_user(runEnvs.getTranus());		// 修改柜员	
			rate.setLast_auth_user(runEnvs.getCkbsus());		// 修改复核柜员
			Fap_tax_rateDao.updateOne_odb1(rate);
			
		}
		
		bizlog.method(" FaTaxRateMnt.mntTaxRate end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年9月18日-下午4:24:55</li>
	 *         <li>功能说明：税率删除</li>
	 *         </p>
	 * @param input
	 * @return
	 */
	public static void delTaxRate(cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.delTaxRate.Input input) {
		
		bizlog.method(" FaTaxRateMnt.delTaxRate begin >>>>>>>>>>>>>>>>");
		
		// 非空校验
		FaTaxRate.checkBaseNull(input);
		
		// 记录校验
		fap_tax_rate rate = Fap_tax_rateDao.selectOne_odb1(input.getBranch_code(), input.getBusiness_code(), 
				input.getCal_tax_method_code(), input.getEffect_date(), CommTools.prcRunEnvs().getCorpno(), false);
		if(CommUtil.isNull(rate)) {
			throw GlError.GL.E0225(input.getBranch_code(), input.getBusiness_code(), 
					input.getCal_tax_method_code(), input.getEffect_date());
		}
		
		Fap_tax_rateDao.deleteOne_odb1(input.getBranch_code(), input.getBusiness_code(), 
				input.getCal_tax_method_code(), input.getEffect_date(), CommTools.prcRunEnvs().getCorpno());
		
		bizlog.method(" FaTaxRateMnt.delTaxRate end <<<<<<<<<<<<<<<<");
		
	}

	/**
	 * 
	 * @Author L
	 *         <p>
	 *         <li>2020年9月18日-下午4:25:10</li>
	 *         <li>功能说明：查询税率结果集</li>
	 *         </p>
	 * @param input
	 * @param output
	 */
	public static void queryTaxRateList(cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.queryTaxRateList.Input input,
			cn.sunline.ltts.busi.fa.servicetype.SrvFaTaxRate.queryTaxRateList.Output output) {
		
		bizlog.method(" FaTaxRateMnt.queryTaxRateList begin >>>>>>>>>>>>>>>>");
		
		//获取公共运行变量
	    RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
	   
	    //获取符合条件的所有明细
	    long pageno = runEnvs.getPageno();
	    long pgsize = runEnvs.getPgsize();
	    Page<fap_tax_rate> rateList = FaLoanAccountingDao.lstFapTaxRate(runEnvs.getCorpno(), input.getBranch_code(), 
	    		input.getBusiness_code(), input.getCal_tax_method_code(), input.getEffect_date(),
	    		input.getInvalid_date(), input.getValid_status(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
	    
	    bizlog.debug("**********[%s]", rateList);
	    
	    runEnvs.setCounts(rateList.getRecordCount());
	    
	    //设置输出参数
	    output.setQueryOut(new DefaultOptions<>(rateList.getRecords()));
		
		bizlog.method(" FaTaxRateMnt.queryTaxRateList end <<<<<<<<<<<<<<<<");
	}
	
}
