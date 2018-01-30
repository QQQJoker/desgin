package com.joker.base;

import java.util.Random;

public class Animal {

	
	
	public static void main(String []args) {
		Random random = new Random(47);
	    Rodent rodents [] = new Rodent[9];
	    for(int i=0 ;i<rodents.length;i++) {
	    	rodents[i] = next(random); 
	    }
        
	    for(Rodent rodent:rodents) {
	    	rodent.sya();
	    }
		
	}
	
	public static Rodent next(Random random) {
		
		switch(random.nextInt(3)) {
		  default :
		  case 0: return new Mouse();
		  case 1: return new Gerbil();
		  case 2: return new Hamster();
		}
	}
	
	
}

class Rodent{
	
	void sya(){
      System.out.println("Rodent.say()");		
	}
}

class Mouse extends Rodent {

	@Override
	void sya() {
		System.out.println("Mouse.say()");
	}
	
}

class Gerbil extends Rodent{

	@Override
	void sya() {
      System.out.println("Gerbil.say()");
	}
	
}

class Hamster extends Rodent {

	@Override
	void sya() {
       System.out.println("Hamster.say()");
	}
	
}
