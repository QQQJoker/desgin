package com.qcloud.sms;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.errors.ApError.Aplt;
import cn.sunline.ltts.busi.sys.tables.KnpParaTable.KnpPara;

public class SmsSenderConfig {
	
	private static int _APPID_DEFAULT = 1400036859;
	private static String _APPKEY_DEFAULT = "8ba5c6a96dd09a7fbcb144b7e19e7ae6";
	private static String _URL_DEFAULT = "https://yun.tim.qq.com/v5/tlssmssvr/sendsms";
	
	public static boolean isEnabled() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "enabled", "%", "%", false);
		if (p == null) {
			return false;
		} else {
			try {
				return Boolean.parseBoolean(p.getPmval1());
			} catch (Exception e) {
				throw Aplt.E0000("SMS短信消息发送参数[enabled]必须配置为布尔型！[knp_para,parmcd=SmsSingleSender]");
			}
		}
	}

	public static int getAppid() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "appid", "%", "%", false);
		if (p == null) {
			return _APPID_DEFAULT;// 没有配置按默认的
		} else {
			try {
				return Integer.parseInt(p.getPmval1());
			} catch (NumberFormatException e) {
				throw Aplt.E0000("SMS短信消息发送参数[appid]必须配置为整数！[knp_para,parmcd=SmsSingleSender]");
			}
		}
	}

	public static String getAppkey() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "appkey", "%", "%", false);
		if (p == null) {
			return _APPKEY_DEFAULT;// 没有配置按默认的
		}
		if (CommUtil.isNull(p.getPmval1()))
			throw Aplt.E0000("SMS短信消息发送配置参数[appkey]不正确！[knp_para,parmcd=SmsSingleSender]");
		else
			return p.getPmval1();
	}

	public static String getUrl() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "url", "%", "%", false);
		if (p == null) {
			return _URL_DEFAULT; // 没有配置按默认的
		}
		if (CommUtil.isNull(p.getPmval1()))
			throw Aplt.E0000("SMS短信消息发送配置参数[url]不正确！[knp_para,parmcd=SmsSingleSender]");
		else
			return p.getPmval1();
	}

	public static String getPhoneNumber() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "phone", "%", "%", false);
		if (p == null) {
			throw Aplt.E0000("SMS短信消息发送配置参数[phone]不正确！[knp_para,parmcd=SmsSingleSender]");
		}
		if (CommUtil.isNull(p.getPmval1()))
			throw Aplt.E0000("SMS短信消息发送配置参数[phone]不正确！[knp_para,parmcd=SmsSingleSender]");
		else
			return p.getPmval1();
	}

	public static int getTemplId() {
		KnpPara p = CommTools.KnpParaQryByCorpno("SmsSingleSender", "templId", "%", "%", false);
		if (p == null) {
			throw Aplt.E0000("SMS短信消息发送配置参数[templId]不正确！[knp_para,parmcd=SmsSingleSender]");
		}
		if (CommUtil.isNull(p.getPmval1()))
			throw Aplt.E0000("SMS短信消息发送配置参数[templId]不正确！[knp_para,parmcd=SmsSingleSender]");
		else {
			try {
				return Integer.parseInt(p.getPmval1());
			} catch (NumberFormatException e) {
				throw Aplt.E0000("SMS短信消息发送参数[templId]必须配置为整数！[knp_para,parmcd=SmsSingleSender]");
			}
		}
	}
}
