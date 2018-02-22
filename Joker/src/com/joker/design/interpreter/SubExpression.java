package com.joker.design.interpreter;

import java.util.HashMap;

public class SubExpression extends SymbolExpression{

	public SubExpression(Expression left, Expression right) {
		super(left, right);
	}

	@Override
	public int interperter(HashMap<String, Integer> var) {

		return super.left.interperter(var) - super.right.interperter(var);
	}

}
