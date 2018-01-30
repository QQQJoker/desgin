package com.joker.design.factory;

public class HumanFactory extends AbstractHumanFactory {

	@Override
	public <T extends Human> T createHuman(Class<T> c) {

		Human human = null;
		try {
			human = (T) Class.forName(c.getName()).newInstance();
			
		}catch(Exception e) {
			System.out.println("humans error");
		}
		
		return (T)human;
	}

}
