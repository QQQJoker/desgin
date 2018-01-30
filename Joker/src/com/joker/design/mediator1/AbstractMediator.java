package com.joker.design.mediator1;

import com.joker.design.mediator.Purchase;
import com.joker.design.mediator.Sale;
import com.joker.design.mediator.Stock;

public abstract class AbstractMediator {

	protected Purchase purchase;
	
	protected Sale sale;
	
	protected Stock stock;
	
	public AbstractMediator() {
	/*	purchase = new Purchase(this);
		sale = new Sale(this);
		stock = new Stock(this);*/
	}
	//中介者最重要的方式叫做事件方法，处理多个对象之间的关系
	public abstract void execute(String str,Object...objects);
	
}
