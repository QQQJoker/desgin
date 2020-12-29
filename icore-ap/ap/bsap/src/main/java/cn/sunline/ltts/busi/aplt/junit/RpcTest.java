/*package cn.sunline.ltts.busi.aplt.junit;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import cn.sunline.edsp.midware.base.model.URL;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.midware.rpc.core.AccessId;
import cn.sunline.edsp.midware.rpc.core.model.RPCResult;
import cn.sunline.edsp.midware.rpc.packet.sdk.RPCConsumer;
import cn.sunline.edsp.midware.rpc.registry.RegistryConfig;
import cn.sunline.ltts.busi.aplt.tools.ApAPI;

import com.alibaba.fastjson.JSON;

*//**
 * RPC 测试远程交易
 * 
 * 例子：
 * bsap.rpc.qrdptyRpcTest
 * 
 * @author lizhs
 * @date 2017年6月22日
 *//*
@ApAPI
public class RpcTest extends AbstractTest {
    protected String serviceId;
    protected String scenarioId;
    protected String version;
    protected String dcn;
    protected String registryURL;
    private RegistryConfig registryConfig;

    @Before
    public void initRegistryConfig() {
        if (registryConfig != null)
            return;
        registryConfig = new RegistryConfig(URL.valueOf(registryURL));
    }

    @AfterClass
    public static void afterClass() {
        System.setProperty("pluginMode", "");
    }

   

    public ResponseData _call() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("call_timeout", "10000"); // 服务参数
        //params.put(RPCConstant.ALLOW_REPETITION_INIT_KEY, "true"); 2.6.4 平台升级错误
        params.put(RPCParamType.channelpool_max.getName(), "1");
        params.put(RPCParamType.service_appId.getName(), "junit");

        *//*** 初始化调用实例类 *//*
        
        RPCConsumer.init(registryConfig, list);
        //        Dest dest = getAccessId();

        AccessId accessId = new AccessId(serviceId, scenarioId, version, dcn);
        ReferenceConfigSun referenceConfig = new ReferenceConfigSun(accessId.getServiceId(), scenarioId, accessId.getVersion(), dcn, params);
        RPCResult result = new RPCResult();
        //      result.setBizHeader(header);
        String reqPkgStr = JSON.toJSONString(requestPkgMap, true);
        printlnTmp("请求报文", reqPkgStr);
        result.setData(reqPkgStr);
        RPCConsumer.init(registryConfig, referenceConfig);
        try {
            RPCResult resultRes = RPCConsumer.invoke(registryConfig.getAddress(), accessId, result);
            //          Object responseObj = RPCConsumer.invoke(accessId, getSendContent("demo01.json"), params);
            Map retMap = JSON.parseObject(resultRes.getData().toString());
            ResponseData ret = new ResponseData("", retMap);
            printlnTmp("返回报文", JSON.toJSONString(retMap, true));
            return ret;
        } catch (Throwable e) {
            e.printStackTrace();
            throw ExceptionUtil.wrapThrow("RPC发送失败", e);
        } finally {
        }
    }

    //    public abstract Dest getAccessId();

    //public abstract String getRegistryURL();

    @Override
    void setUp_Before() {
        //只加载基础设施
        System.setProperty("pluginMode", "*.plugin2.xml");
    }
}
*/