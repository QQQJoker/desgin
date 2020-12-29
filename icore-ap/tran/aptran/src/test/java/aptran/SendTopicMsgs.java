package aptran;

import org.junit.Test;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.junit.OnlineTest;
import cn.sunline.ltts.busi.aplt.tools.AsyncMessageUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.bsap.type.ApMessageComplexType.SQLCType;
import cn.sunline.ltts.busi.sys.namedsql.KnpParaSqlDao;
import cn.sunline.ltts.busi.sys.parm.TrxBaseEnvs.MessageRealInfo;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_MSGOPT;
import cn.sunline.edsp.base.lang.Params;

public class SendTopicMsgs extends OnlineTest{

	/**
	 * TOPIC
	 * 发送多条消息
	 */
	@Test
	public void sendTopicMsgs(){
		initEnv();
		KnpPara para = SysUtil.getInstance(KnpPara.class);
		para.setParmcd("testSqlPara");
		para.setPmkey1("pmkey1");
		para.setPmval4("pmval4");
		para.setPmval5("pmval5");
		//KnpParaDao.insert(para);

		Params p = new Params();
		p.addAll(CommUtil.toMap(para));
		int cnt = KnpParaSqlDao.updKnpParaByParmcdAndPmkey1(CommTools.prcRunEnvs().getCorpno(), para.getParmcd(), para.getPmkey1(), para.getPmval4(),
				para.getPmval5());
		SQLCType sqlct = SysUtil.getInstance(SQLCType.class);
		sqlct.setEffcnt(cnt);
		sqlct.setSqlfid(KnpParaSqlDao.namedsql_updKnpParaByParmcdAndPmkey1);
		sqlct.setSqlpam(p);
		sqlct.setSqlstr(null);
		sqlct.setTabnam("knp_para");
		
		MessageRealInfo mri = SysUtil.getInstance(MessageRealInfo.class);
		mri.setMsgobj(sqlct);;
		mri.setMsgopt(E_MSGOPT.SUCESS);
		mri.setMsgtyp(SQLCType.class.getName());
		mri.setMtopic("test");
		AsyncMessageUtil.add(mri);
		
		MessageRealInfo mri2 = SysUtil.getInstance(MessageRealInfo.class);
		mri2.setMsgobj(sqlct);
		mri2.setMsgopt(E_MSGOPT.SUCESS);
		mri2.setMsgtyp(SQLCType.class.getName());
		mri2.setMtopic("test");
		AsyncMessageUtil.add(mri2);
		AsyncMessageUtil.publishOrSave(E_MSGOPT.SUCESS);
	}
}
