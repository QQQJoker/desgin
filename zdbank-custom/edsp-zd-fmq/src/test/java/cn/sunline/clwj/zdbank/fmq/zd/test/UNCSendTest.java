package cn.sunline.clwj.zdbank.fmq.zd.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import cn.sunline.clwj.zdbank.fmq.type.FmqEnumType.E_SENDTYPE;

public class UNCSendTest {

	@Test
	public void test() {
		List<E_SENDTYPE> sendTypeList = new ArrayList<>();
//		sendTypeList.add(E_SENDTYPE.ALL);
		sendTypeList.add(E_SENDTYPE.SMS);
		sendTypeList.add(E_SENDTYPE.MAIL);
		sendTypeList.add(E_SENDTYPE.PUSH);
		
		
		if(sendTypeList.contains(E_SENDTYPE.ALL)) {
			System.out.println("ALL");
		}else {
			for(E_SENDTYPE sendType:sendTypeList) {
				switch(sendType) {
				case SMS:
					System.out.println("SMS");
					break;
				case MAIL:
					System.out.println("MAIL");
					break;
				case PUSH:
					System.out.println("PUSH");
					break;
				default:
					System.out.println("SMS");
				}
			}
		}
	}

}
