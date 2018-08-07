package com.joker.desginpattern.singletonpattern;

/**
 * 饱汉模式  线程安全
 * @author Joker
 *
 */
public class SingletonDoubleCheck {

	private static SingletonDoubleCheck singletonTwo = null;
	
	private SingletonDoubleCheck() {}
	
	public static SingletonDoubleCheck getInstance() {
		if(singletonTwo == null) {
			synchronized(SingletonDoubleCheck.class) {
				if(singletonTwo == null) {
					return singletonTwo;
				}
			}
		}
		return singletonTwo;
	}
}
