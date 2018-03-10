package com.joker.notice;

import java.io.Serializable;

public class Person implements Serializable{

	private static final long serialVersionUID = 5403247122805250442L;
	
	private String name;
	
	private String age;
	
	public String getAge() {
		return age;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
