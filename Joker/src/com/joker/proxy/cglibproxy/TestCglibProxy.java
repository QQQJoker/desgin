package com.joker.proxy.cglibproxy;

public class TestCglibProxy {

	public static void main(String []agrs) {
		
		  BookFacadeCglib cglib=new BookFacadeCglib();  
	      BookFacadeImpl bookCglib=(BookFacadeImpl)cglib.getInstance(new BookFacadeImpl());  
	      bookCglib.addBook();
	}
}
