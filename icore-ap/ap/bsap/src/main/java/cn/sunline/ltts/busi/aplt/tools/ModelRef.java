package cn.sunline.ltts.busi.aplt.tools;

public class ModelRef<T> {
	T origin;
	public  ModelRef(){
	} 
	
	public  ModelRef(T origin){
		this.origin=origin;
	} 
	public T getOrigin() {
		return origin;
	}
	
	public void setOrigin(T model) {
		this.origin = model;
	}

}
