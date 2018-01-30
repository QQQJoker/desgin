package com.joker.base;

public class ExtednsTest {

	
	public static void main(String []args) {
		
		One as = new Two();
		as.eat();
		
	}
	
	
}


class One{
	
	void eat() {
		System.out.println("this is One.eat() method");
		say();
	}
	
	void say() {
		System.out.println("this is One.say() method");
	}
	
}

class Two extends One{

	@Override
	void say() {
		System.out.println("this is Two.say() method");
	}
	
}