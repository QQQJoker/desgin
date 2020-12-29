package cn.sunline.ltts.busi.aplt.junit;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.Before;

import com.alibaba.fastjson.JSON;

import cn.sunline.adp.cedar.base.engine.HeaderDataConstants;
import cn.sunline.adp.cedar.base.engine.RequestData;
import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.adp.cedar.base.engine.data.DataArea;
import cn.sunline.adp.cedar.base.engine.datamapping.EngineContext;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.adp.cedar.engine.online.InServiceController;
import cn.sunline.adp.cedar.engine.online.OnlineEngineTemplate;
import cn.sunline.adp.core.profile.ProfileSwitcher;
import cn.sunline.adp.metadata.base.config.ConfigConstants;
import cn.sunline.adp.metadata.base.util.EdspCoreBeanUtil;
import cn.sunline.edsp.base.util.exception.ExceptionUtil;
import cn.sunline.edsp.microcore.boot.Bootstrap;
import cn.sunline.ltts.busi.aplt.tools.ApltEngineContext;
import cn.sunline.ltts.busi.aplt.tools.CommTools;

/**
 * 共测试：服务、ODB、Nsql 等
 * 
 * 
 * 目的：支持测试案例重复跑
 * 
 * IDE跑问题：
 * 1、流水重复，需要每次跑调整（通过代码动态设置）
 * 2、上送报文，有些数据必须在表中存在
 * 
 * @author lizhs
 * @date 2017年6月21日
 */

public abstract class AbstractTest {
    private static boolean initInJvm = false;
    public static  boolean ideRun=false;
    /**
     * 请求报文Map <br/>
     */
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

    public void initTxnPkg(String pkgFile) {
        String content = "";
        try {
            InputStream is = AbstractTest.class.getClassLoader().getResourceAsStream(pkgFile);
            try {
                content = IOUtils.toString(is);
                is.close();
            } catch (IOException e) {
                throw ExceptionUtil.wrapThrow("读取报文模板内容失败", e);
            }

            if (content.trim().startsWith("{")) { // JSON模版文件
                this.requestPkgMap = JSON.parseObject(content);
            } else { // 模版文件未知类型
                throw ExceptionUtil.wrapThrow("模版文件类型未知![%s]", content);
            }
        } catch (Exception e) {
            throw ExceptionUtil.wrapThrow("请求报文模版文件解析失败！[%s]", content);
        }
    }

    public void setTxnPkgUseExp(String exp, String value) {
        try {
            // 判断并解析值的类型
            Object valueObj = null;
            try {
                if (value.startsWith("{") || value.startsWith("[")) {
                    valueObj = JSON.parse(value);
                } else {
                    valueObj = value;
                }
            } catch (Exception e) {
                throw ExceptionUtil.wrapThrow(e);
            }
            // 重置请求报文指定字段值
            this.setValue(this.requestPkgMap, exp, valueObj);
        } catch (Exception e) {
            throw ExceptionUtil.wrapThrow(e);
        }
    }

    private void setValue(Map<String, Object> map, String exp, Object valueObj) {
        OgnlUtil.setValue(map, exp, valueObj);
    }

    public final ResponseData call() {
        ideRun=true;
        RequestData currentData = null;
        if (!EngineContext.getRequestData().getData().isEmpty())
            currentData = EngineContext.getRequestData();
        try {
            return _call();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            ideRun=false;
            if (currentData != null)
                EngineContext.push(new EngineContext(currentData, EngineContext.getEngineRuntimeContext()));
        }
    }

    abstract ResponseData _call();

    private void initCommPkg() {
        String inpus = CommTools.getSequence("junit.inpus", 10);
        setTxnPkgUseExp("comm_req.inpusq", "junit_" + inpus);
        setTxnPkgUseExp("comm_req.pckgsq", "junit_" + inpus);
        setTxnPkgUseExp("comm_req.transq", "junit_" + inpus);

        setTxnPkgUseExp("comm_req.inpucd", "01");
        setTxnPkgUseExp("comm_req.tranus", "9852001");
        setTxnPkgUseExp("sys.prcscd", "qrchdf");
    }

    /**
     * 测试 DAO或服务时使用
     */
    public void initEnv() {
        initCommPkg();

        String sendPkg = JSON.toJSONString(requestPkgMap, true);
        printlnTmp("初始化", sendPkg);

        DataArea reqDataArea = DataArea.buildWithData(requestPkgMap);
        OnlineEngineTemplate engineTemplate = new OnlineEngineTemplate();
        InServiceController serviceController = new InServiceController(reqDataArea.getSystem().getString(HeaderDataConstants.NAME_PRCSCD));
        RequestData requestData = new RequestData(null, reqDataArea);
        //TODO 初始化beforeRunENV
        ApltEngineContext.getTxnTempObjMap().clear();
    }

    protected void printlnTmp(String name, String content) {
        System.out.println(">>>>>>>>>  " + name + "开始      >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
        System.out.println(content);
        System.out.println(">>>>>>>>>  " + name + "结束      >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
    }

    @Before
    public void setUp() {
        setUp_Before();

        if (initInJvm)
            return;

        System.out.println("=============[启动]开始===============");
        try {
            startInJvm();
            initInJvm = true;
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        } finally {
            System.out.println("=============[启动]结束===============");
        }
    }

    void setUp_Before() {
    };

    private void startInJvm() {
        try {
            initConf();
            Bootstrap.main(null);
            ProfileSwitcher.get().useDebugListener = true;

            Runtime.getRuntime().addShutdownHook(shutdownHook);
            initInJvm = true;
            System.out.println("start...");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    static Thread shutdownHook = new Thread("junit-shutdown") {
        public void run() {
//            Bootstrap.stop();  20200429 xieqq平台屏蔽了调用stop方法
            System.out.println("end....");
        }
    };

    private static void initConf() {
        //System.getProperty("ltts.vmid");
        System.setProperty("ltts.vmid","vmid-junit");
        //单元侧默认采用中文方式
        if (System.getProperty(ConfigConstants.CONFIG_Language) == null)
            System.setProperty(ConfigConstants.CONFIG_Language, "cn");

        if (System.getProperty(ConfigConstants.SETTING_FILE) == null)
            System.setProperty(ConfigConstants.SETTING_FILE, "setting.dev.properties");

        //2015-07-25 如果参数中有送则用上送的配置，如果没有就使用ltts-core-plugin中定义的默认的开发日志配置文件
        if (System.getProperty("log4j.configurationFile") == null)
            System.setProperty("log4j.configurationFile", "ltts_log_dev.xml");
    }

    public void commit()
    {
        EdspCoreBeanUtil.getDBConnectionManager().commit();
    }

    public void rollback() {
        EdspCoreBeanUtil.getDBConnectionManager().rollback();
    }
    


    /**
     * 测试过程中方便获取唯一流水
     * <p>Title:genSeq </p>
     * <p>Description:	</p>
     * @author 91656
     * @date   2017年6月23日 
     * @return
     */
	public String genSeq() {
		// TODO Auto-generated method stub
		return "junit_"+SysUtil.nextValue("junit_seq");
	}


}
