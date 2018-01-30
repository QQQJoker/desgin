package com.joker.design.mediator1;

public class Mediator extends AbstractMediator{

	@Override
	public void execute(String str, Object... objects) {

		if(str.equals("purchase.buy")) {//采购电脑
			this.buyComputer((int) objects[0]);
		}else if(str.equals("sale.sale")){//销售电脑
			this.sellComputer((int)objects[0]);
		}else if(str.equals("sale.offsell")) { //打折销售
			this.offsell();
		}else if(str.equals("stock.clear")) { //清仓处理
			this.clearStock();
		}
	}
	//采购电脑
	private void buyComputer(int number) {
		int salestatus = super.sale.getSaleStatus();
		if(salestatus > 80) {
			System.out.println("采购IBM电脑："+number+"台");
			super.stock.increase(number);
		}else {
			int buynumber = number/2;
			System.out.println("销售情况不好，折半采购IBM电脑："+buynumber+"台");
//			super.stock.increase(buynumber);
		}
	}
	//销售电脑
	private void sellComputer(int number) {
		if(super.sale.getSaleStatus() < number) { //库存不够销售
			super.purchase.buyIBMComputer(number);
		}
		super.stock.decrease(number);
	}
	
	//折价销售电脑
	private void offsell() {
		System.out.println("打折销售IBM电脑："+stock.getStockNumber()+"台");
	}
	
	//清仓处理
	private void clearStock() {
		super.sale.offSale();
		super.purchase.refuseBuyIBMComputer();
	}
	
}
