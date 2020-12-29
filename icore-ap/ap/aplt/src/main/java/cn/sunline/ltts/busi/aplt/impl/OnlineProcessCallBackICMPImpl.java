package cn.sunline.ltts.busi.aplt.impl;

import cn.sunline.adp.metadata.base.dao.Operator;

public class OnlineProcessCallBackICMPImpl extends cn.sunline.ltts.busi.aplt.impl.DaoProcessCallBackImpl {

  
	public void beforeDaoProcess(Class<?> intfClass, Operator op,Object parameters) {
		 
		 
		super.beforeDaoProcess(intfClass, parameters);
	/*	//todo  增加aplt中的处理逻辑
		if (SysCommFieldTable1.kap_comm1.class.isAssignableFrom(intfClass)) {
			Map<String, Object> mapCommFld = CommUtil.toMap(parameters);
			mapCommFld.put("","");
		//	intfClass.
		} */
  

}
    
//    private   void  processResponsePkg(ResponseData response,Throwable cause){
//       
//    }

//    @Override
//    public void beforePkgFormat(ResponseData response) {
//        super.beforePkgFormat(response);
//    }
}
