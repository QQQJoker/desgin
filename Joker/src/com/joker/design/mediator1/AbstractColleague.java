package com.joker.design.mediator1;

public abstract class AbstractColleague {
	
	protected AbstractMediator mediator;
	
	public AbstractColleague(AbstractMediator mediator) {
		this.mediator = mediator;
	}
}
