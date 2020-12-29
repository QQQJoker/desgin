package cn.sunline.ltts.busi.aplt.spi;

import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.metadata.base.dao.Operator;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.impl.DaoProcessCallBackImpl;
import cn.sunline.ltts.busi.aplt.tables.SysCommFieldTable1;
import cn.sunline.ltts.busi.aplt.tools.DateTools;
import cn.sunline.ltts.busi.aplt.tools.SlTools;
import cn.sunline.ltts.busi.sys.dict.ApDict;
import cn.sunline.ltts.busi.sys.dict.BaseDict;

@SPIMeta(id="dao_process_callback")
@Order(1000)
public class DaoProcessCallBackImplAplt extends DaoProcessCallBackImpl {
    private static final BizLog bizlog = BizLogUtil.getBizLog(DaoProcessCallBackImplAplt.class);

    @Override
    public void beforeDaoProcess(Class<?> tableType, Operator op, Object parameters) {
        super.beforeDaoProcess(tableType, op, parameters);
        if (tableType == null) {
            return;
        } else if(SysCommFieldTable1.kap_comm1.class.isAssignableFrom(tableType)){
        	SlTools.setCommField(tableType, parameters);
        };
    }

    /**
     * 在对引用营销系统公共字段的表进行新增、修改操作时进行法人编号赋值
     * 
     * @param parameters
     * @param corpno
     */
    public void setCommData(Object parameters, String corpno) {
        Map<String, Object> mapComm = CommUtil.toMap(parameters);
        if (bizlog.isDebugEnabled())
            bizlog.debug("表对象的当前法人值[%s]！", mapComm.get(ApDict.Aplt.corpno.getId()));

        if (CommUtil.isNull(mapComm.get(BaseDict.Comm.corpno.getId()))) {
            mapComm.put(BaseDict.Comm.corpno.getId(), corpno);
            if (bizlog.isDebugEnabled())
                bizlog.debug("表对象处理后的法人值[%s]！", mapComm.get(BaseDict.Comm.corpno.getId()));
        }

        if (CommUtil.isNull(mapComm.get("gmt_create"))) {
            mapComm.put("gmt_create", DateTools.getCurrentDateTime());
        }
        /*if(CommUtil.isNull(mapComm.get("gmt_modified"))){
          mapComm.put("gmt_modified", DateTools.getCurrentDateTime2());
        }*/
        mapComm.put("gmt_modified", DateTools.getCurrentDateTime());
    }
}
