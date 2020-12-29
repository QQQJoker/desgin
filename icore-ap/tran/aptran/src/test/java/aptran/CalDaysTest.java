package aptran;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import cn.sunline.ltts.busi.aplt.tools.DateTools;
//import cn.sunline.ltts.busi.sys.datatype.BaseEnumType.E_JIAOYILX;
//import cn.sunline.ltts.busi.sys.datatype.BaseEnumType.E_QDAOLEIX;
import cn.sunline.adp.cedar.engine.online.AdpUnitTest;
/**
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author T
 *         <p>
 *         <li>2014年5月30日-上午10:47:38</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228T：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class CalDaysTest extends AdpUnitTest {

	//private static final BizLog bizlog = LogManager.getBizLog(CalDaysTest.class);
	@Before
	public void setup() throws Exception {
		Map<String, Object> trxReq = new HashMap<String, Object>();

		trxReq.put("huihuaid", "TESTID");
		trxReq.put("jiaoyigy", "000658");
		trxReq.put("jiaoyijg", "5521101");
		trxReq.put("fenhdaim", "55");
		trxReq.put("jiaoyima", "0000");
		trxReq.put("waibclma", "tlsigw");
		//trxReq.put("qudaoolx", E_QDAOLEIX.GM.toString());
		trxReq.put("waiblius", "wblsh");
		trxReq.put("shouqlsh", "sqlsh");
		trxReq.put("ipdizhii", "10.10.20.70");
		//trxReq.put("jiaoyilx", E_JIAOYILX.WHJY.toString());
		trxReq.put("querenbz", "1");
		trxReq.put("chongzbz", "0");

		initBizEnv(trxReq);
	}

	@Test
	public void TestDay() {
		test("20140105", "20140205", 0,0, 31);
		test("20140105", "20140305", 0,0, 59);
		test("20140105", "20140325", 0,0, 79);
	}

	private void test(String sStartDate, String sEndDate, int iType, int iFlag, int a) {
		int iTerm = DateTools.calDays(sStartDate, sEndDate, iType, iFlag);
		Assert.assertEquals(a, iTerm);
	}
}
