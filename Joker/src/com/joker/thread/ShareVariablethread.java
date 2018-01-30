package com.joker.thread;

public class ShareVariablethread {

	public static void main(String []args) {
		//
		Mythread a = new Mythread("thread-A");
		Mythread b = new Mythread("thread-B");
		Mythread c = new Mythread("thread-C");
		a.start();
		b.start();
		c.start();
		//
		InnerMythread A = new InnerMythread("Innerthread-A");
		InnerMythread B = new InnerMythread("Innerthread-B");
		InnerMythread C = new InnerMythread("Innerthread-C");
		A.start();
		B.start();
		C.start();
	}
}


class InnerMythread extends Thread {
	private int count = 5;
	
	public InnerMythread(String threadName) {
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


