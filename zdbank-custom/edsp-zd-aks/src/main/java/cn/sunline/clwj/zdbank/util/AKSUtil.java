package cn.sunline.clwj.zdbank.util;

import java.util.Map;

import com.wangyin.key.server.DeviceCryptoService;
import com.wangyin.key.server.model.RadixEnum;

import cn.sunline.clwj.zdbank.busi.aks.errors.AksError;
import cn.sunline.clwj.zdbank.plugin.AKSPlugin;

public class AKSUtil {

	
	/**
	 * 保型加密
	 * 
	 * @param aliasName
	 *            密钥别名，需要在接入前向AKS系统申请
	 * @param radix
	 *            保型进制，10进制_数字字符串 或者 64进制_base64字符串
	 * @param tweak
	 *            加密扰乱(base64字符串)
	 * @param srcData
	 *            待加密原文
	 * 
	 *@exception 加密失败，返回的数字串为null
	 *            
	 * @return 加密后密文
	 * 
	 * @author xieqq 20190618
	 * 
	 */
	public static String encryptByAKS(String srcData) {
		String decStr = AKSPlugin.deviceCryptoService.encryptByFF1(AKSPlugin.config.getSecure(), RadixEnum.Radix_10, "", srcData);
		if (decStr == null) {
			throw AksError.aksConn.A0001();
		}
		return decStr;
	}

	/**
	 * 保型解密
	 * 
	 * @param aliasName
	 *            密钥别名，需要在接入前向AKS系统申请
	 * @param radix
	 *            保型进制，10进制_数字字符串 或者 64进制_base64字符串
	 * @param tweak
	 *            加密扰乱(base64字符串)
	 * @param enData
	 *            待解密原文
	 * @return 解密后明文
	 * 
	 * @exception 解密失败，返回的数字串为null
	 * 
	 * @author xieqq 20190618
	 */
	public static String decryptByAKS(String enData) {
		String srcStr = AKSPlugin.deviceCryptoService.decryptByFF1(AKSPlugin.config.getSecure(), RadixEnum.Radix_10, "", enData);
		if (srcStr == null) {
			throw AksError.aksConn.A0002();
		}
		return srcStr;
	}
	
	/**
	 * 获取加密客户端实例
	 * @return
	 */
	public static DeviceCryptoService getAKSClient() {
		return AKSPlugin.deviceCryptoService;
	}
	

}

