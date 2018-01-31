package com.joker.design.strategy;

public class ClientTest {

	public static void main(String[] args) {

		//声明一个具体的策略
		Strategy strategy = new ConcreteStrategy1();
		
		//声明上下文对象
		Context context = new Context(strategy);

		//执行封装后的方法
		context.doAnything();
		
		//声明第二个具体的策略
		Strategy strategy2 = new ConcreteStrategy2();
				
		//声明上下文对象
		Context context2 = new Context(strategy2);
		
		//执行封装后的方法		
		context2.doAnything();
	}

}
