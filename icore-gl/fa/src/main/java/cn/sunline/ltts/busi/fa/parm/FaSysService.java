package cn.sunline.ltts.busi.fa.parm;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.Fap_sys_defineDao;
import cn.sunline.ltts.busi.fa.tables.TabFaParm.fap_sys_define;
import cn.sunline.ltts.busi.sys.type.GlBusinessType.E_SYSTEMSERVICESTATUS;
import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.ltts.fa.util.FaConst;
/**
 * <p>
 * 文件功能说明：
 *       	总账服务	
 * </p>
 * 
 * @Author hehe
 *         <p>
 *         <li>2017年3月13日-上午11:19:33</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>2017年3月13日-hehe：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class FaSysService {

	private static final BizLog bizlog = BizLogUtil.getBizLog(FaSysService.class);
	
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-上午11:33:07</li>
	 *         <li>功能说明：开启总账服务</li>
	 *         </p>
	 */
	public static void setSysClose() {
		setSysStatus(E_SYSTEMSERVICESTATUS.OFF);
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-上午11:33:10</li>
	 *         <li>功能说明：关闭总账服务</li>
	 *         </p>
	 */
	public static void setSysOpen() {		
		setSysStatus(E_SYSTEMSERVICESTATUS.ON);		
	}
	
	/**
	 * @Author hehe
	 *         <p>
	 *         <li>2017年3月13日-上午11:29:37</li>
	 *         <li>功能说明：更改总账服务状态</li>
	 *         </p>
	 * @param status
	 */
	private static void setSysStatus( E_SYSTEMSERVICESTATUS status ) {
		
		bizlog.method("setStatus begin >>>>>>>>>>>>>>>>>>>>");
		bizlog.parm("status [%s]", status);
		
		fap_sys_define sysTable = Fap_sys_defineDao.selectOne_odb1( FaConst.GL_SYSTEM , true);
		
		sysTable.setSystem_service_status(status);
		sysTable.setRecdver(sysTable.getRecdver() + 1);
		
		Fap_sys_defineDao.updateOne_odb1(sysTable);
		
		bizlog.method("FaSysService end <<<<<<<<<<<<<<<<<<<<");
		
	}
	public static E_SYSTEMSERVICESTATUS getSysStatus() {
		
		bizlog.method("getSysStatus begin >>>>>>>>>>>>>>>>>>>>");
		
		fap_sys_define sysTable = Fap_sys_defineDao.selectOne_odb1( FaConst.GL_SYSTEM , true);

		
		bizlog.method("getSysStatus end <<<<<<<<<<<<<<<<<<<<");
		return sysTable.getSystem_service_status();
		
	}
}
