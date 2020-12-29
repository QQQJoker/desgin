package cn.sunline.ltts.busi.aplt.junit;

import java.util.Date;

import org.junit.Before;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
//import cn.sunline.ltts.plugin.online.facade.OnlineFacade;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.base.logging.LogConfigManager;
import cn.sunline.adp.cedar.base.pkg.PkgFactory;
import cn.sunline.adp.cedar.base.pkg.PkgMode;
import cn.sunline.adp.cedar.engine.online.InServiceController;
import cn.sunline.adp.cedar.engine.online.InServiceController.ServiceCategory;
import cn.sunline.adp.cedar.engine.online.OnlineEngineTemplate;
import cn.sunline.ltts.busi.aplt.tools.ApAPI;

/**
 * <pre>
 * 用于单元测试：服务、ODB、Nsql、联机交易 等
 * 
 * 说明：整个测试过程平台只会启动一次
 * 
 * 例子：
 * onl.urmainTest               测试交易的例子
 * servicetype.SrvPbBranchTest  测试服务的例子
 * comm.KubUserDAOTest          测试DAO的例子
 * </pre>
 * @author lizhs
 * @date 2017年6月21日
 */
@ApAPI
public class OnlineTest extends AbstractTest {

    @Override
    public ResponseData _call() {
        String sendPkg = JSON.toJSONString(requestPkgMap, true);
        printlnTmp("请求报文", sendPkg);

        EngineContext.clear();
        DataArea reqDataArea = DataArea.buildWithData(requestPkgMap);
        RequestData request = new RequestData("", reqDataArea);

        InServiceController serviceController;

        if ("s".equals(request.getRequestHeader().get("ServiceCategory"))) {
            serviceController = new InServiceController(request.getRequestHeader().getServiceCode(), null, ServiceCategory.S);
        } else {
            String flowTranId = request.getRequestHeader().getServiceCode();
            serviceController = new InServiceController(flowTranId);

        }
        serviceController.setInnerServiceCode("*");

        long start = System.currentTimeMillis();

        OnlineEngineTemplate engineTemplate = new OnlineEngineTemplate();

        ResponseData response = engineTemplate.process(new Date(), request, serviceController);

        String output = PkgFactory.get().getPkgWrapper().format(response.getBody(), null, PkgMode.response);

        long useTime = System.currentTimeMillis() - start;

        printlnTmp("响应报文", output);
        printlnTmp("响应耗时", useTime + "");
        
        return response;
    }

    @Before
    public void init() {
        LogConfigManager.get().setCurrentSystemType(LogConfigManager.SystemType.onl);
    }

}
