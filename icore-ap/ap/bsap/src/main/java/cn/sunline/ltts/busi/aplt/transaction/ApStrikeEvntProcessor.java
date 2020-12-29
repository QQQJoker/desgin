package cn.sunline.ltts.busi.aplt.transaction;

import cn.sunline.ltts.busi.aplt.tables.SysBusiTable.KnbEvnt;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_COLOUR;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_STACPS;

/**
 * 冲正事件接口类
 * 实现类规则：ApStrikeEvntProcessor+交易事件,如：ApStrikeEvntProcessorDP01-开户冲正
 */
public interface ApStrikeEvntProcessor {
	public static String  implClazzPrefix="cn.sunline.ltts.busi.aplt.strk.impl.ApStrikeEvntProcessor";
	/**
	 * 冲正事件接口
	 * @param stacps 冲正分类
	 * @param colour 红蓝字标记
	 * @param evnt 冲正事件
	 */
	public void process(E_STACPS stacps,E_COLOUR colour, KnbEvnt evnt);

}
