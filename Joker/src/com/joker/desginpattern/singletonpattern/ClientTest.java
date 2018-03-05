package com.joker.desginpattern.singletonpattern;

public class ClientTest {

	public static void main(String[] args) {
		
		Singleton.getInstance();
		// Singleton singleton = new Singleton(); 无法通过new 的方式创建对象。
	}

}
