package com.joker.design.decorator;
/**
 * 抽象装饰者
 * @author Joker
 *
 */
public abstract class Decorator extends Component{

	private Component component = null;
	//构造函数传递被修饰者
	public Decorator(Component component) {
		this.component = component;
	}
	
	//委托给被装饰者执行
	@Override
	public void operate() {
		this.component.operate();
	}
	
	
}
