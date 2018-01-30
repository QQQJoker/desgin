package com.joker.thread;

public class Mythread extends Thread {
	private int count = 5;
	
	public Mythread(String threadName) {
		super();
		this.setName(threadName);
	}

	@Override
	public void run() {
		super.run();
		while(count > 0){
			count--;
			System.out.println("由"+Mythread.currentThread().getName()+"计算 count = "+count);
		}
	}
}
