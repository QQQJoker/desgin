package cn.sunline.clwj.zdbank.zd.r2m.api;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 平台封装缓存API扩展点接口定义 <br/>
 * 
 * @author jianghong
 * 
 */
public interface EDSPCache {	

    /**
     * 检测指定键的缓存数据是否存在 <br/>
     * 
     * @param key
     * 
     *        待检测的缓存键
     * @return 存在返回true，不存在返回false
     */
    public boolean exist(String key);

    /**
     * 删除指定键的缓存数据 <br/>
     * 
     * @param key
     *        待删除的缓存键
     * @return 已删除的缓存个数
     */
    public long delCachedData(String key);

    /**
     * （批量）删除指定键的缓存数据 <br/>
     * 
     * @param keys
     *        待删除的缓存键列表
     * @return 已删除的缓存个数
     *         TODO DAP实现实际返回值为参数数组元素个数，该返回值没有实际意义
     */
    public long delCachedData(String[] keys);
  
    /**
     * 给指定的缓存键赋值 <br/>
     * 注：存在覆盖现象 <br/>
     * 
     * @param key
     *        待赋值的缓存键
     * @param data
     *        待赋值的缓存数据
     * @return 成功返回true，失败返回false
     */
    public boolean setData(String key, String data);

    /**
     * 给指定的缓存键赋值，并指定其缓存失效时间 <br/>
     * 注：存在覆盖现象 <br/>
     * 
     * @param key
     *        待赋值的缓存键
     * @param data
     *        待赋值的缓存数据
     * @param timeout
     *        缓存失效时间值(秒)
     * @return 成功返回true，失败返回false
     */ 	
    public boolean setData(String key, String data, int timeout);

    /**
     * 给指定的缓存键赋值 <br/>
     * 注：存在覆盖现象 <br/>
     *  
     * @param key
     *        待赋值的缓存键
     * @param data
     *        待赋值的缓存数据
     * @return 成功返回true，失败返回false
     */
    public boolean setData(String key, Serializable data);

    /**
     * 设置缓存数据（缓存键不存在时） <br/>
     * 
     * @param key
     *        待缓存键
     * @param data
     *        待缓存数据
     * @return 成功返回true，失败返回false
     */
    public boolean setDataNx(String key, String data);

    /**
     * 获取指定缓存键的值 <br/>
     * 
     * @param key
     *        缓存键
     * @return 缓存字符串值
     */
    public String getCachedString(String key);

    /**
     * 设置指定缓存键的缓存失效时间 <br/>
     * 
     * @param key
     *        缓存键
     * @param time
     *        缓存失效时间值
     * @param timeunit
     *        缓存失效时间单位
     * @return 设置之后的缓存失效时间（以秒为单位）
     */
    public long setExpire(String key, long time, TimeUnit timeunit);

    /**
     * 查询指定键的缓存失效时间 <br/>
     * 
     * @param key
     *        缓存键
     * @return 返回当前剩余的缓存失效时间（以秒为单位） -1表示有对应的缓存但没有失效时间，-2表示没有对应的缓存
     */
    public long getExpire(String key);

    /**
     * 指定缓存键的原子自增 <br/>
     * 注: 缓存键对应的数据必须是数值格式 <br/>
     * 
     * @param key
     *        待操作的缓存键
     * @param step
     *        自增步长
     * @return 自增后的缓存数据值
     */
    public long incr(String key, long step);

    /**
     * 指定缓存键的原子自减 <br/>
     * 注: 缓存键对应的数据必须是数值格式 <br/>
     * 
     * @param key
     *        待操作的缓存键
     * @param step
     *        自减步长
     * @return 自减后的缓存数据值
     */
    public long decr(String key, long step);

    /**
     * 缓存键重命名 <br/>
     * 
     * @param oldKey
     *        旧缓存键
     * @param newKey
     *        新缓存键
     * @return 成功返回true，失败返回false
     */
    public boolean reName(String oldKey, String newKey);

    /**
     * 请求指定缓存键的资源锁（非阻塞式锁） <br/>
     * 注: 必须和unLock()方法配合使用，防止锁资源泄漏 <br/>
     * 
     * @param key
     *        待加锁的缓存键
     * @param name
     *        待加锁标识（由谁加锁）
     * @return 成功返回true，失败返回false
     */
    public boolean tryLock(String key, String name);

    /**
     * 释放指定缓存键的资源锁 <br/>
     * 注: 必须配合tryLock()方法使用，防止锁资源泄漏 <br/>
     * 
     * @param key
     *        待释放锁的缓存键
     * @param name
     *        待释放指定标识的锁（由谁释放）
     * @return
     */
    public boolean unLock(String key, String name);

    /**
     * TODO
     * 
     * @param key
     * @param name
     * @param time
     * @param time1
     * @param timeunit
     * @return
     */
    public boolean tryLock(String key, String name, long trytime, long timeout,
            TimeUnit timeunit);

    /**
     * TODO
     * 
     * @param key
     * @return
     */
    public Set<String> getHAllKeys(String key);

    /**
     * TODO
     * 
     * @param key
     * @return
     */
    public Set<String> getSharedPatternKeysAll(String key);
    
    /**
     * 移除并返回set集合中的一个随机元素。
     * @param key
     * @return
     * @throws Exception
     */
    public String spop(String key);
    
    /**
     * 返回set集合中的一个随机元素。
     * @param key
     * @return
     * @throws Exception
     */
    public String srandMember(String key);
  
    /**
     * 返回set集合中的随机元素 。
     * @param key count
     * @return
     * @throws Exception
     */
    public List<String> srandMemberlist(String key,int count)throws Exception;

    /**
     * 获取缓存池（与缓存中心通信的缓存客户端）信息 <br/>
     * 
     * @return 缓存池信息Map
     */
    
    public Map<String, String> getRedisPoolInfo();

    /**
     * 将哈希表key中的域field的值设置为data
     * 该方法会覆盖已有的field值
     * 操作成功返回true否则返回false
     * 
     * @param key 哈希表的键
     * @param field 哈希表的域
     * @param data 对应域的值
     * @return boolean
     */
    public boolean hset(String key, Serializable field, Serializable data);

    /**
     * 将哈希表key中的域field的值设置为data，当且仅当field不存在
     * 若域field已存在，该操作无效返回false
     * 操作成功返回true否则返回false
     * 
     * @param key 哈希表的键
     * @param field 哈希表的域
     * @param data 对应域的值
     * @return boolean
     */
    public boolean hsetNx(String key, Serializable field, Serializable data);

    /**
     * 同时将多个field-value（域-值）设置到哈希表key中
     * 此命令会覆盖哈希表中的域
     * 
     * @param key 哈希表的键
     * @param map field-value（域-值）的集合
     * @return boolean
     */
    public boolean hmset(String key, Map<Serializable, Serializable> map);

    /**
     * 判断哈希表key的域是否存在
     * 
     * @param key 哈希表的key
     * @param field 哈希表的域
     * @return boolean
     */
    public boolean hexist(String key, Serializable field);

    /**
     * 删除哈希表key中多个域fields的值
     * 不存在的将被忽略
     * 
     * @param key 哈希表
     * @param fields 域集合
     * @return long
     */
    public long hdel(String key, Serializable[] fields);

    /**
     * 返回哈希表key中给定域field的值
     * 
     * @param key 哈希表
     * @param field 指定的域
     * @return Serializable
     */
    public Serializable hget(String key, Serializable field);

    /**
     * 返回指定哈希表key所有的域和值
     * 
     * @param key 哈希表
     * @return Map<Serializable,Serializable>
     */
    public Map<Serializable, Serializable> hgetall(String key);

    /**
     * 返回哈希表key的所有值
     * 
     * @param key 哈希表
     * @return List<Serializable>
     */
    public List<Serializable> hvals(String key);

    /**
     * 将一个活多个data加入到集合key当中
     * 已经存在的数据将忽略
     * 
     * @param key 集合
     * @param datas 数据集
     * @return boolean
     */
    public boolean sadd(String key, String[] datas);

    /**
     * 将一个活多个data加入到集合key当中
     * 已经存在的数据将忽略
     * 
     * @param key 集合
     * @param datas 数据集
     * @return boolean
     */
    public boolean sadd(String key, Serializable[] datas);

    /**
     * 返回集合key的所有成员
     * 
     * @param key 集合
     * @return Set<String>
     */
    public Set<String> smembers(String key);

    /**
     * 返回集合key的所有成员
     * 
     * @param key 集合
     * @return Set<Serializable>
     */
    public Set<Serializable> smembersSerializable(String key);

    /**
     * 判断数据是否存在集合中
     * 
     * @param key 集合
     * @param data 数据
     * @return boolean
     */
    public boolean sismember(String key, String data);

    /**
     * 判断数据是否存在集合中
     * 
     * @param key 集合
     * @param data 数据
     * @return boolean
     */
    public boolean sismember(String key, Serializable data);

    /**
     * 返回集合key的基数(集合中元素的数量)
     * 
     * @param key 集合
     * @return Long
     */
    public Long scard(String key);

    /**
     * 删除集合中的多个元素
     * 不存在的元素将被忽略
     * 
     * @param key 集合
     * @param datas 数据集
     * @return Long
     */
    public Long srem(String key, String[] datas);

    /**
     * 删除集合中的多个元素
     * 不存在的元素将被忽略
     * 
     * @param key 集合
     * @param datas 数据集
     * @return Long
     */
    public Long srem(String key, Serializable[] datas);

    // list(表)
    /**
     * 返回列表key中，下标为index的元素
     * 下标参数start和stop都以0为底，也就是说，0表示列表的第一个元素
     * 也可用负数下标，-1表示最后一个，-2倒数第二个
     * 
     * @param key 列表
     * @param index 下标
     * @return String
     */
    public String lindex(String key, long index);

    /**
     * 返回列表key中，下标为index的元素
     * 下标参数start和stop都以0为底，也就是说，0表示列表的第一个元素
     * 也可用负数下标，-1表示最后一个，-2倒数第二个
     * 
     * @param key 列表
     * @param index 下标
     * @return String
     */
    public Serializable lindexSerializable(String key, long index);

    /**
     * 返回列表key的长度
     * key不存在返回0
     * key不是列表将会出错
     * 
     * @param key 列表
     * @return long
     */
    public long llen(String key);

    /**
     * 将一个或多个值data插入列表key的表头
     * key不存在将会创建一个空列表并执行lpush
     * 返回执行操作后列表长度
     * key不是列表将会出错
     * 
     * @param key 列表
     * @param datas 数据集
     * @return long
     */
    public long lpush(String key, String[] datas);

    /**
     * 将一个或多个值data插入列表key的表头
     * key不存在将会创建一个空列表并执行rpush
     * 返回执行操作后列表长度
     * key不是列表将会出错
     * 
     * @param key 列表
     * @param datas 数据集
     * @return long
     */
    public long lpush(String key, Serializable[] datas);

    /**
     * 将一个活多个值data插入列表key的表尾
     * key不存在将会创建一个空列表并执行lpush
     * 返回执行操作后列表长度
     * key不是列表将会出错
     * 
     * @param key 列表
     * @param datas 数据集
     * @return long
     */
    public long rpush(String key, String[] datas);

    /**
     * 将一个活多个值data插入列表key的表尾
     * key不存在将会创建一个空列表并执行rpush
     * 返回执行操作后列表长度
     * key不是列表将会出错
     * 
     * @param key 列表
     * @param datas 数据集
     * @return long
     */
    public long rpush(String key, Serializable[] datas);

    /**
     * 移除 并返回列表表头
     * 
     * @param key 列表
     * @return String
     */
    public String lpop(String key);

    /**
     * 移除 并返回列表表头
     * 
     * @param key 列表
     * @return Serializable
     */
    public Serializable lpopSerializable(String key);

    /**
     * 移除 并返回列表表尾
     * 
     * @param key 列表
     * @return String
     */
    public String rpop(String key);

    /**
     * 移除 并返回列表表尾
     * 
     * @param key 列表
     * @return Serializable
     */
    public Serializable rpopSerializable(String key);

    /**
     * 返回列表指定区间(start,stop)内的元素
     * 下标参数start和stop都以0为底，也就是说，0表示列表的第一个元素
     * 也可用负数下标，-1表示最后一个，-2倒数第二个
     * 如果start=0,stop=10返回数据为下标0-10的11个元素
     * 
     * @param key 列表
     * @param start 开始下标
     * @param stop 结束下标
     * @return List<String>
     */
    public List<String> lrange(String key, int start, int stop);

    /**
     * 返回列表指定区间(start,stop)内的元素
     * 下标参数start和stop都以0为底，也就是说，0表示列表的第一个元素
     * 也可用负数下标，-1表示最后一个，-2倒数第二个
     * 如果start=0,stop=10返回数据为下标0-10的11个元素
     * 
     * @param key 列表
     * @param start 开始下标
     * @param stop 结束下标
     * @return List<Serializable>
     */
    public List<Serializable> lrangeSerializable(String key, int start, int stop);

    /**
     * Redis Lrange 返回列表中指定区间内的元素
     * 区间以偏移量 START 和 END 指定。 
     * 其中 0 表示列表的第一个元素， 1 表示列表的第二个元素，以此类推。
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * @param key
     * @return List<String>
     */
    public List<String> lrange(String key);

    /**
     * Redis Lrange 返回列表中指定区间内的元素
     * 区间以偏移量 START 和 END 指定。 
     * 其中 0 表示列表的第一个元素， 1 表示列表的第二个元素，以此类推。
     * 你也可以使用负数下标，以 -1 表示列表的最后一个元素， -2 表示列表的倒数第二个元素，以此类推。
     * 
     * @param key
     * @return List<Serializable>
     */
    public List<Serializable> lrangeSerializable(String key);
}
