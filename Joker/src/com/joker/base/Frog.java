package com.joker.base;

public class Frog {

}

class Characteristic {
	private String s;
	Characteristic(String s){
		this.s = s;
		System.out.println("Creating Characteristic " + s);
	}
	protected void disopse() {
		System.out.println("disopsing Characteristic" + s);
	}
}