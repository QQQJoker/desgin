package cn.sunline.clwj.zdbank.encstring;
//package cn.sunline.edsp.zd.paas.encstring;
//
//import cn.sunline.adp.cedar.base.logging.BizLog;
//import cn.sunline.adp.cedar.base.logging.BizLogUtil;
//import cn.sunline.edsp.base.security.EncStringSecurity;
//import cn.sunline.edsp.busi.msap.jd.aks.errors.AksError;
//import cn.sunline.edsp.microcore.spi.SPIMeta;
//import cn.sunline.edsp.zd.paas.plugin.AKSPlugin;
//
//@SPIMeta(id = "aks")
//public class AKSEncStringSecurity implements EncStringSecurity{
//	
//	private static final BizLog LOG = BizLogUtil.getBizLog(AKSEncStringSecurity.class);
//
//	@Override
//	public String encrypt(String srcData) {
//		if (null == srcData || srcData.length() == 0) {
//			return srcData;
//		}
//		String aliasName = AKSPlugin.config.getAppname();
//		String encStr = AKSPlugin.deviceCryptoService.encryptString(aliasName, srcData.getBytes());
//		if (encStr == null) {
//			throw AksError.aksConn.A0001();
//		}
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("aks encrypt srcData: [" + srcData + "], encStr[" + encStr + "]");
//		}
//		return encStr;
//	}
//
//	@Override
//	public String decrypt(String encData) {
//		if (null == encData || encData.length() == 0) {
//			return encData;
//		}
//		String aliasName = AKSPlugin.config.getAppname();
//		byte[] decBytes = AKSPlugin.deviceCryptoService.decryptString(aliasName, encData);
//		if (decBytes == null) {
//			throw AksError.aksConn.A0002();
//		}
//		String srcStr = new String(decBytes);
//		if (LOG.isDebugEnabled()) {
//			LOG.debug("aks decrypt encData: [" + encData + "], srcStr[" + srcStr + "]");
//		}
//		return srcStr;
//	}
//
//}
