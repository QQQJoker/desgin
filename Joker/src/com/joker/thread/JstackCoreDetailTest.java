package com.joker.thread;

import java.util.concurrent.TimeUnit;

public class JstackCoreDetailTest {

	public static void main(String []args) {
		final Thread thread = new Thread() {
			
			@Override
			public void run() {
				
				System.out.println("currentThreadName is "+Thread.currentThread().getName());
				
				synchronized(this) {
					
					try {
						TimeUnit.SECONDS.sleep(60);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					
				}
				
			} 
			
		};
		
		thread.start();// Runable 状态
		
		thread.setName("Thead-1");
		//主线程进入
		synchronized(thread) {
			
			System.out.println(Thread.currentThread().getName());
			
			try {
				TimeUnit.SECONDS.sleep(60);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		
	}
}
