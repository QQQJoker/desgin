package com.joker.design.decorator;
/**
 * 具体构件
 * @author Joker
 *
 */
public class ConcreteComponent extends Component {

	@Override
	public void operate() {
		System.out.println("do something");
	}

}
