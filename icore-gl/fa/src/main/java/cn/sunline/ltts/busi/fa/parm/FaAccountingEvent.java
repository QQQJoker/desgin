package cn.sunline.ltts.busi.fa.parm;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.fa.namedsql.FaParmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_event_parmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs.RunEnvs;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.ltts.sys.dict.GlDict;




public class FaAccountingEvent {
	
	private static final BizLog bizlog = BizLogUtil.getBizLog(FaAccountingEvent.class);

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-下午4:15:53</li>
	 *         <li>功能说明：检查不允许为空的字段</li>
	 *         </p>
	 * @param eventInfo
	 *            会计事件核算复合类型
	 */
	public static void checkNull(FaAccountingEventInfo eventInfo) {
		// 检查不能为空的值
		CommTools.fieldNotNull(eventInfo.getSys_no(), GlDict.A.sys_no.getId(), GlDict.A.sys_no.getLongName());
		CommTools.fieldNotNull(eventInfo.getAccounting_subject(), GlDict.A.accounting_subject.getId(), GlDict.A.accounting_subject.getLongName());
		CommTools.fieldNotNull(eventInfo.getAccounting_alias(), GlDict.A.accounting_alias.getId(), GlDict.A.accounting_alias.getLongName());
		CommTools.fieldNotNull(eventInfo.getBal_attributes(), GlDict.A.bal_attributes.getId(), GlDict.A.bal_attributes.getLongName());
		CommTools.fieldNotNull(eventInfo.getGl_code(), GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-下午4:20:09</li>
	 *         <li>功能说明：检查指定的记录是否存在</li>
	 *         </p>
	 * @param sysNo
	 *            系统编号
	 * @return
	 */
	public static boolean checkExists(String sysNo, String accountingAlias, String balProp) {
		fap_accounting_event_parm info = Fap_accounting_event_parmDao.selectOne_odb1(sysNo, accountingAlias, balProp, false);

		// 没有记录返回false，存在返回true。
		return (info == null) ? false : true;
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-下午4:22:45</li>
	 *         <li>功能说明: 检查送入字段的合法性</li>
	 *         </p>
	 * @param eventInfo
	 *            会计事件核算定义复合类型
	 */
	public static void checkValidity(String glCode,String offsetGlCode, String sysNo , String accountingSubject , String balProperty ) {
		
		fap_accounting_subject fap = Fap_accounting_subjectDao.selectOne_odb1(glCode, false);
		if(CommUtil.isNull(fap)){
			throw GlError.GL.E0201(glCode);
		}
		fap_sys_define fapdef = Fap_sys_defineDao.selectOne_odb1(sysNo, false);
		if(CommUtil.isNull(fapdef)){
			throw GlError.GL.E0200(sysNo);
		}
		if(CommUtil.isNotNull(offsetGlCode)) {
			fap_accounting_subject fap2 = Fap_accounting_subjectDao.selectOne_odb1(offsetGlCode, false);
			if(CommUtil.isNull(fap2)){
				throw GlError.GL.E0201(offsetGlCode);
			}
		}
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月15日-下午4:14:48</li>
	 *         <li>功能说明：根据系统编号+核算别名+余额属性找到一条记录</li>
	 *         </p>
	 * @param sys_no 系统编号
	 * @param accounting_alias 核算别名
	 * @param bal_property 余额属性
	 * @return 表结构的复合类型
	 */
	public static FaAccountingEventInfo getAccountingEventInfo( String sys_no, String accounting_alias ,String bal_property  ) {
		
		//系统编号必须在下拉列表中存在
		//ApDropList.exists(FaConst.SYS_NO, sys_no , true);

		//余额属性必须在下拉字典中
		//ApDropList.exists(FaConst.BAL_PROPERTY, bal_property, true);
		
		FaAccountingEventInfo info = SysUtil.getInstance(FaAccountingEventInfo.class);
		
		if( !checkExists(sys_no, accounting_alias, bal_property)  )
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname(), 
					GlDict.A.sys_no.getId(),sys_no,
					GlDict.A.accounting_alias.getId(), accounting_alias, 
					GlDict.A.bal_attributes.getId(), bal_property );
		
		fap_accounting_event_parm table = Fap_accounting_event_parmDao.selectOne_odb1(sys_no, accounting_alias, bal_property, false);
		
		info.setSys_no(table.getSys_no());  //系统编号
		info.setAccounting_subject(table.getAccounting_subject());  //会计主体
		info.setAccounting_alias(table.getAccounting_alias());  //核算别名
		info.setRemark(table.getRemark());  //备注
		info.setBal_attributes(table.getBal_attributes());  //余额属性
		info.setGl_code(table.getGl_code());  //科目号
		info.setOffset_gl_code(table.getOffset_gl_code());  //对方科目
		info.setRecdver(table.getRecdver());//数据版本
		
		return info;
		
	}

	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-下午4:26:39</li>
	 *         <li>功能说明：查询会计事件定义明细列表</li>
	 *         </p>
	 * @param eventInfo
	 *            会计事件核算定义复合类型
	 */
	public static Options<FaAccountingEventInfo> queryEvent( FaAccountingEventInfo  eventInfo ) {
		
		// 获取公共变量
		RunEnvs runEnvs = CommToolsAplt.prcRunEnvs();
		String orgId = runEnvs.getCorpno();
		long pageno = runEnvs.getPageno();
		long pgsize = runEnvs.getPgsize();
		
		Page<FaAccountingEventInfo> page = FaParmDao.lstAcctingEvent(orgId, eventInfo.getSys_no(), eventInfo.getAccounting_subject(),
				eventInfo.getAccounting_alias(), eventInfo.getRemark(), eventInfo.getBal_attributes(), eventInfo.getGl_code(),
				eventInfo.getOffset_gl_code(), (pageno - 1) * pgsize, pgsize, runEnvs.getCounts(), false);
		
		runEnvs.setCounts(page.getRecordCount());
		Options<FaAccountingEventInfo> info = new DefaultOptions<FaAccountingEventInfo>();
		info.setValues(page.getRecords());
		
		return info;
	}

	/**
	 * 
	 * @Author 
	 *         <p>
	 *         <li>2020年10月16日-下午2:22:26</li>
	 *         <li>功能说明：检查科目号合法</li>
	 *         </p>
	 * @param gl_code
	 */
	public static void checkGlCodeExists(String gl_code) {
		
		bizlog.method(" FaAccountingEvent.checkGlCodeExists begin >>>>>>>>>>>>>>>>");
		
		if(CommUtil.isNotNull(gl_code)) {
			fap_accounting_subject fap = Fap_accounting_subjectDao.selectOne_odb1(gl_code, false);
			if(!CommUtil.isNotNull(fap)){
				throw GlError.GL.E0201(gl_code);
			}
		} else {
			throw ApPubErr.APPUB.E0001(GlDict.A.gl_code.getId(), GlDict.A.gl_code.getLongName());
		}
		
		bizlog.method(" FaAccountingEvent.checkGlCodeExists end <<<<<<<<<<<<<<<<");
		
	}
	
	
	
}
