package com.joker.design.mediator1;

public class Purchase extends AbstractColleague{

	public Purchase(AbstractMediator mediator) {
		super(mediator);
	}

	public void buyIBMComputer(int number) {
		super.mediator.execute("purchase.buy", number);
	}
	
	public void refuseBuyIBMComputer() {
		System.out.println("不再采购IBM电脑");
	}
}
