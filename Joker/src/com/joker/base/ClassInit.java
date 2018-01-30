package com.joker.base;

public class ClassInit extends Insect {

	private int k = printInit("ClassInit.k initialized");
	
	public ClassInit() {
		System.out.println("k = "+k);
		System.out.println("j = "+j);
		
	}
	
	private static int x2 = printInit("static ClassInit.x2 initialized");
	
	/**
	 * 运行这个类首先访问ClassInit.main()，加载器加载ClassInit类，ClassInit继承了Insect，所以加载器继续加载Insect类 
	 * 然后对加载的类的static域(同一个类中的static域则是按编码顺序)进行初始化，顺序是由最顶层的父类开始。所有的static域初始化完成
	 * 类加载就完成。
	 * 创建对象：对象所有的域进行初始化，基本数据类型为默认值，引用类型为null。然后调用调用构造器，顺序是从最顶层的父类开始，
	 * 
	 * */	
	public static void main(String []args) {
		System.out.println("ClassInit constructor");
	    ClassInit b = new ClassInit();
	}
}

class Insect{
	private int i = 9;
	protected int j;
	Insect(){
		System.out.println("i = "+i+", j = "+j);
		j=39;
	}
	
	private static int x1 = printInit("static Insect.x1 initialized");
	
	static int printInit(String s) {
		System.out.println(s);
		return 47;
	}
}


