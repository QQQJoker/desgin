package cn.sunline.clwj.zdbank.util;

import cn.sunline.adp.cedar.base.boot.plugin.IReplaceExtension;

public interface IEncrypt extends IReplaceExtension{

	 public static String POINT = "pckg.aks.encrypt";
	 
	 public String encrypt(String srcStr);
}
