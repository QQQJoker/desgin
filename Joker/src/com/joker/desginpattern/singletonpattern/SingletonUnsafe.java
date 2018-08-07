package com.joker.desginpattern.singletonpattern;
/**
 * 饿汉模式，线程不安全
 * <li>具有懒加载的效果，使用synchronized实现多线程下的同步，线程安全，但是</li>
 * @author Joker
 *
 */
public class SingletonUnsafe {

	private static SingletonUnsafe singleton = null;
	
	private SingletonUnsafe() {}
	
	public  static synchronized SingletonUnsafe getInstance() {
		if(singleton == null ) {
			new SingletonUnsafe();
		}
		return singleton;
	}
}
