package cn.sunline.ltts.busi.aplt.tools;
/**
 *excel数据类型对象
 * @author shuqingling
 * 
 *
 */
public class ExcelEntity {
	//数据类型  1-String 2-date,3-number,4-boolean,5-formula
	private     short   dataType;
	private  String  data;
	public short getDataType() {
		return dataType;
	}
	public void setDataType(short dataType) {
		this.dataType = dataType;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	

}
