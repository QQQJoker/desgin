package cn.sunline.clwj.zdbank.cds.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import cn.sunline.adp.core.util.JsonUtil;
import cn.sunline.adp.metadata.base.util.PropertyUtil;

public class TestRoute {
	@Test
	public void testrouteStr() {
		String str = "{\"cplDrawAcctIn\":{\"acctno\":\"60000000000000000000123\",\"tranam\":\"100\",\"crcycd\":\"CNY\",\"cardno\":\"6231899929377663082\",\"custac\":\"9992000001102\",\"acseno\":null,\"toacct\":\"623510100009047\",\"opacna\":\"孙志伟\",\"linkno\":null,\"auacfg\":null,\"opbrch\":\"999000\",\"bankcd\":null,\"bankna\":null,\"smrycd\":null,\"smryds\":null,\"remark\":null,\"strktg\":null,\"ischck\":null,\"isdedu\":null,\"dedutp\":null,\"detlsq\":null,\"macdrs\":null,\"teleno\":null,\"imeino\":null,\"udidno\":null,\"trands\":null,\"servtp\":null,\"intrcd\":null,\"transq\":null,\"issucc\":null}}";
		Object value = PropertyUtil.createAccessor(JsonUtil.parse(str)).getNestedProperty("cplDrawAcctIn.acctno");
		assertEquals("60000000000000000000123", value);
	}
}
