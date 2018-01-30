package com.joker.proxy.staticproxy;

public class Test {

	public static void main(String []args) {
		Count countImpl = new CountImpl();
		
		CountStaticProxy proxy = new CountStaticProxy(countImpl);
		
		proxy.addCount();
		proxy.queryCount();
		proxy.updateCount();
		
		
	}
}
