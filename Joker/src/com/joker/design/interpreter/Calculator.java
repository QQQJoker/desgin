
package com.joker.design.interpreter;

import java.util.HashMap;
import java.util.Stack;

public class Calculator {

	private Expression expression;
	
	public Calculator(String expStr) {
		Stack<Expression> stack = new Stack<Expression>();
		
		char[] charArry = expStr.toCharArray();
		
		Expression left = null;
		
		Expression right = null;
		
		for(int i=0;i<charArry.length;i++) {
			
			switch(charArry[i]) {
			case '+':
				left = stack.pop();
				right = new VarExpression(String.valueOf(charArry[++i]));
				stack.push(new AddExpression(left, right));
				break;

			case '-':
				left = stack.pop();
				right = new VarExpression(String.valueOf(charArry[++i]));
				stack.push(new SubExpression(left, right));
				break;
			default:
				stack.push(new VarExpression(String.valueOf(charArry[i])));
			}
			
		}
		this.expression = stack.pop();
	}
	
	public int run(HashMap<String,Integer> var) {
		return this.expression.interperter(var);
	}
	
}
