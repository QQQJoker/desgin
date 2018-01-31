package com.joker.design.extendadapter;
/**
 * 类适配器
 * @author Joker
 *
 */
public class Adapter extends Adaptee implements Target{

	public void request() {
		super.doSomething();
	}
}
