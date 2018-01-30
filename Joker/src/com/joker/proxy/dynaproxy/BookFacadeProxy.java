package com.joker.proxy.dynaproxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class BookFacadeProxy implements InvocationHandler{

	private Object target;
	
	public Object bind(Object target) {
		this.target = target;
		return  Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
	}
	
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

		Object result = null;
		System.out.println("begin transation");
        result = method.invoke(proxy, args);
		System.out.println("end transation");
		return result;
	}

}
