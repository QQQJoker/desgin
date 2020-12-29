package cn.sunline.ltts.busi.aplt.tools;

import java.math.BigDecimal;

import org.apache.commons.lang3.RandomUtils;

public class RandomMoneyTools {
	/**
	 * 小红包金额生成算法 <br/>
	 * 
	 * @param remainSize
	 *            剩余个数
	 * @param remainMoney
	 *            剩余金额
	 * @return
	 */
	/**
	 * 小红包金额生成算法
	 * @param remainSize 剩余个数
	 * @param remainMoney 剩余金额
	 * @param min 最小金额
	 * @param max 最大金额
	 * @return
	 */
	public static BigDecimal getRandomMoney(int remainSize,
			BigDecimal remainMoney, BigDecimal min, BigDecimal max) {

		BigDecimal realmax = null;
		BigDecimal realmin = null;
		BigDecimal minlow = min.multiply(BigDecimal.valueOf(remainSize));
		BigDecimal realmoney = remainMoney.subtract(minlow);
		BigDecimal use = max.subtract(min);

		if (1 == remainSize) { // 最后一个红包

			return remainMoney;
		}

		if (realmoney.longValue() - use.longValue() * (remainSize - 1) > 0) {

			long small = (realmoney.longValue() - use.longValue()
					* (remainSize - 1));
			realmin = new BigDecimal(small);
		} else {
			realmin =  BigDecimal.ZERO;
		}

		BigDecimal predictMoney = realmoney.divideToIntegralValue(
				BigDecimal.valueOf(remainSize)).multiply(BigDecimal.valueOf(2));

		int re = predictMoney.compareTo(use);
		if (re >= 0)
			realmax = max;

		else
			realmax = predictMoney.add(min);
		long low = realmin.longValue() + min.longValue();
		
		return BigDecimal
				.valueOf(RandomUtils.nextLong(low, realmax.longValue()));
	}
	/**
	 * 获取随机金额数
	 * @param remainSize 剩余个数
	 * @param remainMoney 剩余金额
	 * @param min 最小金额
	 * @param max 最大金额
	 * @return
	 */
	public static String[] getRandomMoneyList(int remainSize,
			BigDecimal remainMoney, BigDecimal min, BigDecimal max){
		BigDecimal b=new BigDecimal(100);
		remainMoney=remainMoney.multiply(b);
		String[] s =new String[remainSize];
		BigDecimal maxamt = BigDecimal.ZERO;//最大金额
		int j =0;//最大金额的下标
		min=min.multiply(b);
		max=max.multiply(b);
		for(int i= remainSize ;i>0;i--){
			BigDecimal temp = getRandomMoney(i, remainMoney, min, max);
			remainMoney =remainMoney.subtract(temp);
			if(temp.compareTo(maxamt)==1){
				maxamt = temp;
				j=i-1;
			}
			s[i-1] = temp.divide(b).toString();
		}
//		s[j] = s[j]+"M";//最佳手气
		s[j] = s[j];
		return s;
	}
	
	public static void main(String[] args) {
	    String[] randomMoneyList = getRandomMoneyList(10, BigDecimal.valueOf(1000), BigDecimal.valueOf(0), BigDecimal.valueOf(200));
	    for (String string : randomMoneyList) {
	        System.err.println(string);
        }
	}
}
