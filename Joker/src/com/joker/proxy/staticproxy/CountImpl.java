package com.joker.proxy.staticproxy;

public class CountImpl implements Count {

	@Override
	public void addCount() {
       System.out.println("this is add count method!");
	}

	@Override
	public void queryCount() {
		System.out.println("this is query count method!");
		
	}

	@Override
	public void updateCount() {

		System.out.println("this is update count method!");
		
	}

}
