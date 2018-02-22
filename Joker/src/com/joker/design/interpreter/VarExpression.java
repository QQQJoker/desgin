package com.joker.design.interpreter;

import java.util.HashMap;

public class VarExpression extends Expression {

	private String key;
	
	public  VarExpression(String key) {
		this.key = key;
	}
	
	@Override
	public int interperter(HashMap<String, Integer> var) {

		return  var.get(this.key);
	}

	
}
