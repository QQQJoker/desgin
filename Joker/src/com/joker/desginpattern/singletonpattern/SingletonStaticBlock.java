package com.joker.desginpattern.singletonpattern;

/**
 * 饱汉模式二，自行对象实例化采用静态块实现 线程安全
 * <li>和静态常量实现的方法的原理一样，在类装载的时候完成对象实例化，没有达到懒加载的效果，但是避免了线程安全</li>
 * @author Joker
 *
 */
public class SingletonStaticBlock {

	private static SingletonStaticBlock singletonTwo = null;
	
	static {
		singletonTwo = new SingletonStaticBlock();
	}
	
	private SingletonStaticBlock() {}
	
	public static SingletonStaticBlock getInstance() {
		return singletonTwo;
	}
}
