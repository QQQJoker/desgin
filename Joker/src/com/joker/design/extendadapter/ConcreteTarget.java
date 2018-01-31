package com.joker.design.extendadapter;

public class ConcreteTarget implements Target{

	@Override
	public void request() {
		System.out.println("if you need me help please call me");
	}

}
