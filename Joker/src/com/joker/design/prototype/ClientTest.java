package com.joker.design.prototype;

import java.util.Random;

public class ClientTest {

	private static int MAX_COUNT = 6;
	
	public static void main(String[] args) {

		int i = 0;
		Mail mail = new Mail(new AdvTemplate());
		mail.setTail("招商银行版权所有");
		
		while(i < MAX_COUNT) {
			mail.setAppellation(getRandString(5)+"先生/女士");
			mail.setReceiver(getRandString(8)+"@"+getRandString(5)+".com");
			sendMail(mail);
			i++;
		}
		
	}
	//发邮件
	public static void sendMail(Mail mail) {
		System.out.println("标题："+mail.getSubject()+"\n收件人："+mail.getReceiver()+"\n发送成功！\n");
	}

	//获取制定长度字符串
	public static String getRandString(int maxLength) {
		String source = "qwertyuiopasdfghjklzxcvbnm";
		StringBuffer sb = new StringBuffer();
		Random rand = new Random();
		for(int i=0;i<maxLength;i++) {
			sb.append(source.charAt(rand.nextInt(source.length())));
		}
		return sb.toString();
	}

}
