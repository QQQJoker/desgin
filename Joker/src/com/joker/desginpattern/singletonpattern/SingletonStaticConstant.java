package com.joker.desginpattern.singletonpattern;

/**
 * 饱汉模式  线程安全
 * <li>优点：写法简单，在类装载的时候就完成了实例化，避免了线程安全问题</li>
 * <li>缺点：在类装在的时候完成了实例化，没有达到懒加载的效果，如果一直没有使用过这个实例会造成内存浪费</li>
 * @author Joker
 *
 */
public class SingletonStaticConstant {

	private static SingletonStaticConstant singletonTwo = new SingletonStaticConstant();
	
	private SingletonStaticConstant() {}
	
	public static SingletonStaticConstant getInstance() {
		return singletonTwo;
	}
}
