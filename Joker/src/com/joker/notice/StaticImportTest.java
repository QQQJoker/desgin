package com.joker.notice;

import static java.lang.Math.PI;
import static java.lang.Math.abs;

public class StaticImportTest {

	//重写静态导入的常量和方法会导致静态导入失效
	public final static String PI ="joker";
	
	public static int abs(int abs) {
		return 0;
	}
	
	public static void main(String[] args) {
	     
		System.out.println("PI="+PI);
		System.out.println("abs(-100)="+abs(-100));
		
	}

	

}
