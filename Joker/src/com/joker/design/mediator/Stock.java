package com.joker.design.mediator;

public class Stock {

	private static int COMPUTER_NUMBER = 100;
	
	//增加库存
	public void increase(int number) {
		COMPUTER_NUMBER = COMPUTER_NUMBER + number;
		System.out.println("库存数量为："+COMPUTER_NUMBER);
	}
	//获取库存数量
	public int getStockNumber() {
		return COMPUTER_NUMBER;
	}
	//存货压力大了，通知采购人员不要采购了，销售人员尽快销售
	public void clearStock() {
		Purchase puchase = new Purchase();
		Sale sale = new Sale();
		System.out.println("清理存货数量为："+COMPUTER_NUMBER);
		//打折销售
		sale.offSale();
		//采购人员不要再采购
		puchase.refuseBuyIBMComputer();
	}
	
	//库存降低
	public void decrease(int number) {
		COMPUTER_NUMBER = COMPUTER_NUMBER - number;
		System.out.println("库存数量为："+COMPUTER_NUMBER);
	}

}
