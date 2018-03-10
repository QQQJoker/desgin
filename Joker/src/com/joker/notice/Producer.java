package com.joker.notice;

public class Producer {

	public static void main(String[] args) {
		
		Person person = new Person();
		
		person.setName("joker");
		//序列化成数据流写入磁盘
		SerializationUtils.writeObject(person);

	}

}
