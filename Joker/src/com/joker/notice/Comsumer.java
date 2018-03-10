package com.joker.notice;

public class Comsumer {

	public static void main(String[] args) {
		//接收序列化的数据进行反序列化转换成对象
		Person o = (Person) SerializationUtils.readObject();
		System.out.println("name = "+o.getName());
	}

}
