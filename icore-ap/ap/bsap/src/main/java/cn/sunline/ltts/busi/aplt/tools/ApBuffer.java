package cn.sunline.ltts.busi.aplt.tools;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.ltts.busi.sys.errors.ApError;

/**
 * <p>
 * 文件功能说明：交易缓冲区处理
 * </p>
 * 
 * @Author lid
 *         <p>
 *         <li>2016年12月12日-上午11:51:55</li>
 *         <li>修改记录</li>
 *         <li>-----------------------------------------------------------</li>
 *         <li>标记：修订内容</li>
 *         <li>20140228 lid：创建注释模板</li>
 *         <li>-----------------------------------------------------------</li>
 *         </p>
 */
public class ApBuffer {

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2016年12月12日-上午11:52:59</li>
	 *         <li>功能说明：清空数据缓冲区的所有数据集</li>
	 *         </p>
	 */
	public static void clear() {
		Map<String, Object> currentLayerBuffer = getBuffer();
		currentLayerBuffer.clear();
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2016年12月12日-上午11:54:58</li>
	 *         <li>功能说明：将某个数据集加到数据缓冲区。（采用先删除再增加的方式）</li>
	 *         </p>
	 * @param dataMart
	 * @param commObj
	 */
	public static void addData(String dataMart, Map<String, Object> commObj) {
		addData(dataMart, commObj, false);
	}

	/**
	 * @Author lid
	 *         <p>
	 *         <li>2016年03月20日-上午09:54:58</li>
	 *         <li>功能说明：往某个数据集追加数据。</li>
	 *         </p>
	 * @param dataMart
	 * @param commObj
	 */
	public static void appendData(String dataMart, Map<String, Object> commObj) {
		addData(dataMart, commObj, true);
	}

	@SuppressWarnings("unchecked")
	private static void addData(String dataMart, Map<String, Object> commObj, boolean appendFlag) {

		//ApDropList.exists(ApConst.DATA_MART, dataMart);
		//RunEnvs runEnvs = BaseTools.prcRunEnvs();

		Map<String, Object> ruleBuffer = getBuffer();
	
		// 追加
		if (appendFlag) {
			Object data = ruleBuffer.get(dataMart);
			if (data == null) {
				ruleBuffer.put(dataMart, commObj);
			}else{
				if(data instanceof Map){
					Map<String,Object> dataMap = (Map<String,Object>) data;
					for(String key : commObj.keySet()){
						// 有不一样的key 才添加
						if(!dataMap.containsKey(key)){ 
							dataMap.put(key, commObj.get(key));
						}
					}
				}
			}

		}
		else {
			ruleBuffer.remove(dataMart);

			ruleBuffer.put(dataMart, commObj);
		}
	}
	
	/**
	 * @Author tsichang
	 *         <p>
	 *         <li>2017年3月9日-下午4:51:31</li>
	 *         <li>功能说明：获取缓冲区某个字段的值 </li>
	 *         </p>
	 * @param dataMart
	 * @param fieldName
	 * @return String
	 */
	@SuppressWarnings("unchecked")
	public static String getFieldValue(String dataMart, String fieldName) {
		
		Map<String, Object> ruleBuffer = getBuffer();
		Object value = null;
		
		// 如果是公共运行变量，直接去公共运行变量去取
		if (ApConstants.RUN_ENVS.equals(dataMart)) {
			value = CommTools.getTrxRunEnvsValue(fieldName);
		}
		else {// 其他的就从缓冲区取
			Object dataObj = ruleBuffer.get(dataMart);// 数据集
			if (dataObj == null) {
				throw ApError.Aplt.E0056(dataMart);
			}

			if (dataObj instanceof Map) {
				Map<String, Object> data = (Map<String, Object>) dataObj;
				value = data.get(fieldName);
			}
		}
		
		if (CommUtil.isNull(value)) {
			throw ApError.Aplt.E0057(dataMart, fieldName);
		}

		return value.toString();
	}	
	
	/**
	 * 
	 * @Author tsichang
	 *         <p>
	 *         <li>2017年4月25日-上午9:56:15</li>
	 *         <li>功能说明：获取当前的缓存区</li>
	 *         </p>
	 * @return
	 */
	@SuppressWarnings({ "unchecked" })
	public static Map<String, Object> getBuffer() {
		Object bufferObj = CommTools.prcRunEnvs().getRulebf();
		
		Stack<Map<String, Object>> bufferStack = null;
		if (bufferObj == null) {
			bufferStack = new Stack<Map<String, Object>>();
			CommTools.prcRunEnvs().setRulebf(bufferStack);
		} else {
			bufferStack = (Stack<Map<String, Object>>)bufferObj;
		}
		
		if (bufferStack.size() == 0) { // 如果栈底服务是直接通过调用服务API，而不是通过"服务类型执行器"调用的，则需要初始化压入第一层缓存。
			Map<String, Object> currentBuffer = new HashMap<String, Object>();
			bufferStack.push(currentBuffer);
			
			return currentBuffer;
		} else {
			return bufferStack.peek();
		}
		
	}
	
}
