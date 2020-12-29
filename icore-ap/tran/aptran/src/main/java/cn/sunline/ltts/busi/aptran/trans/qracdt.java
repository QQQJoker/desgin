
package cn.sunline.ltts.busi.aptran.trans;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.base.lang.Options;
import cn.sunline.edsp.base.lang.Page;
import cn.sunline.edsp.base.lang.options.DefaultOptions;
import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.busi.icore.ap.acdt.namedsql.ApAcdtNSQLDao;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.busi.aplt.tools.CommToolsAplt;
import cn.sunline.ltts.busi.sys.errors.ApError;
/**
 * 
 * <p>
 * 文件功能说明：
 *       			
 * </p>
 * 
 * @Author os_cl_zhouxin
 *         <p>
 *         <li>2020年10月27日-上午9:08:41</li>
 *         <li>修改记录</li>
 *         <li>增加查询账务日期功能</li>
 *         <li>标记：修订内容</li>
 *         <li>2020年10月27日-os_cl_zhouxin：创建注释模板</li>
 *         <li></li>
 *         </p>
 */
public class qracdt {
	/**
	 * 
	 * @Author os_cl_zhouxin
	 *         <p>
	 *         <li>2020年10月27日-上午9:10:38</li>
	 *         <li>功能说明：查询所有的账务日期</li>
	 *         </p>
	 * @param output
	 */

	public static void qracdt( final cn.sunline.ltts.busi.aptran.trans.intf.Qracdt.Output output){
		//分页查询页码、页容量设置
		int pageno = ConvertUtil.toInteger(CommToolsAplt.prcRunEnvs().getPageno());//页码
		int pgsize = ConvertUtil.toInteger(CommToolsAplt.prcRunEnvs().getPgsize());//页容量
		int totlCount = 0; // 记录总数
		int startno = (pageno - 1) * pgsize;// 起始记录数
		
		// 取得账务日期信息列表
		Page<AppAcdt> apacdt=ApAcdtNSQLDao.lstApAcdt(startno, pgsize, totlCount, false);
		if(CommUtil.isNull(apacdt)) {
			throw ApError.Acdt.E0001();
		}
		Options<AppAcdt> options=new DefaultOptions<AppAcdt>();
		
		options.addAll(apacdt.getRecords());
		
		// 设置总记录数
		CommToolsAplt.prcRunEnvs().setCounts(apacdt.getRecordCount());
		
		output.setAcdt(options);
	}






}
