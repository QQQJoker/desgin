package com.joker.design.interpreter;

import java.util.HashMap;

public class AddExpression extends SymbolExpression{

	public AddExpression(Expression left, Expression right) {
		super(left, right);
	}

	@Override
	public int interperter(HashMap<String, Integer> var) {

		return super.left.interperter(var)+super.right.interperter(var);
	}

}
