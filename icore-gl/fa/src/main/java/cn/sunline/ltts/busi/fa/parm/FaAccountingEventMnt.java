package cn.sunline.ltts.busi.fa.parm;
import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.audit.ApDataAudit;
import cn.sunline.ltts.busi.aplt.tools.ApKnpPara;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_event_parmDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_accounting_subjectDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_event_parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_accounting_subject;
import cn.sunline.ltts.busi.fa.type.ComFaParm.FaAccountingEventInfo;
import cn.sunline.ltts.busi.sys.dict.BaseDict;
import cn.sunline.ltts.busi.sys.errors.ApPubErr;
import cn.sunline.ltts.busi.sys.errors.GlError;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.sys.dict.GlDict;
public class FaAccountingEventMnt {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaAccountingEventMnt.class);
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月15日-下午5:13:21</li>
	 *         <li>功能说明：增加会计事件核算定义</li>
	 *         </p>
	 * @param EventInfo 会计事件核算定义复合类型
	 */
	public static void addAccountingEvent( FaAccountingEventInfo  eventInfo ) {
		
		//检查不能为空的值
		FaAccountingEvent.checkNull(eventInfo);
		//检查送上的数据对应记录是否存在
		if( FaAccountingEvent.checkExists(eventInfo.getSys_no(), eventInfo.getAccounting_alias(), eventInfo.getBal_attributes()) ){
			throw ApPubErr.APPUB.E0019(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname(), eventInfo.getSys_no()+" "+eventInfo.getAccounting_alias()+" "+eventInfo.getBal_attributes());
		}
		
		//账户序号检查
		String defaultSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1();//基准账户默认账户序号
		if(CommUtil.equals(defaultSeq, eventInfo.getAcct_seq())){
		    throw GlError.GL.E0117(eventInfo.getAcct_seq(), defaultSeq);
		}
		
		//检查合法性
		FaAccountingEvent.checkValidity(eventInfo.getGl_code(),eventInfo.getOffset_gl_code(),eventInfo.getSys_no(), eventInfo.getAccounting_subject(), eventInfo.getBal_attributes());
		
		fap_accounting_event_parm info = SysUtil.getInstance(fap_accounting_event_parm.class);
		
		info.setSys_no(eventInfo.getSys_no());  //系统编号
		info.setAccounting_subject(eventInfo.getAccounting_subject());  //会计主体
		info.setAccounting_alias(eventInfo.getAccounting_alias());  //核算别名
		info.setRemark(eventInfo.getRemark());  //备注
		info.setBal_attributes(eventInfo.getBal_attributes());  //余额属性
		info.setGl_code(eventInfo.getGl_code());  //科目号
		info.setOffset_gl_code(eventInfo.getOffset_gl_code());  //对方科目
		info.setAcct_seq(eventInfo.getAcct_seq());
		info.setRecdver(1l);//版本号默认为1
		
		Fap_accounting_event_parmDao.insert(info);
		
		ApDataAudit.regLogOnInsertParameter(info);
		bizlog.method(" addAccountingEvent end >>>>>>>>>>>>>>>>");
		
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月15日-下午5:15:49</li>
	 *         <li>功能说明：维护会计事件核算定义</li>
	 *         </p>
	 * @param EventInfo 会计事件核算定义复合类型
	 */
	public static void mntAccountingEvent( FaAccountingEventInfo  eventInfo ) {
		//TODO
		
		//找出待维护的数据
		if( !FaAccountingEvent.checkExists(eventInfo.getSys_no(), eventInfo.getAccounting_alias(), eventInfo.getBal_attributes()) ){
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname(),
					GlDict.A.sys_no.getId()	, eventInfo.getSys_no(),
					GlDict.A.accounting_alias.getId(), eventInfo.getAccounting_alias(), 
					GlDict.A.bal_attributes.getId(), eventInfo.getBal_attributes());
		}
		if(CommUtil.isNull(eventInfo.getAccounting_subject())) {
			throw ApPubErr.APPUB.E0001(GlDict.A.accounting_subject.getId(), GlDict.A.accounting_subject.getLongName());
		}
		
		// 校验科目号,对方科目号是否存在
		FaAccountingEvent.checkGlCodeExists(eventInfo.getGl_code());
		FaAccountingEvent.checkGlCodeExists(eventInfo.getOffset_gl_code());
		
		//账户序号检查
        String defaultSeq = ApKnpPara.getKnpPara("ACCT_SEQ", "BASE_ACCOUNT").getPmval1();//基准账户默认账户序号
        if(CommUtil.equals(defaultSeq, eventInfo.getAcct_seq())){
            throw GlError.GL.E0117(eventInfo.getAcct_seq(), defaultSeq);
        }
			
		fap_accounting_event_parm oldInfo = Fap_accounting_event_parmDao.selectOne_odb1(eventInfo.getSys_no(), eventInfo.getAccounting_alias(), eventInfo.getBal_attributes(),false);
		//克隆
		fap_accounting_event_parm mntInfo = CommTools.clone(fap_accounting_event_parm.class, oldInfo);
		
		mntInfo.setSys_no(eventInfo.getSys_no());  //系统编号
		mntInfo.setAccounting_subject(eventInfo.getAccounting_subject());  //会计主体
		mntInfo.setAccounting_alias(eventInfo.getAccounting_alias());  //核算别名
		mntInfo.setRemark(eventInfo.getRemark());  //备注
		mntInfo.setBal_attributes(eventInfo.getBal_attributes());  //余额属性
		mntInfo.setGl_code(eventInfo.getGl_code());  //科目号
		mntInfo.setOffset_gl_code(eventInfo.getOffset_gl_code());  //对方科目
		mntInfo.setCorpno(eventInfo.getCorpno());  //法人代码
        mntInfo.setAcct_seq(eventInfo.getAcct_seq());
		
		//对比数据版本
		if( CommUtil.compare(oldInfo.getRecdver(), eventInfo.getRecdver())  !=0 ){
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname());
		}
		
		// 登记审计
		
		if ( ApDataAudit.regLogOnUpdateParameter(oldInfo, mntInfo) == 0) {
			throw ApPubErr.APPUB.E0023(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname());
		}
		
		mntInfo.setRecdver(eventInfo.getRecdver()+1);
		Fap_accounting_event_parmDao.updateOne_odb1(mntInfo);
		
		
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月15日-下午5:24:19</li>
	 *         <li>功能说明：根据系统编号+核算别名+余额属性删除一条记录 </li>
	 *         </p>
	 * @param sysNo 系统编号
	 * @param accountingAlias 核算别名
	 * @param balProp 余额属性
	 */
	public static void delAccountingEvent( String sysNo, String accountingAlias, String balProp ,Long recdver ) {
		//TODO
		//找出待删除的数据,不存在报错
		if( !FaAccountingEvent.checkExists(sysNo, accountingAlias, balProp) ){
			throw ApPubErr.APPUB.E0025(OdbFactory.getTable(fap_accounting_event_parm.class).getLongname(),
					GlDict.A.sys_no.getId()	, sysNo,
					GlDict.A.accounting_alias.getId(), accountingAlias, 
					GlDict.A.bal_attributes.getId(), balProp);
		}
			
		fap_accounting_event_parm deletEvent = Fap_accounting_event_parmDao.selectOne_odb1(sysNo, accountingAlias, balProp, false);
		
		// 版本号非空校验
		CommTools.fieldNotNull(recdver, BaseDict.Comm.recdver.getId(), BaseDict.Comm.recdver.getLongName());

		// 对比数据版本
		if (CommUtil.compare(recdver, deletEvent.getRecdver()) != 0) {
			throw ApPubErr.APPUB.E0018(OdbFactory.getTable(fap_accounting_event_parm.class).getName());
		}

		// 删除数据
		Fap_accounting_event_parmDao.deleteOne_odb1(sysNo, accountingAlias, balProp);

		// 登记审计
		ApDataAudit.regLogOnDeleteParameter(deletEvent);
	}
}
