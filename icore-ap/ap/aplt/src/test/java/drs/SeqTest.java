package drs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;
import cn.sunline.ltts.busi.aplt.type.ApDefineType.ApParaMatain;



public class SeqTest {

	@Test
	public void test() {
		List list = new ArrayList();
		Map map = new HashMap();
		map.put("a", null);
		map.put("b", "");
		list.add(map);
		ApParaMatain ss = SysUtil.getInstance(ApParaMatain.class);
		Map a = CommUtil.toMap(ss);
		boolean s = CommUtil.isNotNull(a);
		System.out.println(s);
	}

}
