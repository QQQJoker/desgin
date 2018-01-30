package com.joker.proxy.cglibproxy;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class BookFacadeCglib implements MethodInterceptor{

	private Object target;
	
	
	public Object getInstance(Object target) {
		this.target = target;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(this.target.getClass());
		
		enhancer.setCallback(this);
		
		return enhancer.create();
		
	}
	@Override
	public Object intercept(Object arg0, Method arg1, Object[] arg2, MethodProxy arg3) throws Throwable {

		System.out.println("begin the transaction");
		
		arg3.invokeSuper(arg0, arg2);
		
		System.out.println("end ths transaction");
		
		return null;
	}

}
