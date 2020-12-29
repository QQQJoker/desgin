
package cn.sunline.ltts.busi.aptran.trans;

import java.util.Date;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.edsp.base.util.date.DateUtil;
import cn.sunline.edsp.busi.aptran.namedsql.UpdAppAcdtDao;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdt;
import cn.sunline.ltts.acdt.ApAcdt.AppAcdtDao;
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
 *         <li>2020年10月27日-下午7:30:07</li>
 *         <li>修改记录</li>
 *         <li>修改账务日期表</li>
 *         <li>标记：修订内容</li>
 *         <li>2020年10月27日-os_cl_zhouxin：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class upacdt {

	public static void upacdt( String oldvalidt,  String newvalidt){
		//生效日期不能为空
		if(CommUtil.isNull(newvalidt)) {
			throw ApError.Acdt.E0007();
		}
		
	
		//判断是否改动
		if(oldvalidt.compareTo(newvalidt)==0) {
			throw ApError.Acdt.E0008();
		}
		
		AppAcdt appacdt=AppAcdtDao.selectOne_odb1(newvalidt, false);
		//判断数据库中是否存在相同值
		if(CommUtil.isNotNull(appacdt)) {
			throw ApError.Acdt.E0005();
		}
		
		//当前系统时间
	     String updttm=DateUtil.getNow("yyyy-MM-dd HH:mm:ss");
	
		//生效日期不能小于当前系统时间
	   if(newvalidt.compareTo(updttm)<0) {
		throw ApError.Acdt.E0006();
	    }

		
		UpdAppAcdtDao.UpdAppAcdt(oldvalidt, newvalidt,updttm);

	}
}
	







