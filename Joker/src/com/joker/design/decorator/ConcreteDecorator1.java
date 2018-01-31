package com.joker.design.decorator;
/**
 * 具体的装饰类
 * @author Joker
 *
 */
public class ConcreteDecorator1 extends Decorator {

	//定义被装饰者
	public ConcreteDecorator1(Component component) {
		super(component);
	}
	
	//定义自己的修饰方法
	private void method1() {
		System.out.println("method1 修饰");
	}

	//重写父类的operation方法
	@Override
	public void operate() {
		this.method1();
		super.operate();
	}
	

}
