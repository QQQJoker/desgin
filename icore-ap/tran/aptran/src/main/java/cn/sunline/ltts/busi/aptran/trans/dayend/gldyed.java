package cn.sunline.ltts.busi.aptran.trans.dayend;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.aptran.dayend.DayEndTools;

public class gldyed {

	public static void glDayEnd() {
		String flowType = "gl_dayend";
		DayEndTools.beginFlow(CommTools.getFrdm(), flowType);
	}
}
