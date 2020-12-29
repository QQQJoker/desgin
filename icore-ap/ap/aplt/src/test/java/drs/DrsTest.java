//package drs;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import cn.sunline.edsp.component.rpc.common.constants.RPCParamType;
//import cn.sunline.edsp.microcore.Bootstrap;
//import cn.sunline.edsp.midware.drs.common.model.DCN;
//import cn.sunline.edsp.midware.drs.common.server.DRSFactoryManager;
//import cn.sunline.edsp.midware.drs.common.server.IDRSFactory;
//import cn.sunline.edsp.midware.drs.common.server.IDRSManage;
//import cn.sunline.edsp.midware.drs.common.server.IDRSRegistry;
//
///**
// * 
// * @author lizhs
// * @date 2017年5月31日
// * 
// * ********
// * @see http://atlassian.yfb.sunline.cn:8090/pages/viewpage.action?pageId=4031037
// */
//public class DrsTest {
//    //    @ClassRule
//    //    public static OnlineTest rule = new OnlineTest();
//
//    static IDRSFactory factory = null;
//
//    @BeforeClass
//    public static void setUp_Before() {
//        //只加载基础设施
//        System.setProperty("pluginMode", "*.plugin2.xml");
//        Bootstrap.start();
//        System.out.println("setUp_Before");
//
//        factory = DRSFactoryManager.getInstance().getFactory();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put(RPCParamType.timeout.getName(), "10000");
//        params.put(RPCParamType.service_appId.getName(), "junit");
//        //这里的配置参数，要根据服务端的配置来决定
//        //测试环境
//        factory.init("zookeeper://10.22.10.99:2181", "drs", "CS", "3.3", params);
//    }
//
//    @Test
//    public void test_getDCN() {
//        IDRSManage manage = factory.getManageService();
//        List<DCN> dcnList = manage.getDCN();
//        System.out.println("DCN数量："+dcnList.size());
//        for (DCN dcn : dcnList) {
//            System.out.println(dcn.getDcnID());
//        }
//        
//        List<DCN> dcnByType = manage.getDCNByType("A-dcn");
//        System.out.println("A-dcn"+dcnByType.size());
//          dcnByType = manage.getDCNByType("C-dcn");
//          System.out.println("C-dcn"+dcnByType.size());
//    }
//
//    @Test
//    public void test_create_DCN_R00() {
//        IDRSManage manage = factory.getManageService();
////        manage.createDCNByRDCN("R00");
//    }
//
//    @Test
//    public void test_create_ADCN_DM0() {
//        IDRSManage manage = factory.getManageService();
////        manage.createDCNByADCN("DM0");
//        System.out.println("OK");
//    }
//
//    @Test
//    public void test_create_DCN_R01() {
//        IDRSManage manage = factory.getManageService();
////        manage.createDCNByRDCN("R01");
//    }
//
//    @Test
//    public void _test_registryDCNByECIF() {
//        IDRSRegistry registry = factory.getRegistryService();
//        registry.registryDCNByECIF("1000000000000080", "R00");
//    }
//
//    @Test
//    public void test_registryDCNByECIF() {
//        IDRSRegistry registry = factory.getRegistryService();
//        //自动为ECIF信息选择一个合适的DCN节点，并创建ECIF信息和DCN的映射关系。
//        //其它类型的客户信息注册操作相同。
//        String dcn = registry.registryDCNByECIF(UUID.randomUUID().toString());
//        System.out.println(dcn);
//    }
//
//}
