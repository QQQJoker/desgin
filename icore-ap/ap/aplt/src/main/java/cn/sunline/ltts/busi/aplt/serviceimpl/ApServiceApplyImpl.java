package cn.sunline.ltts.busi.aplt.serviceimpl;

import cn.sunline.ltts.busi.aplt.para.ApParaManage;
import cn.sunline.ltts.busi.aplt.type.ApDefineType.ApParaMatain;

/**
 * 应用平台服务应用实现 应用平台服务应用实现
 */
@cn.sunline.adp.core.annotation.Generated
public class ApServiceApplyImpl implements
		cn.sunline.ltts.busi.aplt.servicetype.ApServiceApply {

	@Override
	public String prcParaMatain(ApParaMatain cplApParaMatainIn) {
		return ApParaManage.prcParaMatain(cplApParaMatainIn);
	}
}
