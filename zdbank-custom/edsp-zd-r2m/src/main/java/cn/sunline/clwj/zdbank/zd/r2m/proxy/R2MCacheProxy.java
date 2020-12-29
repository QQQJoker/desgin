package cn.sunline.clwj.zdbank.zd.r2m.proxy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.wangyin.rediscluster.client.R2mClusterClient;

import cn.sunline.clwj.zdbank.zd.r2m.api.EDSPCache;
import cn.sunline.clwj.zdbank.zd.r2m.util.SerializeUtils;

@Component
public class R2MCacheProxy implements EDSPCache {

	@Autowired(required=false)
	private R2mClusterClient client;

	@Override
	public boolean exist(String paramString) {
		return client.exists(paramString);
	}

	@Override
	public long delCachedData(String paramString) {
		return client.del(paramString);
	}

	@Override
	public long delCachedData(String[] paramArrayOfString) {
		return client.del(paramArrayOfString);
	}

	@Override
	public boolean setData(String paramString1, String paramString2) {
		if (client.set(paramString1, paramString2) != null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean setData(String paramString, Serializable paramSerializable) {
		String result = client.set(SerializeUtils.serialize(paramString), SerializeUtils.serialize(paramSerializable));
		if (result != null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean setData(String key, String value, int timeout) {
		String result = client.setex(key, timeout, value);
		if (result != null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean setDataNx(String key, String value) {
		Long ret = client.setnx(key, value);
		if(ret != null && ret > 0) {
			return true;
		}
		return false;
	}

	@Override
	public String getCachedString(String paramString) {
		return client.get(paramString);
	}

	@Override
	public long setExpire(String paramString, long paramLong, TimeUnit paramTimeUnit) {
		switch (paramTimeUnit) {
		case SECONDS:
			return client.expire(paramString, (int) paramLong);
		case MILLISECONDS:
			return client.pexpire(paramString, (int) paramLong);
		default:
			break;
		}
		throw new UnsupportedOperationException("只支持时间为秒或者毫秒");
	}

	@Override
	public long getExpire(String paramString) {
		return client.ttl(paramString);
	}

	@Override
	public long incr(String paramString, long paramLong) {
		return client.incrBy(paramString, paramLong);
	}

	@Override
	public long decr(String paramString, long paramLong) {
		return client.decrBy(paramString, paramLong);
	}

	@Override
	public boolean reName(String paramString1, String paramString2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock(String paramString1, String paramString2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean unLock(String paramString1, String paramString2) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock(String paramString1, String paramString2, long paramLong1, long paramLong2,
			TimeUnit paramTimeUnit) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> getHAllKeys(String paramString) {
		return client.hkeys(paramString);
	}

	@Override
	public Set<String> getSharedPatternKeysAll(String paramString) {
		String pattern = "*" + paramString + "*";
		return client.hkeys(pattern);
	}

	@Override
	public String spop(String paramString) {
		return client.spop(paramString);
	}

	@Override
	public String srandMember(String paramString) {
		return client.srandmember(paramString);
	}

	@Override
	public List<String> srandMemberlist(String paramString, int paramInt) {
		return client.srandmember(paramString, paramInt);
	}

	@Override
	public Map<String, String> getRedisPoolInfo() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hset(String paramString, Serializable paramSerializable1, Serializable paramSerializable2) {
		byte[] _key = SerializeUtils.serialize(paramString);
		byte[] _field = SerializeUtils.serialize(paramSerializable1);
		byte[] _data = SerializeUtils.serialize(paramSerializable2);
		long result = client.hset(_key, _field, _data);
		boolean bool = Boolean.valueOf((result == 1L) || (result == 0L)).booleanValue();
		return bool;
	}

	@Override
	public boolean hsetNx(String paramString, Serializable paramSerializable1, Serializable paramSerializable2) {
		return ((hexist(paramString, paramSerializable1)) ? false
				: hset(paramString, paramSerializable1, paramSerializable2));
	}

	@Override
	public boolean hmset(String paramString, Map<Serializable, Serializable> paramMap) {
		byte[] _key = SerializeUtils.serialize(paramString);
		Map hashMap = new HashMap();
		for (Map.Entry entry : paramMap.entrySet()) {
			hashMap.put(SerializeUtils.serialize(entry.getKey()), SerializeUtils.serialize(entry.getValue()));
		}
		if (client.hmset(_key, hashMap) != null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean hexist(String paramString, Serializable paramSerializable) {
		boolean bool = client
				.hexists(SerializeUtils.serialize(paramString), SerializeUtils.serialize(paramSerializable))
				.booleanValue();
		return bool;
	}

	@Override
	public long hdel(String paramString, Serializable[] paramArrayOfSerializable) {
		byte[] _key = SerializeUtils.serialize(paramString);
		int lenth = paramArrayOfSerializable.length;
		byte[][] _field = new byte[lenth][];
		for (int j = 0; j < lenth; ++j) {
			Serializable serializable = paramArrayOfSerializable[j];
			_field[j] = SerializeUtils.serialize(serializable);
		}
		return client.hdel(_key, _field);
	}

	@Override
	public Serializable hget(String paramString, Serializable paramSerializable) {
		byte[] result = client.hget(SerializeUtils.serialize(paramString), SerializeUtils.serialize(paramSerializable));
		Serializable localSerializable = SerializeUtils.deSerialize(result);
		return localSerializable;
	}

	@Override
	public Map<Serializable, Serializable> hgetall(String paramString) {
		Map<Serializable, Serializable> hash = new HashMap();
		Map<byte[], byte[]> result = client.hgetAll(SerializeUtils.serialize(paramString));
		for (Map.Entry entry : result.entrySet()) {
			hash.put(SerializeUtils.deSerialize((byte[]) entry.getKey()),
					SerializeUtils.deSerialize((byte[]) entry.getValue()));
		}
		return hash;
	}

	@Override
	public List<Serializable> hvals(String paramString) {
		List<Serializable> list = new ArrayList<Serializable>();
		client.hvals(SerializeUtils.serialize(paramString));
		Collection<byte[]> result = client.hvals(SerializeUtils.serialize(paramString));
		for (byte[] bs : result) {
			list.add(SerializeUtils.deSerialize(bs));
		}
		return list;
	}

	@Override
	public boolean sadd(String paramString, String[] paramArrayOfString) {
		long result = client.sadd(paramString, paramArrayOfString);
		boolean bool = Boolean.valueOf((result == 1L) || (result == 0L)).booleanValue();
		return bool;
	}

	@Override
	public boolean sadd(String paramString, Serializable[] paramArrayOfSerializable) {
		int length = paramArrayOfSerializable.length;
		byte[][] e = new byte[length][];
		for (int i = 0; i < length; ++i) {
			Serializable serializable = paramArrayOfSerializable[i];
			e[i] = SerializeUtils.serialize(serializable);
		}
		long result = client.sadd(SerializeUtils.serialize(paramString), e);
		boolean bool = Boolean.valueOf((result == 1L) || (result == 0L)).booleanValue();
		return bool;
	}

	@Override
	public Set<String> smembers(String paramString) {
		Set<String> localSet = client.smembers(paramString);
		return localSet;
	}

	@Override
	public Set<Serializable> smembersSerializable(String paramString) {
		Set<Serializable> serializables = new HashSet<Serializable>();
		Set<byte[]> set = client.smembers(SerializeUtils.serialize(paramString));
		for (byte[] bs : set) {
			serializables.add(SerializeUtils.deSerialize(bs));
		}
		return serializables;
	}

	@Override
	public boolean sismember(String paramString1, String paramString2) {
		return client.sismember(paramString1, paramString2).booleanValue();
	}

	@Override
	public boolean sismember(String paramString, Serializable paramSerializable) {
		boolean bool1 = client
				.sismember(SerializeUtils.serialize(paramString), SerializeUtils.serialize(paramSerializable))
				.booleanValue();
		return bool1;
	}

	@Override
	public Long scard(String paramString) {
		return client.scard(paramString);
	}

	@Override
	public Long srem(String paramString, String[] paramArrayOfString) {
		return client.srem(paramString, paramArrayOfString);
	}

	@Override
	public Long srem(String paramString, Serializable[] paramArrayOfSerializable) {
		int length = paramArrayOfSerializable.length;
		byte[][] members = new byte[length][];
		for (int i = 0; i < length; ++i) {
			Serializable serializable = paramArrayOfSerializable[i];
			members[i] = SerializeUtils.serialize(serializable);
		}
		return client.srem(SerializeUtils.serialize(paramString), members);
	}

	@Override
	public String lindex(String paramString, long paramLong) {
		return client.lindex(paramString, paramLong);
	}

	@Override
	public Serializable lindexSerializable(String paramString, long paramLong) {
		byte[] result = client.lindex(SerializeUtils.serialize(paramString), paramLong);
		return SerializeUtils.deSerialize(result);
	}

	@Override
	public long llen(String paramString) {
		return client.llen(paramString);
	}

	@Override
	public long lpush(String paramString, String[] paramArrayOfString) {
		return client.lpush(paramString, paramArrayOfString);
	}

	@Override
	public long lpush(String paramString, Serializable[] paramArrayOfSerializable) {
		int length = paramArrayOfSerializable.length;
		byte[][] _values = new byte[length][];
		for (int i = 0; i < length; ++i) {
			Serializable serializable = paramArrayOfSerializable[i];
			_values[i] = SerializeUtils.serialize(serializable);
		}
		long i = client.lpush(SerializeUtils.serialize(paramString), _values).longValue();
		return i;
	}

	@Override
	public long rpush(String paramString, String[] paramArrayOfString) {
		return client.rpush(paramString, paramArrayOfString);
	}

	@Override
	public long rpush(String paramString, Serializable[] paramArrayOfSerializable) {
		int length = paramArrayOfSerializable.length;
		byte[][] _values = new byte[length][];
		for (int i = 0; i < length; ++i) {
			Serializable serializable = paramArrayOfSerializable[i];
			_values[i] = SerializeUtils.serialize(serializable);
		}
		long i = client.rpush(SerializeUtils.serialize(paramString), _values).longValue();
		return i;
	}

	@Override
	public String lpop(String paramString) {
		return client.lpop(paramString);
	}

	@Override
	public Serializable lpopSerializable(String paramString) {
		byte[] result = client.lpop(SerializeUtils.serialize(paramString));
		Serializable localSerializable = SerializeUtils.deSerialize(result);
		return localSerializable;
	}

	@Override
	public String rpop(String paramString) {
		return client.rpop(paramString);
	}

	@Override
	public Serializable rpopSerializable(String paramString) {
		byte[] result = client.rpop(SerializeUtils.serialize(paramString));
		Serializable localSerializable = SerializeUtils.deSerialize(result);
		return localSerializable;
	}

	@Override
	public List<String> lrange(String paramString, int paramInt1, int paramInt2) {
		return client.lrange(paramString, paramInt1, paramInt2);
	}

	@Override
	public List<Serializable> lrangeSerializable(String paramString, int paramInt1, int paramInt2) {
		List<Serializable> result = new ArrayList<Serializable>();
		List<byte[]> list = client.lrange(SerializeUtils.serialize(paramString), paramInt1, paramInt2);
		for (byte[] bs : list) {
			result.add(SerializeUtils.deSerialize(bs));
		}
		return result;
	}

	@Override
	public List<String> lrange(String paramString) {
		long e = llen(paramString);
		return lrange(paramString, 0, (int) e);
	}

	@Override
	public List<Serializable> lrangeSerializable(String paramString) {
		long e = llen(paramString);
		return lrangeSerializable(paramString, 0, (int) e);
	}

}
