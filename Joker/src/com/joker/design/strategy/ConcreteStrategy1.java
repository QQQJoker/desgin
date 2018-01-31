package com.joker.design.strategy;

public class ConcreteStrategy1 implements Strategy{

	@Override
	public void doSomething() {
		System.out.println("这是第一个策略算法");
	}

}
