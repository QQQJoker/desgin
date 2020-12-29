package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tables.SysDbTable.ApbAcctRoutDao;
import cn.sunline.ltts.busi.aplt.tools.ApAcctRoutTools;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.iobus.servicetype.in.IoInQuery;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpParaDao;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_ACCTTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_ACCTROUTTYPE;
 /**
  * 账号信息查询实现
  * 账号信息查询实现
  *
  */
@cn.sunline.adp.core.annotation.Generated
@cn.sunline.adp.metadata.model.annotation.ConfigType(value="IoApAccountImpl", longname="账号信息查询实现", type=cn.sunline.adp.metadata.model.annotation.ConfigType.Type.SERVICE)
public class IoApAccountImpl implements cn.sunline.ltts.busi.iobus.servicetype.IoApAccount{
	/**
	 * 判断系统内账号类型，0为客户账号，1为内部账号
	 * 
	 * @param account 产生序列号key
	 * @return 账号类型:0为客户账号，1为内部账号
	 */
	public void queryAccountType(String acctno, final cn.sunline.ltts.busi.iobus.servicetype.IoApAccount.queryAccountType.Output output){
		
		//查询参数配置表，获取客户账号前六位编码：623540
		/*KnpPara para1 = CommTools.getInstance(KnpPara.class);
    	para1 = CommTools.KnpParaQryByCorpno("ACCTTP", "custtp", "%", "%",true);
		String custtp = para1.getPmval1(); //623540
*/		
		if(CommUtil.isNull(acctno)){
			//为空判断
			throw Aplt.E0000("卡号不能为空");
		}
		// 长度十九位，且前六位为623540，判断为客户账号
	//	if(acctno.length() == 19 && CommUtil.equals(acctno.substring(0, 6), custtp)){
			//账号符合客户账号规范，查寻电子账号表中有无该记录
//			if(CommUtil.isNull(CommTools.getInstance(IoCaSevQryTableInfo.class).kna_acdc_selKnaAcdcByCardNo(acctno, false))){
//				throw Aplt.E0000("电子账号表中未找到该账号记录");
//			}
		if(ApAcctRoutTools.getRouteType(acctno) == E_ACCTROUTTYPE.CARD){
			output.setAccttp(E_ACCTTP.DP);
		}else if(ApAcctRoutTools.getRouteType(acctno) == E_ACCTROUTTYPE.INSIDE ){
			
			//账号符合内部户账号规范，查寻内部户账号表中有无该记录
			if(CommUtil.isNull(CommTools.getInstance(IoInQuery.class).InacInfoQuery(acctno))){
				throw Aplt.E0000("内部户账号表中未找到该账号记录");
			}
			output.setAccttp(E_ACCTTP.IN);
			
		}else{
			
			//不符合任何种类账号规则，报错
			throw Aplt.E0000("系统内账号类型只支持卡账号和内部账号");
		}

	}
}

