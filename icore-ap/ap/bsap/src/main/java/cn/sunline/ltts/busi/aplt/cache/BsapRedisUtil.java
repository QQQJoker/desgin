package cn.sunline.ltts.busi.aplt.cache;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import cn.sunline.adp.cedar.base.logging.BizLog;
import cn.sunline.adp.cedar.base.logging.BizLogUtil;
import cn.sunline.adp.core.util.SpringUtils;

@Component
public class BsapRedisUtil {

	@Autowired
	private RedisTemplate<String, Object> redisTemplateImpl;

	private static RedisTemplate<String, Object> redisTemplate;

	@PostConstruct
	public void init() {
		BsapRedisUtil.redisTemplate = redisTemplateImpl;
	}

	private static final BizLog logger = BizLogUtil.getBizLog(BsapRedisUtil.class);

	/**
	 * Spring 初始化使用, 禁止调用防止方直接使用, 清使用 {@link #getInstance()}
	 */
	public BsapRedisUtil() {
	}

	/**
	 * 获取<code>BsapRedisUtil</code>实例
	 * 
	 * @return
	 */
	public static BsapRedisUtil getInstance() {
		BsapRedisUtil instance = SpringUtils.getBean(BsapRedisUtil.class);
		if (instance == null) {
			logger.error("BsapRedisUtil failed to initialize");
			throw new RuntimeException("BsapRedisUtil failed to initialize");
		}
		return instance;
	}

	/** 返回RedisTemplate实例, 调用方根据业务场景调用 */
	public static RedisTemplate<String, Object> getRedisTemplate() {
		return BsapRedisUtil.getInstance().getTemplate();
	}

	private RedisTemplate<String, Object> getTemplate() {
		if (redisTemplate == null) {
			throw new RuntimeException("redisTemplate is not inited .");
		} else {
			return redisTemplate;
		}
	}

	/**
	 * 插入键、MAP
	 */
	public void insertAllMap(String key, Map<String, ?> map) {
		getTemplate().opsForHash().putAll(key, map);
	}

	/**
	 * 插入键、哈希ID、值
	 */
	public void insertMap(String key, String hashkey, Object o) {
		getTemplate().opsForHash().put(key, hashkey, o);
	}

	/** 获取key, haskey的值 */
	public Object selectByHashKey(String key, Object hashKey) {
		return getTemplate().opsForHash().get(key, hashKey);
	}

	/** 获取MAP */
	public Map<?, ?> selectMapByKey(String key) {
		return getTemplate().opsForHash().entries(key);
	}

	/**
	 * 插入单值
	 */
	public void insertKV(String key, Object o) {
		getTemplate().opsForValue().set(key, o);
	}

	/** 获取单值 */
	public Object selectValueByKey(String key) {
		return getTemplate().opsForValue().get(key);
	}

	/**
	 * 插入列表
	 */
	public void insertList(String key, List<?> o) {
		getTemplate().opsForList().leftPush(key, o);
	}

	/** 获取列表 */
	public List<?> selectListByKey(String key, long start, long end) {
		return getTemplate().opsForList().range(key, start, end);
	}
	
	
	public boolean hasKey(String key) {
		return getTemplate().hasKey(key);
	}
	
	public boolean exist(String key) {
		return getTemplate().hasKey(key);
	}
	
	public void setData(String key, Object value) {
		insertKV(key, value);
	}
	
	public void delCachedData(String key) {
		getTemplate().delete(key);
	}

}
