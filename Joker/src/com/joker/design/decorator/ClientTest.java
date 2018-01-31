package com.joker.design.decorator;

public class ClientTest {

	public static void main(String[] args) {
		
		//原始构件
		Component component = new ConcreteComponent();
		
		//第一次装饰
		component = new ConcreteDecorator1(component);
		
		//第二次装饰
		component = new ConcreteDecorator2(component);
		
		//第三次装饰
		component = new ConcreteDecorator3(component);
		
		
		component.operate();
	}

}
