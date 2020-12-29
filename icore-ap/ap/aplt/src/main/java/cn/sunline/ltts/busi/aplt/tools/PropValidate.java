package cn.sunline.ltts.busi.aplt.tools;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import oracle.net.ns.Communication;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.SlError;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_FINLIN;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_INITIN;
import cn.sunline.ltts.busi.sys.type.SlEnumType.E_PRVLTP;
/**
 * 
 * @ClassName: PropValidate 
 * @Description: 属性值校验公共类
 * @author baifangping
 * @date 2016年8月11日 上午8:59:18 
 *
 */
public class PropValidate {
	/**
	 * 
	 * @Title: PropIntrV 
	 * @Description: 整数型属性值校验 
	 * @param oInitinV	开始起始值包含
	 * @param oInitv	开始起始值
	 * @param oFinlinV	开始终止值包含
	 * @param oFinlvl	开始终止值
	 * @param nInitinV	输入起始值包含
	 * @param nInitvS	输入起始值
	 * @param nFinlinV	输入终止值包含
	 * @param nFinlvlS	输入终止值
	 * @param propvlS	输入属性缺省值
	 * @author baifangping
	 * @date 2016年8月11日 上午9:11:53 
	 * @version V2.3.0
	 * @return 
	 */
	public static void PropIntrV(String oInitinV, Long oInitv, String oFinlinV,
			Long oFinlvl, String nInitinV, String nInitvS, String nFinlinV, String nFinlvlS, String propvlS) {
		//1、类型转化String——>Long
		long propvl = 0;
		long initvl = 0;
		long finlvl = 0;
		try {
			if (CommUtil.isNotNull(propvlS)) {
				propvl = Long.parseLong(propvlS);// 转换LONG类型
			}
			if (CommUtil.isNotNull(nInitvS)) {
				initvl = Long.parseLong(nInitvS);// 转换LONG类型
			}
			if (CommUtil.isNotNull(nFinlvlS)) {
				finlvl = Long.parseLong(nFinlvlS);// 转换LONG类型
			}
		} catch (Exception e) {
			throw SlError.SlComm.E0001("类型转换异常，请确认属性值类型是否正确！");
		}
		// 2、开始起始值不包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//2.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl < oInitv)||(initvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
			}else {
				//包含（x < i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl <= oInitv)||(initvl >= oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
			}
			//2.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl < oInitv)||(finlvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}else {
				//包含（x < f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl <= oInitv)||(finlvl >= oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}
		}
		//3、开始起始值包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//3.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl < oInitv)||(initvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
			}else {
				//包含（x <= i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl < oInitv)||(initvl >= oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
			}
			//3.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl < oInitv)||(finlvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}else {
				//包含（x <= f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl < oInitv)||(finlvl >= oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}
		}
		//4、开始起始值不包含、开始终止值包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//4.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl < oInitv)||(initvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
				
				
			}else {
				//包含（x < i <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((initvl <= oInitv)||(initvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值起始值输入有误！");
				}
			}
			//4.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl < oInitv)||(finlvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}else {
				//包含（x < f <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((finlvl <= oInitv)||(finlvl > oFinlvl)){
					throw SlError.SlComm.E0001("属性值终止值输入有误！");
				}
			}
		}
		//5、开始起始值包含、开始起始值包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//输入起始值（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if((initvl < oInitv)||(initvl > oFinlvl)){
				throw SlError.SlComm.E0001("属性值起始值输入有误！");
			}
			//输入终止值（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if((finlvl < oInitv)||(finlvl > oFinlvl)){
				throw SlError.SlComm.E0001("属性值终止值输入有误！");
			}
			
		}
		//6、判断输入起始值与输入终止值
		if (initvl > finlvl) {
			throw SlError.SlComm.E0001("属性值输入有误！");
		}
		//7、判断输入属性缺省值
		
		if (CommUtil.isNotNull(propvlS)) {
			if ((propvl<initvl)||(propvl>finlvl)) {
				throw SlError.SlComm.E0001("属性缺省值输入有误！");
			}
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				if (propvl==initvl) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
			}
			if (CommUtil.equals(oFinlinV, E_INITIN.NO.getValue())) {
				if (propvl==finlvl) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
			}
		}
	}
	/**
	 * 
	 * @Title: PropDateV 
	 * @Description: 日期类型属性值校验 
	 * @param oInitinV	开始起始值包含
	 * @param oInitv	开始起始值
	 * @param oFinlinV	开始终止值包含
	 * @param oFinlvl	开始终止值
	 * @param nInitinV	输入起始值包含
	 * @param nInitvS	输入起始值
	 * @param nFinlinV	输入终止值包含
	 * @param nFinlvlS	输入终止值
	 * @param propvlS	输入属性缺省值
	 * @author baifangping
	 * @date 2016年8月11日 上午10:22:44 
	 * @version V2.3.0
	 */
	public static void PropDateV(String oInitinV, Date oInitv, String oFinlinV,
			Date oFinlvl, String nInitinV, String nInitvS, String nFinlinV,
			String nFinlvlS, String propvlS) {
		//1、类型装换String->date
		Date nInitv = SlTools.SqlStringToDate2(nInitvS);
		Date nFinlvl =SlTools.SqlStringToDate2(nFinlvlS);
		//2、开始起始值不包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//2.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
				
			}else {
				//包含（x < i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oInitv)==0)||(DateTools2.compareDate(nInitv,oFinlvl)==1)||(DateTools2.compareDate(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
				
			}
			//2.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
			}else {
				//包含（x < f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oInitv)==0)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
			}
		}
		//3、开始起始值包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//3.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
			}else {
				//包含（x <= i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oFinlvl)==1)||(DateTools2.compareDate(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
			}
			//3.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
			}else {
				//包含（x <= f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
			}
		}
		//4、开始起始值不包含、开始终止值包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//4.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
			}else {
				//包含（x < i <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oInitv)==0)||(DateTools2.compareDate(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
				}
			}
			//4.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
				
			}else {
				//包含（x < f <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oInitv)==0)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
				}
			}
		}
		//5、开始起始值包含、开始起始值包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//输入起始值（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((DateTools2.compareDate(nInitv,oInitv)==-1)||(DateTools2.compareDate(nInitv,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值起始值输入有误(日期类型)！");
			}
			//输入终止值（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((DateTools2.compareDate(nFinlvl,oInitv)==-1)||(DateTools2.compareDate(nFinlvl,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值終止值输入有误(日期类型)！");
			}
			
		}
		//6、判断输入起始值与输入终止值
		if (DateTools2.compareDate(nInitv,nFinlvl)==1) {
			throw SlError.SlComm.E0001("属性值输入有误(日期类型)！");
		}
		//7、判断输入属性缺省值
		if (CommUtil.isNotNull(propvlS)) {
			Date propvl =SlTools.SqlStringToDate2(propvlS);
			if ((DateTools2.compareDate(nInitv,propvl)==1)||(DateTools2.compareDate(nFinlvl,propvl)==-1)) {
				throw SlError.SlComm.E0001("属性值输入有误(日期类型)！");
			}
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				if (DateTools2.compareDate(nInitv,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(日期类型)！");
				}
			}
			if (CommUtil.equals(oFinlinV, E_INITIN.NO.getValue())) {
				if (DateTools2.compareDate(nFinlvl,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(日期类型)！");
				}
			}
		}
		
	}
	/**
	 * 
	 * @Title: PropDatmV 
	 * @Description: 时间戳类型属性值校验 
	 * @param oInitinV	开始起始值包含
	 * @param oInitvS	开始起始值
	 * @param oFinlinV	开始终止值包含
	 * @param oFinlvlS	开始终止值
	 * @param nInitinV	输入起始值包含
	 * @param nInitv	输入起始值
	 * @param nFinlinV	输入终止值包含
	 * @param nFinlvl	输入终止值
	 * @param propvl	输入属性缺省值
	 * @author baifangping
	 * @date 2016年9月1日 上午10:22:44 
	 * @version V2.3.0
	 */
	public static void PropDatmV(String oInitinV, Timestamp oInitvS, String oFinlinV,
			Timestamp oFinlvlS, String nInitinV, String nInitv, String nFinlinV,
			String nFinlvl, String propvl) {
		//1、类型装换String->date 
		String oInitv = SlTools.SqlTimestampToString(oInitvS);
		String oFinlvl =SlTools.SqlTimestampToString(oFinlvlS);
		//2、开始起始值不包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//2.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
				
			}else {
				//包含（x < i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oInitv)==0)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
				
			}
			//2.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
			}else {
				//包含（x < f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oInitv)==0)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
			}
		}
		//3、开始起始值包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//3.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
			}else {
				//包含（x <= i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
			}
			//3.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
			}else {
				//包含（x <= f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
			}
		}
		//4、开始起始值不包含、开始终止值包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//4.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
			}else {
				//包含（x < i <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oInitv)==0)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
				}
			}
			//4.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
				
			}else {
				//包含（x < f <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oInitv)==0)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
				}
			}
		}
		//5、开始起始值包含、开始起始值包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//输入起始值（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((SlTools.compareTimeDatm(nInitv,oInitv)==-1)||(SlTools.compareTimeDatm(nInitv,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值起始值输入有误(日期戳类型)！");
			}
			//输入终止值（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((SlTools.compareTimeDatm(nFinlvl,oInitv)==-1)||(SlTools.compareTimeDatm(nFinlvl,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值終止值输入有误(日期戳类型)！");
			}
			
		}
		//6、判断输入起始值与输入终止值
		if (SlTools.compareTimeDatm(nInitv,nFinlvl)==1) {
			throw SlError.SlComm.E0001("属性值输入有误(日期戳类型)！");
		}
		//7、判断输入属性缺省值
		if (CommUtil.isNotNull(propvl)) {
			if ((SlTools.compareTimeDatm(nInitv,propvl)==1)||(SlTools.compareTimeDatm(nFinlvl,propvl)==-1)) {
				throw SlError.SlComm.E0001("属性值输入有误(日期戳类型)！");
			}
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				if (SlTools.compareTimeDatm(nInitv,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(日期戳类型)！");
				}
			}
			if (CommUtil.equals(oFinlinV, E_INITIN.NO.getValue())) {
				if (SlTools.compareTimeDatm(nFinlvl,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(日期戳类型)！");
				}
			}
		}
		
	}
	/**
	 * 
	 * @Title: PropTimeV 
	 * @Description: 时间类型属性值校验 
	 * @param oInitinV	开始起始值包含
	 * @param oInitvS	开始起始值
	 * @param oFinlinV	开始终止值包含
	 * @param oFinlvlS	开始终止值
	 * @param nInitinV	输入起始值包含
	 * @param nInitv	输入起始值
	 * @param nFinlinV	输入终止值包含
	 * @param nFinlvl	输入终止值
	 * @param propvl	输入属性缺省值
	 * @author baifangping
	 * @date 2016年8月11日 上午11:14:09 
	 * @version V2.3.0
	 */
	public static void PropTimeV(String oInitinV, Time oInitvS, String oFinlinV,
			Time oFinlvlS, String nInitinV, String nInitv, String nFinlinV,
			String nFinlvl, String propvl) {
		//1、类型转换
		String oInitv = SlTools.SqlTimeToString2(oInitvS);
		String oFinlvl = SlTools.SqlTimeToString2(oFinlvlS);
		
		//2、开始起始值不包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//2.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
				
			}else {
				//包含（x < i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oInitv)==0)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)||(SlTools.compareTimeN(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
				
			}
			//2.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
			}else {
				//包含（x < f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oInitv)==0)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
			}
		}
		//3、开始起始值包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//3.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
			}else {
				//包含（x <= i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)||(SlTools.compareTimeN(nInitv,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
			}
			//3.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
			}else {
				//包含（x <= f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==0)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
			}
		}
		//4、开始起始值不包含、开始终止值包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//4.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
			}else {
				//包含（x < i <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oInitv)==0)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
				}
			}
			//4.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
				
			}else {
				//包含（x < f <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oInitv)==0)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)) {
					throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
				}
			}
		}
		//5、开始起始值包含、开始起始值包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//输入起始值（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((SlTools.compareTimeN(nInitv,oInitv)==-1)||(SlTools.compareTimeN(nInitv,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值起始值输入有误(时间类型)！");
			}
			//输入终止值（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if ((SlTools.compareTimeN(nFinlvl,oInitv)==-1)||(SlTools.compareTimeN(nFinlvl,oFinlvl)==1)) {
				throw SlError.SlComm.E0001("属性值終止值输入有误(时间类型)！");
			}
			
		}
		//6、判断输入起始值与输入终止值
		if (SlTools.compareTimeN(nInitv,nFinlvl)==1) {
			throw SlError.SlComm.E0001("属性值输入有误(时间类型)！");
		}
		//7、判断输入属性缺省值
		if (CommUtil.isNotNull(propvl)) {
			if ((SlTools.compareTimeN(nInitv,propvl)==1)||(SlTools.compareTimeN(nFinlvl,propvl)==-1)) {
				throw SlError.SlComm.E0001("属性值输入有误(时间类型)！");
			}
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				if (SlTools.compareTimeN(nInitv,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(时间类型)！");
				}
			}
			if (CommUtil.equals(oFinlinV, E_INITIN.NO.getValue())) {
				if (SlTools.compareTimeN(nFinlvl,propvl)==0) {
					throw SlError.SlComm.E0001("属性值输入有误(时间类型)！");
				}
			}
		}
	}
	 
	/**
	 * 
	 * @Title: PropDeclV 
	 * @Description: 浮点类型属性值校验 
	 * @param oInitinV	开始起始值包含
	 * @param oInitv	开始起始值
	 * @param oFinlinV	开始终止值包含
	 * @param oFinlvl	开始终止值
	 * @param nInitinV	输入起始值包含
	 * @param nInitvS	输入起始值
	 * @param nFinlinV	输入终止值包含
	 * @param nFinlvlS	输入终止值
	 * @param propvlS	输入属性缺省值
	 * @author baifangping
	 * @date 2016年8月11日 下午2:54:45 
	 * @version V2.3.0
	 */
	 public static void PropDeclV(String oInitinV, BigDecimal oInitv,
		String oFinlinV, BigDecimal oFinlvl, String nInitinV, String nInitvS,
		String nFinlinV, String nFinlvlS, String propvlS) {
		//1、类型转换
		BigDecimal nFinlvl = new BigDecimal(0.0);
		BigDecimal nInitv = new BigDecimal(0.0);
		BigDecimal propvl = new BigDecimal(0.0);
		try {
			if (CommUtil.isNotNull(propvlS)) {
				propvl = new BigDecimal(propvlS);// 转换BigDecimal类型
			}
			if (CommUtil.isNotNull(nInitvS)) {
				nInitv = new BigDecimal(nInitvS);// 转换BigDecimal类型
			}
			if (CommUtil.isNotNull(nFinlvlS)) {
				nFinlvl = new BigDecimal(nFinlvlS);// 转换BigDecimal类型
			}
		} catch (Exception e) {
			throw SlError.SlComm.E0001("浮点类型转换异常，请确认属性值类型是否正确！");
		}
		//2、开始起始值不包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//2.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}else {
				//包含（x < i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oInitv)==0)||(nInitv.compareTo(oFinlvl)==1)||(nInitv.compareTo(oFinlvl)==0)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
			//2.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
				
			}else {
				//包含（x < f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oInitv)==0)||(nFinlvl.compareTo(oFinlvl)==1)||(nFinlvl.compareTo(oFinlvl)==0)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
		}
		//3、开始起始值包含、开始终止值不包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.NO.getValue())) {
			//3.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
				
			}else {
				//包含（x <= i < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oFinlvl)==1)||(nInitv.compareTo(oFinlvl)==0)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
			//3.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}else {
				//包含（x <= f < y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oFinlvl)==1)||(nFinlvl.compareTo(oFinlvl)==0)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
		}
		//4、开始起始值不包含、开始终止值包含
		if (CommUtil.equals(oInitinV,E_INITIN.NO.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//4.1、判断输入起始值是否包含
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				//不包含（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}else {
				//包含（x < i <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oInitv)==0)||(nInitv.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
			//4.2、判断输入终止值是否包含
			if (CommUtil.equals(nFinlinV, E_INITIN.NO.getValue())) {
				//不包含（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}else {
				//包含（x < f <= y）	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
				if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oInitv)==0)||(nFinlvl.compareTo(oFinlvl)==1)){
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
		}
		//5、开始起始值包含、开始起始值包含
		if (CommUtil.equals(oInitinV,E_INITIN.YES.getValue())&&CommUtil.equals(oFinlinV, E_FINLIN.YES.getValue())) {
			//输入起始值（x <= i <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if((nInitv.compareTo(oInitv)==-1)||(nInitv.compareTo(oFinlvl)==1)){
				throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
			}
			//输入终止值（x <= f <= y）	 	注：x、i、f、y分别为开始起始值、输入起始值、输入终止值、开始终止值
			if((nFinlvl.compareTo(oInitv)==-1)||(nFinlvl.compareTo(oFinlvl)==1)){
				throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
			}
			
		}
		//6、判断输入起始值与输入终止值
		if(nInitv.compareTo(nFinlvl)==1){
			throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
		}
		
		//7、判断输入属性缺省值
		if (CommUtil.isNotNull(propvlS)) {
			if((nInitv.compareTo(propvl)==1)||(nFinlvl.compareTo(propvl)==-1)){
				throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
			}
			if (CommUtil.equals(nInitinV, E_INITIN.NO.getValue())) {
				if (nInitv.compareTo(propvl)==0) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
			if (CommUtil.equals(oFinlinV, E_INITIN.NO.getValue())) {
				if (nFinlvl.compareTo(propvl)==0) {
					throw SlError.SlComm.E0001("属性值起始值输入有误(浮点类型)！");
				}
			}
		}
		
	}
	/**
	 * 
	 * @Title: propValueV 
	 * @Description: 属性值是否在起始值和终止值范围之类
	 * @param initin 起始包含
	 * @param initvl 起始值
	 * @param inlin  终止包含
	 * @param finlvl 终止值
	 * @param propvl 默认值
	 * @param flag 标识
	 * @author baifangping
	 * @date 2016年8月17日 上午11:00:40 
	 * @version V2.3.0
	 * @param flag2 
	 */
	public static void propValueV(String initin, String initvl, String finlin,String finlvl, String propvl, String flag) {
		//根据flag区分比较的类型
		//整型
		if(CommUtil.equals(flag,"intr")){
			//1、类型转化String——>Long
			long propvlv = 0;
			long initvlv = 0;
			long finlvlv = 0;
			try {
				if (CommUtil.isNotNull(propvl)) {
					//propvlv = Long.parseLong(propvl);// 转换LONG类型
					//董志宇-20170917-上述转换若核心传递的是0.00，则转换出现异常
					propvlv = new BigDecimal(propvl).longValue();//转换LONG类型
				}
				if (CommUtil.isNotNull(initvl)) {
					//initvlv = Long.parseLong(initvl);// 转换LONG类型
					//董志宇-20170917-上述转换若核心传递的是0.00，则转换出现异常
					initvlv = new BigDecimal(initvl).longValue();//转换LONG类型
				}
				if (CommUtil.isNotNull(finlvl)) {
					//finlvlv = Long.parseLong(finlvl);// 转换LONG类型
					//董志宇-20170917-上述转换若核心传递的是0.00，则转换出现异常
					finlvlv = new BigDecimal(finlvl).longValue();//转换LONG类型
				}
			} catch (Exception e) {
				throw SlError.SlComm.E0001("类型转换异常，请确认属性值类型是否正确！");
			}
			//2、起始值包含
			if(CommUtil.equals(initin, E_INITIN.YES.getValue())){
				
				if (CommUtil.equals(finlin, E_INITIN.YES.getValue())) {
					//终止值包含
					if (initvlv > finlvlv) {
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}else {
					//终止值不包含
					if(initvlv >= finlvlv){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}
			}else {
				if (initvlv > finlvlv) {
					throw SlError.SlComm.E0001("属性值输入有误！");
				}
			}
			//3、判断输入属性缺省值
			if (CommUtil.isNotNull(propvl) && !CommUtil.equals(propvl, "0")) {
				if ((propvlv<initvlv)||(propvlv>finlvlv)) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
				if (CommUtil.equals(initin, E_INITIN.NO.getValue())) {
					if (propvlv == initvlv) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
				if (CommUtil.equals(finlin, E_INITIN.NO.getValue())) {
					if (propvlv == finlvlv) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
			}
			
		}
		
		//浮点型
		if(CommUtil.equals(flag,"decl")){
			
			//1、类型转化
			BigDecimal finlvlv = new BigDecimal(0.0);
			BigDecimal initvlv = new BigDecimal(0.0);
			BigDecimal propvlv = new BigDecimal(0.0);
			
			try {
				if (CommUtil.isNotNull(propvl)) {
					propvlv = new BigDecimal(propvl);// 转换BigDecimal类型
				}
				if (CommUtil.isNotNull(initvl)) {
					initvlv = new BigDecimal(initvl);// 转换BigDecimal类型
				}
				if (CommUtil.isNotNull(finlvl)) {
					finlvlv = new BigDecimal(finlvl);// 转换BigDecimal类型
				}
			} catch (Exception e) {
				throw SlError.SlComm.E0001("浮点类型转换异常，请确认属性值类型是否正确！");
			}
			//2、起始值包含
			if(CommUtil.equals(initin, E_INITIN.YES.getValue())){
				
				if (CommUtil.equals(finlin, E_INITIN.YES.getValue())) {
					//终止值包含
					if (initvlv.compareTo(finlvlv) == 1) {
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}else {
					//终止值不包含
					if((initvlv.compareTo(finlvlv) == 1)||(initvlv.compareTo(finlvlv) == 0)){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}
			}else {
				if (initvlv.compareTo(finlvlv) == 1) {
					throw SlError.SlComm.E0001("属性值输入有误！");
				}
				
			}
			
			//3、判断输入属性缺省值
			if (CommUtil.isNotNull(propvl) && !CommUtil.equals(new BigDecimal(propvl), BigDecimal.ZERO)) {
				if ((initvlv.compareTo(propvlv) == 1)||(finlvlv.compareTo(propvlv) == -1)) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
				if (CommUtil.equals(initin, E_INITIN.NO.getValue())) {
					if (initvlv.compareTo(propvlv) == 0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
				if (CommUtil.equals(finlin, E_INITIN.NO.getValue())) {
					if (finlvlv.compareTo(propvlv) == 0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
			}
		}
		
		//日期类型
		if(CommUtil.equals(flag,"date")){

			//1、类型装换String->date
			Date initvlv = SlTools.SqlStringToDate2(initvl);
			Date finlvlv = SlTools.SqlStringToDate2(finlvl);
			
			
			//2、起始值包含
			if(CommUtil.equals(initin, E_INITIN.YES.getValue())){
				
				if (CommUtil.equals(finlin, E_INITIN.YES.getValue())) {
					//终止值包含
					if (DateTools2.compareDate(initvlv,finlvlv)==1) {
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}else {
					//终止值不包含
					if((DateTools2.compareDate(initvlv,finlvlv)==1)||(DateTools2.compareDate(initvlv,finlvlv)==0)){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}
			}else {
				if (DateTools2.compareDate(initvlv,finlvlv)==1) {
					throw SlError.SlComm.E0001("属性值输入有误！");
				}
			}
			
			//3、判断输入属性缺省值
			if (CommUtil.isNotNull(propvl)) {
				Date propvlv = SlTools.SqlStringToDate2(propvl);
				if ((DateTools2.compareDate(initvlv,propvlv)==1)||(DateTools2.compareDate(finlvlv,propvlv)==-1)) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
				if (CommUtil.equals(initin, E_INITIN.NO.getValue())) {
					if (DateTools2.compareDate(initvlv,propvlv)==0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
				if (CommUtil.equals(finlin, E_INITIN.NO.getValue())) {
					if (DateTools2.compareDate(finlvlv,propvlv)==0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
			}
			
		}
		
		//时间类型
		if(CommUtil.equals(flag,"time")){
			//1、起始值包含
			if(CommUtil.equals(initin, E_INITIN.YES.getValue())){
				
				if (CommUtil.equals(finlin, E_INITIN.YES.getValue())) {
					//终止值包含
					if (SlTools.compareTimeN(initvl,finlvl)== 1){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}else {
					//终止值不包含
					if((SlTools.compareTimeN(initvl,finlvl)== 1)||(SlTools.compareTimeN(initvl,finlvl)== 0)){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}
			}else {
				if (SlTools.compareTimeN(initvl,finlvl)== 1) {
					throw SlError.SlComm.E0001("属性值输入有误！");
				}
			}
			
			//2、判断输入属性缺省值
			if (CommUtil.isNotNull(propvl)) {
				if ((SlTools.compareTimeN(initvl,propvl)== 1)||(SlTools.compareTimeN(finlvl,propvl)== -1)) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
				if (CommUtil.equals(initin, E_INITIN.NO.getValue())) {
					if (SlTools.compareTimeN(initvl,propvl)== 0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
				if (CommUtil.equals(finlin, E_INITIN.NO.getValue())) {
					if (SlTools.compareTimeN(finlvl,propvl)==0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
			}
			
		}
		
		//时间戳类型
		if(CommUtil.equals(flag,"datm")){
			
			//1、起始值包含
			if(CommUtil.equals(initin, E_INITIN.YES.getValue())){
				
				if (CommUtil.equals(finlin, E_INITIN.YES.getValue())) {
					//终止值包含
					if (SlTools.compareTimeDatm(initvl,finlvl)== 1){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}else {
					//终止值不包含
					if((SlTools.compareTimeDatm(initvl,finlvl)== 1)||(SlTools.compareTimeDatm(initvl,finlvl)== 0)){
						throw SlError.SlComm.E0001("属性值输入有误！");
					}
				}
			}else {
				if (SlTools.compareTimeDatm(initvl,finlvl)== 1) {
					throw SlError.SlComm.E0001("属性值输入有误！");
				}
			}
			
			//2、判断输入属性缺省值
			if (CommUtil.isNotNull(propvl)) {
				if ((SlTools.compareTimeDatm(initvl,propvl)== 1)||(SlTools.compareTimeDatm(finlvl,propvl)== -1)) {
					throw SlError.SlComm.E0001("属性缺省值输入有误！");
				}
				if (CommUtil.equals(initin, E_INITIN.NO.getValue())) {
					if (SlTools.compareTimeDatm(initvl,propvl)== 0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
				if (CommUtil.equals(finlin, E_INITIN.NO.getValue())) {
					if (SlTools.compareTimeDatm(finlvl,propvl)== 0) {
						throw SlError.SlComm.E0001("属性缺省值输入有误！");
					}
				}
			}
			
		}
	}
	/**
	 * 
	 * @Title: compareData 
	 * @Description: 两个日期的比较 
	 * @param oneToD
	 * @param twoToD
	 * @param flag
	 * @author baifangping
	 * @date 2016年8月28日 上午9:02:53 
	 * @version V2.3.0
	 */
	public static void compareData(String oneToD, String twoToD, String flag) {
		//flag = "TDate" ,比较两个日期 ； flag = "TTime" ， 比较两个时间
		//日期比较
		if (CommUtil.equals(flag, "TDate")) {
			//1、类型装换String->date
			Date oneDate = SlTools.SqlStringToDate2(oneToD);
			Date twoDate =SlTools.SqlStringToDate2(twoToD);
			//如果oneDate > twoDate ，提示输入不合法
			if(DateTools2.compareDate(oneDate,twoDate)==1){
				throw SlError.SlComm.E0001("两个日期大小输入有误！");
			}
		}
		//时间比较
		if (CommUtil.equals(flag, "TTime")) {
			if (SlTools.compareTimeN(oneToD,twoToD)== 1) {
				throw SlError.SlComm.E0001("两个时间大小输入有误！");
			}
		}
	}
	
	/**
	 * 检验日期格式及格式化YYYY-mm-dd
	 * @author chengyinghao
	 * @param date
	 * @return
	 */
	public static String genNewDateString(String date){
		if(CommUtil.isNotNull(date)){
			if(!date.matches("^.{4}[-].{2}[-].{2}$")){
				if(date.length() != 8){
					throw SlError.SlComm.E0001("日期长度输入有误应该为8位！");
				}
				date = date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8);
			}
			
			return date;
		}	
		
		return "";
	}
	
	/**
	 * 格式化日期YYYY-mm-dd
	 * @author chengyinghao
	 * @param date
	 * @return
	 */
	public static String SqlDateToString(java.sql.Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(date);
	}
	
	/**
	 * 格式化时间 HH:mm:ss
	 * @author chengyinghao
	 * @param time
	 * @return
	 */
	public static String genNewTimeString(String time){
		if(CommUtil.isNotNull(time)){
			if(!time.matches("^.{2}[:].{2}[:].{2}")){
				if(time.length() != 6){
					throw SlError.SlComm.E0001("时间长度输入有误应该为8位！");
				}
				
				time = time.substring(0, 2) + ":" + time.substring(2, 4) + ":" +time.substring(4, 6);
			}
			return time;
		}
		
		return "";
	}
	
	/**
	 * 格式化时间
	 * @author chengyinghao
	 * @param time
	 * @return
	 */
	public static String SqlTimeToString3(java.sql.Time time) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(time);
	}
	/**
	 * 格式换时间戳
	 * @param stamptime
	 * @return
	 */
	public static String genNewStampTimeString(String stamptime){
		if(CommUtil.isNotNull(stamptime)){
			if(!stamptime.matches("^.{4}[-].{2}[-] .{2}.{2}[:].{2}[:].{2}$")){
				if(stamptime.length() != 14){
					throw SlError.SlComm.E0001("时间长度输入有误应该为14位！");
				}
				
				stamptime = stamptime.substring(0, 4) + "-" + stamptime.substring(4, 6) + "-" +stamptime.substring(6, 8) 
						+ " " + stamptime.substring(8, 10) + ":" + stamptime.substring(10, 12) + ":" +stamptime.substring(12, 14) ;
			}
			return stamptime;
		}
		
		return "";
	}
	/**
	 * 
	 * @Title: judgePropvlCommpare 
	 * @Description: 工厂属性与同步属性属性值的比对 
	 * @param propid  属性id
	 * @param prvltp  属性类型
	 * @param initvlTb 同步属性起始值
	 * @param finlvlTb 同步属性终止值
	 * @param propvlTb 同步属性值
	 * @param initvlDb	工厂属性起始值
	 * @param finlvlDb	工厂属性终止值
	 * @param propvlDb	工厂属性值
	 * @author baifangping
	 * @date 2016年11月25日 下午4:22:03 
	 * @version V2.3.0
	 */
	public static void judgePropvlCommpare(String propid,String prvltp, String initvlTb,
			String finlvlTb, String propvlTb, String initvlDb, String finlvlDb,
			String propvlDb ) {
		/**
		 * 比对逻辑：先判断同步属性的起始值，为空，则仅比对属性值（propvlTb、propvlDb）是否相等
		 *			 不为空，则判断属性起始值（initvlTb、initvlDb）、终止值（finlvlTb、finlvlDb）、
		 * 			属性值（propvlTb、propvlDb）是否相等
		 * 命名规则：Tb指同步属性、Db指工厂属性
		 * 比对思想：对浮点型属性以数值形式做比对 ； 对其它（列表值除外）属性以字符串方式比对
		 */
		//定义变量
		BigDecimal ppvl1 = new BigDecimal(0.0);
		BigDecimal ppvl2 = new BigDecimal(0.0);
		BigDecimal initvl1 = new BigDecimal(0.0);
		BigDecimal finlvl1 = new BigDecimal(0.0);
		BigDecimal initvl2 = new BigDecimal(0.0);
		BigDecimal finlvl2 = new BigDecimal(0.0);
		
		String ppv1 = "";
		String ppv2 = "";
		String ini1 = "";
		String fin1 = "";
		String ini2 = "";
		String fin2 = "";
		
		BigDecimal propvl1 = new BigDecimal(0.0);
		BigDecimal propvl2 = new BigDecimal(0.0);
		
		String pvl1 = "";
		String pvl2 = "";
		
		/*
		 * 判断同步属性起始值是否为空
		 */
		if (CommUtil.isNotNull(initvlTb)) {
			/*
			 * A.起始值、终止值不为空，比较起始值、终止值、属性值的一致性
			 */
			if (CommUtil.isNull(finlvlTb)) {
				throw SlError.SlComm.E0001("同步数据存储出错，在起始值有值的情况下，终止值为空了！");
			}
			
			//与销售工厂属性起始值、终止值做比对的属性，属性值类型不是字符串、按钮和列表类型
			if (CommUtil.equals(prvltp,
					E_PRVLTP.BTN.getValue())
					|| CommUtil.equals(
							prvltp,
							E_PRVLTP.STR.getValue())) {
				throw SlError.SlComm.E0001("属性" + propid
						+ "为按钮或字符串类型，起始值、终止值字段数据应为空（属性映射表）！");
			}
			
			//二位小数、五位小数、百分比属性数值比对
			if (CommUtil.equals(prvltp, E_PRVLTP.DE2.getValue())
					||CommUtil.equals(prvltp, E_PRVLTP.DE5.getValue())
					||CommUtil.equals(prvltp, E_PRVLTP.PER.getValue())){
				//不为空，赋值
				if (CommUtil.isNotNull(propvlTb)) {
					ppvl1 = new BigDecimal(propvlTb);
				}
				if (CommUtil.isNotNull(initvlTb)) {
					initvl1 = new BigDecimal(initvlTb);
				}
				if(CommUtil.isNotNull(finlvlTb)){
					finlvl1 = new BigDecimal(finlvlTb);
				}
				
				if (CommUtil.isNotNull(propvlDb)) {
					ppvl2 = new BigDecimal(propvlDb);
				}
				if (CommUtil.isNotNull(initvlDb)) {
					initvl2 = new BigDecimal(initvlDb);
				}
				if (CommUtil.isNotNull(finlvlDb)) {
					finlvl2 = new BigDecimal(finlvlDb);
				}
				
				if (ppvl1.compareTo(ppvl2)!= 0) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的值与工厂中的不一致("+ppvl1+"!="+ppvl2+")");
				}
				
				if (initvl1.compareTo(initvl2)!= 0) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的起始值与工厂中的不一致("+initvl1+"!="+initvl2+")");
				}
				if (finlvl1.compareTo(finlvl2)!= 0) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的终止值与工厂中的不一致("+finlvl1+"!="+finlvl2+")");
				}
				
			}else{
				/*
				 * 其它类型，时间、日期、时间戳格式化，
				 * 分别将同步的数据格式转化为HH:mm:ss\yyyy-MM-dd\yyyy-MM-dd HH:mm:ss
				 */
				
				if (CommUtil.equals(prvltp, E_PRVLTP.TIM.getValue())) {
					/*
					 * 时间类型
					 */
					if (CommUtil.isNotNull(propvlTb)) {
						ppv1 = SlTools.SqlTimeToString2(SlTools.SqlStringToTime2(propvlTb));
					}
					if (CommUtil.isNotNull(initvlTb)) {
						ini1 = SlTools.SqlTimeToString2(SlTools.SqlStringToTime2(initvlTb));
					}
					if (CommUtil.isNotNull(finlvlTb)) {
						fin1 = SlTools.SqlTimeToString2(SlTools.SqlStringToTime2(finlvlTb));
					}
					
					if (CommUtil.isNotNull(propvlDb)) {
						ppv2 = propvlDb;
					}
					if (CommUtil.isNotNull(initvlDb)) {
						ini2 = initvlDb;
					}
					if (CommUtil.isNotNull(finlvlDb)) {
						fin2 = finlvlDb;
					}
					
				}else if (CommUtil.equals(prvltp, E_PRVLTP.DAT.getValue())) {
					
					/*
					 * 日期格式
					 */
					if (CommUtil.isNotNull(propvlTb)) {
						ppv1 = PropValidate.SqlDateToString(SlTools.SqlStringToDate(propvlTb));
					}
					if (CommUtil.isNotNull(initvlTb)) {
						ini1 = PropValidate.SqlDateToString(SlTools.SqlStringToDate(initvlTb));
					}
					if (CommUtil.isNotNull(finlvlTb)) {
						fin1 = PropValidate.SqlDateToString(SlTools.SqlStringToDate(finlvlTb));
					}
					
					if (CommUtil.isNotNull(propvlDb)) {
						ppv2 = propvlDb;
					}
					if (CommUtil.isNotNull(initvlDb)) {
						ini2 = initvlDb;
					}
					if (CommUtil.isNotNull(finlvlDb)) {
						fin2 = finlvlDb;
					}
				}else if (CommUtil.equals(prvltp, E_PRVLTP.DAM.getValue())) {
					
					/*
					 * 时间戳
					 */
					if (CommUtil.isNotNull(propvlTb)) {
						ppv1 = SlTools.SqlTimestampToString(SlTools.SqlStringToTimestamp2(propvlTb));
					}
					if (CommUtil.isNotNull(initvlTb)) {
						ini1 = SlTools.SqlTimestampToString(SlTools.SqlStringToTimestamp2(initvlTb));
					}
					if (CommUtil.isNotNull(finlvlTb)) {
						fin1 = SlTools.SqlTimestampToString(SlTools.SqlStringToTimestamp2(finlvlTb));
					}
					if (CommUtil.isNotNull(propvlDb)) {
						ppv2 = propvlDb;
					}
					if (CommUtil.isNotNull(initvlDb)) {
						ini2 = initvlDb;
					}
					if (CommUtil.isNotNull(finlvlDb)) {
						fin2 = finlvlDb;
					}
				}else {
					/*
					 * 其它类型
					 */
					
					if (CommUtil.isNotNull(propvlTb)) {
						ppv1 = propvlTb;
					}
					if (CommUtil.isNotNull(initvlTb)) {
						ini1 = initvlTb;
					}
					if (CommUtil.isNotNull(finlvlTb)) {
						fin1 = finlvlTb;
					}
					
					if (CommUtil.isNotNull(propvlDb)) {
						ppv2 = propvlDb;
					}
					if (CommUtil.isNotNull(initvlDb)) {
						ini2 = initvlDb;
					}
					if (CommUtil.isNotNull(finlvlDb)) {
						fin2 = finlvlDb;
					}
				}
				/*
				 * 比较是否相等（字符串比较方式）
				 */
				if (!CommUtil.equals(ppv1, ppv2)) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的值与工厂中的不一致("+ppv1+"!="+ppv2+")");
				}
				if (!CommUtil.equals(ini1, ini2)) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的起始值与工厂中的不一致("+ini1+"!="+ini2+")");
				}
				if (!CommUtil.equals(fin1, fin2)) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的终止值与工厂中的不一致("+fin1+"!="+fin2+")");
				}
			} 
		} else {
			/*
			 * B.起始值、终止值为空，比较属性值是否一致
			 */
			if (CommUtil.isNotNull(finlvlTb)) {
				throw SlError.SlComm.E0001("同步数据存储出错，在起始值为空的情况下，终止值有值！");
			}
			
			//数据格式化
			if (CommUtil.equals(prvltp, E_PRVLTP.DE2.getValue())
					||CommUtil.equals(prvltp, E_PRVLTP.DE5.getValue())
					||CommUtil.equals(prvltp, E_PRVLTP.PER.getValue())){
				/*
				 * 二位小数、四位小数、百分比数值比较
				 */
				if (CommUtil.isNotNull(propvlTb)) {
					propvl1 = new BigDecimal(propvlTb);
				}
				if(CommUtil.isNotNull(propvlDb)){
					propvl2 = new BigDecimal(propvlDb);
				}
				if (propvl1.compareTo(propvl2)!= 0) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的值与工厂中的不一致("+propvl1+"!="+propvl2+")");
				}
				
			}else{
				//时间类型格式化
				if (CommUtil.equals(prvltp, E_PRVLTP.TIM.getValue())) {
					
					if (CommUtil.isNotNull(propvlTb)) {
						pvl1 = SlTools.SqlTimeToString2(SlTools.SqlStringToTime2(propvlTb));
					}
					if (CommUtil.isNotNull(propvlDb)) {
						pvl2 = propvlDb;
					}
					
				}else if (CommUtil.equals(prvltp, E_PRVLTP.DAT.getValue())) {
					//日期类型格式化
					if (CommUtil.isNotNull(propvlTb)) {
						pvl1 = PropValidate.SqlDateToString(SlTools.SqlStringToDate(propvlTb));
					}
					if (CommUtil.isNotNull(propvlDb)) {
						pvl2 = propvlDb;
					}
				}else if (CommUtil.equals(prvltp, E_PRVLTP.DAM.getValue())) {
					//时间戳类型格式化
					if (CommUtil.isNotNull(propvlTb)) {
						pvl1 = SlTools.SqlTimestampToString(SlTools.SqlStringToTimestamp2(propvlTb));
					}
					if (CommUtil.isNotNull(propvlDb)) {
						pvl2 = propvlDb;
					}
					
				}else {
					//其他类型
					if (CommUtil.isNotNull(propvlTb)) {
						pvl1 = propvlTb;
					}
					if (CommUtil.isNotNull(propvlDb)) {
						pvl2 = propvlDb;
					}
				}
				/*
				 * 属性值一致性比较
				 */
				if (!CommUtil.equals(pvl1, pvl2)) {
					throw SlError.SlComm.E0001("属性" + propid
							+ "同步的值与工厂中的不一致("+pvl1+"!="+pvl2+")");
				}
			} 
		}
	}
}
