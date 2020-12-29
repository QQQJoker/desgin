package cn.sunline.clwj.zdbank.impl;

import cn.sunline.adp.cedar.base.logging.SysLog;
import cn.sunline.adp.cedar.base.logging.SysLogUtil;
import cn.sunline.clwj.zdbank.util.AKSUtil;
import cn.sunline.clwj.zdbank.util.IEncrypt;

public class IEncryptAKSImpl implements IEncrypt{
	private static final SysLog log = SysLogUtil.getSysLog(IEncryptAKSImpl.class);

	@Override
	public String encrypt(String srcData) {
		String desData = AKSUtil.encryptByAKS(srcData);
		log.debug("invoke aks encrypt service beforData: %s , afterData: %s ",srcData,desData);
		return desData;
	}

}
