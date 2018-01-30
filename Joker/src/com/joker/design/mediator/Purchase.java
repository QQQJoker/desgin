package com.joker.design.mediator;

public class Purchase {

	public void buyIBMComputer(int number) {
		//访问库存
		Stock stock = new Stock();
		//访问销售
		Sale sale = new Sale();
		int saleStatus = sale.getSaleStatus();
		if(saleStatus > 80) {
			System.out.println("采购IBM电脑["+number+"]台");
			stock.increase(number);
		}else{
			int buyNumber = number/2;
			System.out.println("采购IBM电脑["+buyNumber+"]台");
		}
	}
	
	public void refuseBuyIBMComputer() {
		System.out.println("不再采购IBM电脑");
	}
}
