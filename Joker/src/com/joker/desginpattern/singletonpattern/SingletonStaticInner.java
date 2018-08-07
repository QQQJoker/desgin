package com.joker.desginpattern.singletonpattern;
/**
 * 静态内部类实现
 * <li>使用类装载机制来实例化对象，避免线程不安全问题，实现了懒加载，只有调用了getInstance方法才会进行实例化</li>
 * @author Joker
 *
 */
public class SingletonStaticInner {
	
	private SingletonStaticInner() {}

	private static class SingletonInstance {
		private static final SingletonStaticInner INSTANCE = new SingletonStaticInner();
	}
	
	 public static SingletonStaticInner getInstance() {
	        return SingletonInstance.INSTANCE;
	    }
}
