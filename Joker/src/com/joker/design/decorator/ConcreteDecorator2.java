package com.joker.design.decorator;

public class ConcreteDecorator2 extends Decorator {

	
	
	public ConcreteDecorator2(Component component) {
		super(component);
	}

	//定义自己的装饰方式
	public void method2() {
		System.out.println("method2 装饰");
	}

	//重写父类，并且加上额外的装饰
	@Override
	public void operate() {
		this.method2();
		super.operate();
	}
	
}
