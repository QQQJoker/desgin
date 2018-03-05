package com.joker.desginpattern.singletonpattern;

public class Singleton {

	private static Singleton singleton = null;
	
	private Singleton() {}
	
	public static Singleton getInstance() {
		if(singleton == null ) {
			new Singleton();
		}
		return singleton;
	}
}
