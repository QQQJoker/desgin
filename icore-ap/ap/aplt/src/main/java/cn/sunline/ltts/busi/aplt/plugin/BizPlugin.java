package cn.sunline.ltts.busi.aplt.plugin;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcx;
import cn.sunline.ltts.busi.aplt.tables.SysParmTable.KnpSvcxDao;
import cn.sunline.ltts.busi.aplt.tools.ApConstants;
import cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent;
import cn.sunline.adp.cedar.base.boot.plugin.PluginSupport;

public class BizPlugin extends PluginSupport {
    //    static Properties errorModuleMap;

    @Override
    public boolean initPlugin() {
        //        try {
        //            errorModuleMap = PropertiesLoaderUtils.loadAllProperties("errormodule.map.properties");
        //        } catch (IOException e) {
        //            throw ExceptionUtil.wrapThrow("加载错误码映射文件失败",e);
        //        }
        return true;
    }

    //    public static  Properties getErrorModuleMap(){
    //        return errorModuleMap;
    //    }
    @Override
    public void startupPlugin() {
        //数据初始化
        initSysDataForDB();
    }

    private void initSysDataForDB() {/*
    	不知道干嘛的
        //cn.sunline.ltts.busi.iobus.servicetype.ap.IoApEvent
        KnpSvcx fwsxdy = KnpSvcxDao.selectOne_odb1(
                IoApEvent.class.getSimpleName(), ApConstants.dayendRepADM_event_key, false);
        if (fwsxdy == null) {
            fwsxdy = SysUtil.getInstance(KnpSvcx.class);
            fwsxdy.setRemark("联机批量汇报Adm");
            fwsxdy.setSvimid("IoBatchDayEndRegADM");
            fwsxdy.setSvimky(ApConstants.dayendRepADM_event_key);
            fwsxdy.setSvtpid(IoApEvent.class.getSimpleName());
            KnpSvcxDao.insert(fwsxdy);
        }

        fwsxdy = KnpSvcxDao.selectOne_odb1(
                IoApEvent.class.getSimpleName(), ApConstants.dayendGLRepADM_event_key, false);
        if (fwsxdy == null) {
            fwsxdy = SysUtil.getInstance(KnpSvcx.class);
            fwsxdy.setRemark("总账批量汇报Adm");
            fwsxdy.setSvimid("IoGLDayendRegADM");
            fwsxdy.setSvimky(ApConstants.dayendGLRepADM_event_key);
            fwsxdy.setSvtpid(IoApEvent.class.getSimpleName());
            KnpSvcxDao.insert(fwsxdy);
        }
    */}

    @Override
    public void shutdownPlugin() {
    }
}
