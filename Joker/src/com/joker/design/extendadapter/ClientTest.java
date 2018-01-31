package com.joker.design.extendadapter;

public class ClientTest {

	public static void main(String[] args) {
		
		//原有的业务逻辑
		Target target = new ConcreteTarget();

		target.request();
		
		//增加适配器后的业务逻辑
		Target target2 = new Adapter();
		
		target.request();
	}

}
