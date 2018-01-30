package com.joker.design.factory;

public class YellowHuman implements Human {

	@Override
	public void getColor() {

		System.out.println("yellowhuman's skin is yellow");
	}

	@Override
	public void talk() {

		System.out.println("it's hard to get it when yellowHuman's say");
	}
}
