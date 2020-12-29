package prop;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxns;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpTxnsDao;
import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aplt.tools.PropUtil;
import cn.sunline.ltts.busi.sys.type.ApBaseEnumType.E_TRANTP;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_YES___;
import cn.sunline.adp.cedar.engine.online.AdpUnitTest;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.engine.data.DataArea;

public class PropUtilTest extends AdpUnitTest{
	
	
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
        ApltEngineContext.getTxnTempObjMap().clear();
	}

	@Test
	public void testCheckAndFixPropValue() {
		CommTools.prcRunEnvs().setCorpno("999");
		KnpTxns txns = KnpTxnsDao.selectOne_odb1("MyTran", false);
		KnpTxns txns2 = SysUtil.getInstance(KnpTxns.class);
		CommUtil.copyProperties(txns2, txns);
		txns2.setTranna(txns2.getTranna());
		txns2.setOvertm(2222L);
		txns2.setRedufg(E_YES___.NO);
		txns2.setTrantp(E_TRANTP.CXJY);
		KnpTxns result = PropUtil.checkAndFixPropValue(KnpTxns.class, "test", txns2, txns);
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
