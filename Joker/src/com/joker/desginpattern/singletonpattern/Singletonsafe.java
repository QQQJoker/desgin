package com.joker.desginpattern.singletonpattern;
/**
 * 饿汉模式，线程不安全
 * <li>具有懒加载的效果，但是线程不安全，只能但单线程下使用</li>
 * @author Joker
 *
 */
public class Singletonsafe {

	private static Singletonsafe singleton = null;
	
	private Singletonsafe() {}
	
	public static Singletonsafe getInstance() {
		if(singleton == null ) {
			new Singletonsafe();
		}
		return singleton;
	}
}
