package aptran;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import cn.sunline.adp.cedar.base.engine.ResponseData;
import cn.sunline.ltts.busi.aplt.junit.OnlineTest;

public class SmtbatTest extends OnlineTest{

	@Test
	public void test() {
		initTxnPkg("smtbat.json");
        ResponseData ret = call();
        Assert.assertEquals(ret.getHeaderData().getRetStatus(), "S");
	}

}
