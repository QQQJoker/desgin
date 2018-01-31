package com.joker.design.extendadapter;

public class Adapter extends Adaptee implements Target{

	public void request() {
		super.doSomething();
	}
}
