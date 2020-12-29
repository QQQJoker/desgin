package cn.sunline.clwj.security.test;


import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.busi.sdk.component.BaseComp;
import cn.sunline.adp.cedar.engine.online.AdpUnitTest;
import cn.sunline.clwj.security.component.SAFSecurityComponent;

public class SAFSecurityTest extends AdpUnitTest{
	
	
	@Override
	public void before() {
		super.before();
        DataArea reqDataArea = DataArea.buildWithData(requestPkgMap);
//        LttsReqContext debugctx = LttsReqContext.createLttsReqContext();
//        RequestData requestData = new RequestData("", reqDataArea, debugctx);
//        EngineContext.push(requestData);
        //EngineExtensionManager.get().getTransactionProcessCallback().beforeBizEnv(requestData.getReuqestDataArea());
        
        EngineContext ctx = new EngineContext(new RequestData("", reqDataArea), EngineContext.getEngineRuntimeContext());
        EngineContext.push(ctx);
//        ApltEngineContext.getTxnTempObjMap().clear();
	}

	@Test
	public void testSecurity() {
		BaseComp.Security security = SysUtil.getInstance(BaseComp.Security.class,SAFSecurityComponent.SAFSecurity);
		security.decryptPin("", "");
	}
	
	
    protected Map<String, Object> requestPkgMap = new HashMap() {
        public Object get(Object key) {
            Object ret = super.get(key);
            if (ret == null && ("sys".equals(key) || "comm_req".equals(key) || "input".equals(key))) {
                ret = new HashMap<>();
                put(key, ret);
                return ret;
            }
            return ret;
        };
    };
}
