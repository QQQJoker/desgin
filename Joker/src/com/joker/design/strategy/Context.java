package com.joker.design.strategy;

public class Context {

	//抽象策略
	private Strategy strategy = null;
	//构造函数注入
	public Context(Strategy strategy){
		this.strategy = strategy;
	}
	
	//封装后的策略方法
	public void doAnything() {
		this.strategy.doSomething();
	}
}
