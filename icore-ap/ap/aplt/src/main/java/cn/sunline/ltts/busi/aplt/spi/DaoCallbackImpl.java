package cn.sunline.ltts.busi.aplt.spi;

import java.util.Map;

import org.springframework.core.annotation.Order;

import cn.sunline.adp.metadata.base.dao.Operator;
import cn.sunline.edsp.base.factories.SPIMeta;
import cn.sunline.ltts.busi.aplt.impl.DaoProcessCallBackImpl;
import cn.sunline.ltts.busi.aplt.tables.SysCommFieldTable1.kap_comm1;
import cn.sunline.ltts.busi.aplt.tools.SlTools;

@SPIMeta(id="dao_callback")
@Order(1000)
public class DaoCallbackImpl extends  DaoProcessCallBackImpl {
	
	@Override
	public void beforeDaoProcess(Class<?> tableType, Operator op,
			Object parameters) {
		super.beforeDaoProcess(tableType, op, parameters);
		
		if (tableType == null) {
			return;
		} else if (kap_comm1.class
				.isAssignableFrom(tableType)
				|| kap_comm1.class
						.isAssignableFrom(tableType)
				|| kap_comm1.class
						.isAssignableFrom(tableType)) {
			SlTools.setCommField(tableType, parameters);
		}
		;
	}
	@Override
	public void beforeDaoProcess(Class<?> intfClass, Object parameters) {
		super.beforeDaoProcess(intfClass, parameters);
	}
}
