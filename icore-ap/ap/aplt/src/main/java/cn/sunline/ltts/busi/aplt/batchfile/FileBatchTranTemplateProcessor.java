package cn.sunline.ltts.busi.aplt.batchfile;

import java.math.BigDecimal;

import cn.sunline.edsp.base.util.convert.ConvertUtil;
import cn.sunline.edsp.base.util.lang.StringUtil;
import cn.sunline.adp.metadata.base.util.CommUtil_;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.cedar.server.batch.engine.config.BatchEngineConfigManager;
import cn.sunline.adp.cedar.server.batch.errors.EngineBatchPluginErrorDef;
import cn.sunline.adp.cedar.server.batch.file.FileBatchUtil;
import cn.sunline.adp.cedar.server.batch.model.file.FileBatchTransactionConf;
import cn.sunline.adp.cedar.server.batch.model.file.FileContent;
import cn.sunline.adp.cedar.server.batch.model.file.FileField;
import cn.sunline.adp.cedar.server.batch.model.file.FileTemplateConf;
import cn.sunline.adp.cedar.server.batch.type.BatchEnum;
import cn.sunline.adp.core.bean.accessor.PropertyAccessor;
import cn.sunline.adp.metadata.model.Element;
import cn.sunline.adp.metadata.model.RestrictionType;
import cn.sunline.adp.metadata.model.util.ModelUtil;

/**
 * 文件批量交易模板处理
 * @author jizhirong
 */

public class FileBatchTranTemplateProcessor {
	
	private FileTemplateConf fileTemplateConf;
	public FileBatchTranTemplateProcessor(String tranid){
		//根据交易码初始化 对应的文件批量模板
		 FileBatchTransactionConf fileBatchTransactionConf = BatchEngineConfigManager.get().getFileBatchTransactionConf(tranid);
		 this.fileTemplateConf = fileBatchTransactionConf.getFileTemplate();
	}
	
	 /**
	  * 将模板映射的一条文件体实例，解析成一行文件体文本信息
	  * @param tranid 文件批量交易码
	  * @param obj 文件批量映射实体
	  * @return 拼接的出一行 文本
	  */
	 public <T> String getLineStrOfFileBody(T body){
		 return getLineStr(fileTemplateConf.getBody(),body,fileTemplateConf.getMode(),fileTemplateConf.getSplitor(),fileTemplateConf.isSplitorAsEnd());
	 }
	 /**
	  * 将模板映射的一条文件头实例，解析成一行文件头文本信息
	  * @param tranid 文件批量交易码
	  * @param obj 文件批量映射实体
	  * @return 拼接的出一行文本
	  */
	 public <T> String getLineStrOfFileHead(T head){
		 return getLineStr(fileTemplateConf.getHeader(),head,fileTemplateConf.getMode(),fileTemplateConf.getSplitor(),fileTemplateConf.isSplitorAsEnd());
	 }
	 
	 /**
	  * 通过文件一行字符信息，解析出模板文件体映射的一条实例信息
	  * @param line 从文件中解析的一行文本信息
	  * @return 文件体映射的实例信息
	  */
	 public <T> T getBodyOfLine(String line){
		 T t = FileBatchUtil.parseLine(fileTemplateConf.getBody(), line, fileTemplateConf.getMode(), fileTemplateConf.getSplitor(), fileTemplateConf.getEncoding(), fileTemplateConf.isSplitorAsEnd());
		 return t;
	 }
	 /**
	  * 通过文件一行字符信息，解析出模板文件头映射的一条实例信息
	  * @param line 从文件中解析的一行文本信息
	  * @return 文件头映射的实例信息
	  */
	 public <T> T getHeadOfLine(String line){
		 T t = FileBatchUtil.parseLine(fileTemplateConf.getHeader(), line, fileTemplateConf.getMode(), fileTemplateConf.getSplitor(), fileTemplateConf.getEncoding(), fileTemplateConf.isSplitorAsEnd());
		 return t;
	 }
	 
	 /**
	  * 根据模板解析出一行文本
	  * @param content
	  * @param obj
	  * @param mode
	  * @param splitor
	  * @param isSplitorAsEnd
	  * @return
	  */
	 private <T> String getLineStr(FileContent content, T obj,
			BatchEnum.E_FILEMODE mode, String splitor, boolean isSplitorAsEnd) {
		if ((content == null) || (content.getAllElements().size() < 1)) {
			throw EngineBatchPluginErrorDef.SP_EB.E028();
		}
		StringBuffer sb = new StringBuffer();
		PropertyAccessor accessor = PropertyUtil.createAccessor(obj);
		for (Element element : content.getAllElements()) {
			String value = accessor.getNestedProperty(element.getId()) == null ? "" : accessor.getNestedProperty(element.getId()).toString();
			Object eleValue = ConvertUtil.convert(value, element.getElementJavaClass());
			if ((eleValue instanceof BigDecimal)) {
				value = ((BigDecimal) eleValue).toPlainString();
			}
			sb.append(getFieldStr((FileField) element, value, mode, splitor));
		}
		String ret = sb.toString();
		if (((mode == BatchEnum.E_FILEMODE.splitor) || (mode == BatchEnum.E_FILEMODE.fixAndSplitor)) && (ret.endsWith(splitor)) && (!isSplitorAsEnd)) {
			ret = ret.substring(0, ret.lastIndexOf(splitor));
		}
		if (StringUtil.isNotEmpty(content.getPrefix())) {
			ret = content.getPrefix() + ret;
		}
		if (StringUtil.isNotEmpty(content.getSuffix())) {
			ret = ret + content.getSuffix();
		}
		return ret;
	 }
	 /**
	  * 模板与实体字段映射处理
	  * @param field
	  * @param value
	  * @param mode
	  * @param splitor
	  * @return
	  */
	 private String getFieldStr(FileField field, String value,
			BatchEnum.E_FILEMODE mode, String splitor) {
		if (BatchEnum.E_FILEMODE.fix == mode) {
			int length = field.getLength() == null ? 0 : field.getLength().intValue();
			if (length == 0) {
				RestrictionType rt = ModelUtil.getRestrictionType(field.getTypeObj());
				if (rt == null) {
					throw EngineBatchPluginErrorDef.SP_EB.E046(field.getId());
				}
				length = rt.getMaxLength() == null ? 0 : rt.getMaxLength().intValue();
				if (length == 0) {
					throw EngineBatchPluginErrorDef.SP_EB.E047(field.getId());
				}
			}
			if (CommUtil.isNull(field.getPadding())) {
				throw EngineBatchPluginErrorDef.SP_EB.E045(field.getId());
			}
			boolean lefPadding = field.getLeftPadding().booleanValue();
			if (lefPadding) {
				value = CommUtil_.lpad(value, length, field.getPadding(),
						"UTF-8");
			} else {
				value = CommUtil_.rpad(value, length, field.getPadding(),
						"UTF-8");
			}
		} else if (BatchEnum.E_FILEMODE.splitor == mode) {
			value = value + splitor;
		} else if (BatchEnum.E_FILEMODE.fixAndSplitor == mode) {
			value = getFieldStr(field, value, BatchEnum.E_FILEMODE.fix, splitor);
			value = getFieldStr(field, value, BatchEnum.E_FILEMODE.splitor,splitor);
		}
		return value;
	}

}
