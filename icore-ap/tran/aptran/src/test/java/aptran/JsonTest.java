package aptran;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.busi.sdk.biz.global.SysUtil;

public class JsonTest {
	
	
	@Test
	public void test() {
		try {
			Long l = null;
			l.byteValue();
		} catch (Exception e) {
			System.out.println(e.getCause());
		}
		
		
	}

	public static void main(String[] args) throws Exception {
		String jsonStr= "{'tabnam':'knp_para','sqlfid':'KnpParaSql.updKnpParaByParmcdAndPmkey1','effcnt':1,'sqlpam':{'pmval2':'','pmkey2':'','pmval3':'','pmkey3':'','pmval1':'','parmcd':'testSqlPara','pmkey1':'pmkey1','pmval5':'pmval5','pmval4':'pmval4'}}";
		String clzssStr = "cn.sunline.ltts.busi.bsap.type.ApMessageComplexType$SQLCType";
		Class<?> cls = Class.forName(clzssStr);
		Object sqlcType = SysUtil.deserialize(jsonStr, cls);
		Map<String,Object> map = new HashMap<String, Object>();
		map.put("cnt", 1);
		map.put("ky", "str");
		System.out.println(SysUtil.serialize(sqlcType));
		map.put("sqlc", SysUtil.serialize(sqlcType));
		String serialize = SysUtil.serialize(map);
		System.out.println(serialize);
	}
}
