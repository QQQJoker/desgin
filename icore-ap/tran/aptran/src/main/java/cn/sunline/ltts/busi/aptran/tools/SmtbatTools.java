package cn.sunline.ltts.busi.aptran.tools;

import java.util.ArrayList;
import java.util.List;

import cn.sunline.ltts.busi.aplt.tools.DcnUtil;
import cn.sunline.ltts.busi.sys.type.BaseEnumType.E_DCNTP;

public class SmtbatTools {

	/**
	 * 根据DCN类型获得DCN列表
	 * @param tdcntp：A-dcn，R-dcn，C-dcn
	 * @return
	 */
	public static List<String> getDcnList(E_DCNTP tdcntp) {
		List<String> dcnList = new ArrayList<>();
		switch (tdcntp) {
		case A_DCN:
			dcnList.add(DcnUtil.findAdminDcnNo());
			break;
		case C_DCN:
			dcnList.addAll(DcnUtil.findAllCDcnNos());
			break;
		case R_DCN:
			dcnList.addAll(DcnUtil.findAllRDcnNos());
			break;
		default:
			dcnList.addAll(DcnUtil.findAllDcnNosWithAdmin());
			break;
		}
		return dcnList;
	}
	
	
}
