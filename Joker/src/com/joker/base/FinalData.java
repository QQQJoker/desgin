package com.joker.base;

import java.util.Random;

public class FinalData {
    private static Random rand = new Random(47);
	
    private String id;
    
    private String sb = "a";
    public FinalData(String id) {
    	this.id = id;
    }
	
    private final int valueOne = 9;   // 编译期常量，值不可变
    
    private static final int VALUE_TWO = 99; // 静态编译期常量 ，static 0.
    
    
    public static int VALUE_THREE = 39;  //静态常量，可用于包外，值可以改变
    
    private final int i4 = rand.nextInt(20);
    
    static final int INT_5 = rand.nextInt(20);
    
    private Value v1 = new Value(11);
    
    private final Value v2 = new Value(22);
    
    private static final Value VAL_3 = new Value(33);
    
    private final int[] a = {1,2,3,4,5,6};
    
    public String toString() {
    	return id + ": " + "i4 = " + i4 + ", INT_5 = " + INT_5;
    }
    
	public static void main(String []args) {
		FinalData fd1 = new FinalData("fd1");
		fd1.v2.i++;
		fd1.v1 = new Value(9);
		for(int i=0;i < fd1.a.length; i++) {
			fd1.a[i]++;
		}
		
		System.out.println(fd1);
		System.out.println(" Create new FinalData");
		FinalData fd2 = new FinalData("fd2");
		System.out.println(fd1);
		System.out.println(fd2);
		System.out.println(fd1.show(fd1));
		

		WithFinals w = new WithFinals();
		
		w.i = 10;
		
		System.out.println(w.i);
	}
    public static void show(final int s) {
    	//s++; 只能读取s的值，但是不能去改变s的值
    }
    public static String show(final FinalData s) {
    	//s++; 只能读取s的值，但是不能去改变s的值
    	s.sb = "sss";
    	return s.sb;
    }
    
}

class Value{
	int i;
	public Value(int i) {
		this.i = i;
	}
}


final class WithFinals {
	int i = 9;
	private final void f() {
		System.out.println("WithFinals.f()");
	}
	private void g() {
		System.out.println("WithFinals.g()");
	}
}



