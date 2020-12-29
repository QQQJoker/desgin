package drs;

import java.lang.reflect.InvocationTargetException;

import com.alibaba.fastjson.JSONObject;

import cn.sunline.ltts.busi.aplt.tools.CommTools;
import cn.sunline.ltts.busi.sys.parm.TrxEnvs;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_AUTHLV;

public class AuthInfoTest {

	
	public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		TrxEnvs.AuthInfo trxEnvs = CommTools.getInstance(TrxEnvs.AuthInfo.class);

		trxEnvs.setAuthlv(E_AUTHLV.JJ);
		
		System.out.println(JSONObject.toJSON(trxEnvs));
	}
}
