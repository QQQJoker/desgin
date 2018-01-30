package com.joker.proxy.staticproxy;

public class CountStaticProxy implements Count{

	private Count countImpl;
	
	public CountStaticProxy(Count countImpl) {
		this.countImpl = countImpl;
	}
	
	@Override	
	public void addCount() {
        System.out.println("befor transtion do");
		
        countImpl.addCount();
        
        System.out.println("after transtion do");
		
	}

	@Override
	public void queryCount() {
		System.out.println("befor transtion do");
		
        countImpl.queryCount();
        
        System.out.println("after transtion do");
		
	}

	@Override
	public void updateCount() {
		System.out.println("befor transtion do");
		
        countImpl.updateCount();
        
        System.out.println("after transtion do");
		
	}

}
