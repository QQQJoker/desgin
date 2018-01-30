package com.joker.design.mediator1;

import java.util.Random;

public class Sale extends AbstractColleague{

	public Sale(AbstractMediator mediator) {
		super(mediator);
	}

	//销售IBM电脑
	public void sellIBMComputer(int number) {
		super.mediator.execute("sale.sell", number);
		System.out.println("销售IBM电脑："+number+"台");
	}
	
	//反馈销售情况0-100之间变化，0代表就没人卖，100就代表非常畅销，出一个卖一个
	public int getSaleStatus() {
		Random rand = new Random(System.currentTimeMillis());
		int salestatus = rand.nextInt(100);
		System.out.println("IBM电脑的销售情况为："+salestatus);
		return salestatus;
	}
	//打折处理
	public void offSale() {
		//库存有多少卖多少
		super.mediator.execute("sale.offsell");
	}

}
