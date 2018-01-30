package com.joker.design.factory;

public class BlackHuman implements Human {

	@Override
	public void getColor() {

		System.out.println("blackhuman's skin is black");
	}

	@Override
	public void talk() {

		System.out.println("it's hard to get it when blackHuman's say");
	}

}
