
package cn.sunline.ltts.busi.aptran.serviceimpl;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.clwj.zdbank.cds.util.ShardingUtil;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTran;
import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnsTranDao;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.gns.api.GnsKey;

/**
  * 测试服务实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="MyImpl", longname="测试服务实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class MyImpl implements cn.sunline.ltts.busi.aptran.servicetype.My{
 /**
  * JstSayHello
  *
  */
	public void helloWorld(String custno, String acctno, String tellno, final cn.sunline.ltts.busi.aptran.servicetype.My.helloWorld.Output output){
		
	}
	public String queryParms(String parmcd){
		
		KnpPara para = KnpParaDao.selectOne_odb1(parmcd, "joker", "%", "%", "985", false);
		
		GnsKey  gsnKey = SysUtil.getInstance(GnsKey.class);
		
		gsnKey.setGnschn("000");
		gsnKey.setGnskey("8950001");
		gsnKey.setGnsopt("P");
		gsnKey.setGnssts("1");
		gsnKey.setGnstyp("AC");
		gsnKey.setGnsval("520");
		
		ShardingUtil.addGnsInfoToContext(gsnKey);
		
		KnsTran tran = KnsTranDao.selectOne_odb1("20170822010R0098500001001", "20170822", false);
		
		return para.getPmval1();
		
	}
}

