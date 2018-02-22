package com.joker.design.interpreter;

import java.util.HashMap;

public abstract class Expression {

	//解析公式和数值 其中var中的key值是公式中的参数，value值是具体的数值
	public abstract int interperter(HashMap<String,Integer> var);
}
