package com.joker.design.decorator;

public class ConcreteDecorator3 extends Decorator {

	
	
	public ConcreteDecorator3(Component component) {
		super(component);
	}

	//定义自己的装饰方式
	public void method3() {
		System.out.println("method3 装饰");
	}

	//重写父类，并且加上额外的装饰
	@Override
	public void operate() {
		this.method3();
		super.operate();
	}
	
}
