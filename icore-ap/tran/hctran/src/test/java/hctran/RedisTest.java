package hctran;

import org.junit.Test;

//import redis.clients.jedis.Jedis;

public class RedisTest{

//	@Test
	public void redisTest(){
//		//连接本地的 Redis 服务
//		Jedis jedis = new Jedis("localhost");
//		System.out.println("连接成功");
//		//查看服务是否运行aa
//		System.out.println("服务正在运行：" + jedis.ping());
	}
	
	@Test
	public void hashTest(){
		String[] hcaccts = {"20170816010A00999000000000000001","20170816010A0099900000002","20170816010R0099900066001","20170816010R0099900066002"};
		for (String hcacct : hcaccts) {
			System.out.println(hcacct.hashCode() & 0x7FFFFFFF);
			System.out.println(hcacct.hashCode() & 0x7FFFFFFF % 10);
		}
		
	}
}
