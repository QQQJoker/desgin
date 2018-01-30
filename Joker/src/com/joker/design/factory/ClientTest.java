package com.joker.design.factory;

public class ClientTest {

	public static void main(String[] args) {

		AbstractHumanFactory factory = new HumanFactory();
		
		Human blackhuman = factory.createHuman(BlackHuman.class);
		blackhuman.getColor();
		blackhuman.talk();
		
	}

}
