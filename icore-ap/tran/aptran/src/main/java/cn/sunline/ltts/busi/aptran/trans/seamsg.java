package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;

public class seamsg {


	/**
	 * 将传入的消息json 转化成对应的object对象
	 * @param input
	 * @param property
	 */
	public static void preProcessData(
			final cn.sunline.ltts.busi.amsg.trans.intf.Seamsg.Input input,
			final cn.sunline.ltts.busi.amsg.trans.intf.Seamsg.Property property) {
		String msgstr = input.getMsgstr();
		String msgtyp = input.getMsgtyp();
		try {
			Class<?> clzss = Class.forName(msgtyp);
			Object parse = SysUtil.deserialize(msgstr, clzss);
			property.setMsgobj(parse);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
