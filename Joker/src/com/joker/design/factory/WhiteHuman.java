package com.joker.design.factory;

public class WhiteHuman implements Human {


	@Override
	public void getColor() {

		System.out.println("whitehuman's skin is white");
	}

	@Override
	public void talk() {

		System.out.println("it's hard to get it when whiteHuman's say");
	}
}
