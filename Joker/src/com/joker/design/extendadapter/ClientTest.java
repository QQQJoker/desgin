package com.joker.design.extendadapter;

public class ClientTest {

	public static void main(String[] args) {
		
		Target target = new ConcreteTarget();

		target.request();
		
		Target target2 = new Adapter();
		
		target.request();
	}

}
