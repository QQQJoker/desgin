package cn.sunline.ltts.busi.aplt.tools;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cn.sunline.adp.metadata.base.odb.OdbFactory;
import cn.sunline.adp.metadata.base.util.PropertyUtil;
import cn.sunline.adp.cedar.base.util.CommUtil;
import cn.sunline.adp.core.bean.accessor.PropertyAccessor;
import cn.sunline.adp.metadata.model.SimpleType;
import cn.sunline.adp.metadata.model.database.Field;
import cn.sunline.adp.metadata.model.database.Table;
import cn.sunline.adp.metadata.model.util.ModelUtil;

/**
 *@author wuzx
 * 数据处理工具包
 * @data 23,Nov 12:36 pm
 */
public class DataTools {
    //分隔符和文件格式，可替换生成知道格式的文件
	private static final String splitor = "&";
	private static final String encoding = "gbk";
	private static final String[] whiteTable = new String[]{"mtdate"};
	private static final String whtieFieds = "mtdate";
	
	/**
	 * 获取所有的表模型信息
	 * @return
	 */
	public static List<Table>getAllTables(){
		List<Table> tables = OdbFactory.get().getOdbManager(Table.class).selectAll();
		return tables;
	}
	/**
	 * 删除不需要供给数仓的字段
	 * @param fields
	 * @return 
	 */
	public static void screenFields(List<Field> fields){
		
		Iterator<Field> it = fields.iterator();
		
		if(it.hasNext()){
			for(String str : whiteTable){
				if(CommUtil.equals(str, it.next().getId())){
					fields.remove(it);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Field>  getTableInfo (String tableName) {

		Table table = OdbFactory.get().getOdbManager(Table.class).selectByIndex(Table.TableName.class, tableName);
		if (table == null)
			throw new RuntimeException("数据库表[" + tableName + "]未定义模型!");		
		//无公共字段场景
		if (table.getExtensionType() == null || table.getExtensionType().length == 0) {
			return table.getFields();
		} else {
			List<Field> fields = new ArrayList<>();
			List<Field> parentElements = (List<Field>) ModelUtil.getParentKeys(table);
			List<Field> parentElements_NotKey = new ArrayList<>();
			
			if (parentElements != null) {
				for(Field e : parentElements) {
					if(e.getId().equals(whtieFieds)){
						continue;
					}
					if (e.getKey() != null && e.getKey()) {
						fields.add(e);
					} else {
						parentElements_NotKey.add(e);
					}
				}
			}
			
			fields.addAll(table.getFields());
			
			fields.addAll(parentElements_NotKey);
			
			return fields;
		}
	}
	/**
	 * 根据Unicode编码判断String类型是否含有中文字符
	 * @data Dec 23
	 * @author wuzhixiang
	 */
	private static boolean isChinese(char c){
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
				|| ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS
				|| ub == Character.UnicodeBlock.GENERAL_PUNCTUATION) {
			return true;
		}
		return false;
	}
	//完整的判断中文汉字和符号
	public static boolean isChinese(String str){
		
		char[] ch = str.toCharArray();	
		for(int i=0;i<ch.length;i++){
			char c = ch[i];
			if(isChinese(c)==true){
				return true;
			}
		}
		return false;
	}
	/**
	 * 获取String汉字中文个数
	 */
	public static int chineseNums(String str){
		
		byte b[] = str.getBytes();
		int byteLength = b.length;
		int strLength = str.length();
		if(isChinese(str) == true){			
			return (byteLength - strLength)/2;
		}
			return byteLength - strLength;	
	}
	/**
	 * 获取数据行的字符串
	 * 
	 * @param fields 表字段集合
	 * @param data 行数据
	 */
	public static String getLineStr(List<Field> fields, Object data) {
		StringBuffer sb = new StringBuffer();
		
		PropertyAccessor accessor = PropertyUtil.createAccessor(data);
		
		for(Field field : fields)
		{
			Object value = accessor.getNestedProperty(field.getId());
			
			sb.append(getFieldStr(field, value));
			sb.append(splitor); //拼接分隔符
		}
		
		return sb.toString();
	}
	
	/**
	 * 字段拼装
	 */
	private static String getFieldStr(Field field, Object value){
		
		Integer length = field.getFieldLength();
		
		if (length == null || length == 0) {
			throw new RuntimeException("字段[" + field.getId() + "]未定义长度");
		}
		
		SimpleType st = ModelUtil.getSimpleType(field.getTypeObj());
		
		if (st == null)
			throw new RuntimeException("字段[" + field.getId() + "]未定义类型");
		
		if (value instanceof BigDecimal) {
            value = ((BigDecimal) value).toPlainString();
        }
		
		String ret = (value == null) ? "" : value.toString();
		
		if (st.getJavaClass() == String.class) {	
			length = length * 2;
			//String中文占两个字节算出的位数可能不标准 需要做处理			
			ret = CommUtil.rpad(ret, length, " ", encoding);	
			
		} else if (st.getJavaClass() == Integer.class 
				|| st.getJavaClass() == Long.class
				|| st.getJavaClass() == Double.class
				|| st.getJavaClass() == BigDecimal.class) {
			ret = CommUtil.rpad(ret, length, " ", encoding);
		}
		
		return ret;
	}
	
}

