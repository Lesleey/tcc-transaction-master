package org.mengyun.tcctransaction.repository.helper;

import org.mengyun.tcctransaction.utils.ByteUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.transaction.xa.Xid;
import java.util.*;

/**
 * Redis 工具类, 方便构建Redis的key, 方便存取对应的值transaction
 *
 * Created by changming.xie on 9/15/16.
 */
public class RedisHelper {

    /**
     * 通过key前缀和事务id创建一个 全局唯一的redis的 key。
     *
     * @param keyPrefix key 前缀
     * @param xid 事务
     * @return Redis Key
     */
    public static byte[] getRedisKey(String keyPrefix, Xid xid) {
        byte[] prefix = keyPrefix.getBytes();
        byte[] globalTransactionId = xid.getGlobalTransactionId();
        byte[] branchQualifier = xid.getBranchQualifier();
        byte[] key = new byte[prefix.length + globalTransactionId.length + branchQualifier.length];
        System.arraycopy(prefix, 0, key, 0, prefix.length);
        System.arraycopy(globalTransactionId, 0, key, prefix.length, globalTransactionId.length);
        System.arraycopy(branchQualifier, 0, key, prefix.length + globalTransactionId.length, branchQualifier.length);
        return key;
    }

    /**
     * 获得事务  通过key数组，从redis中获取byte[]数组
     *
     * @param jedisPool redis pool
     * @param key 事务的 Redis Key
     * @return 事务
     */
    public static byte[] getKeyValue(JedisPool jedisPool, final byte[] key) {
        return execute(jedisPool, new JedisCallback<byte[]>() {
                    @Override
                    public byte[] doInJedis(Jedis jedis) {
                        return getKeyValue(jedis, key);
                    }
                }
        );
    }

    /**
     * 获得事务
     *
     * @param jedis redis pool
     * @param key 事务的 Redis Key
     * @return 事务
     */
    public static byte[] getKeyValue(Jedis jedis, final byte[] key) {

        Map<byte[], byte[]> fieldValueMap = jedis.hgetAll(key);
        // 按照 key (version) 正序
        List<Map.Entry<byte[], byte[]>> entries = new ArrayList<Map.Entry<byte[], byte[]>>(fieldValueMap.entrySet());
        Collections.sort(entries, new Comparator<Map.Entry<byte[], byte[]>>() {
            @Override
            public int compare(Map.Entry<byte[], byte[]> entry1, Map.Entry<byte[], byte[]> entry2) {
                return (int) (ByteUtils.bytesToLong(entry1.getKey()) - ByteUtils.bytesToLong(entry2.getKey()));
            }
        });
        if (entries.isEmpty()) {
            return null;
        }
        // 返回最大版本的key的value
        return entries.get(entries.size() - 1).getValue();
    }

    /**
     * 执行回调
     *
     * @param jedisPool redis pool
     * @param callback 命名
     * @param <T> 泛型
     * @return 返回值
     */
    public static <T> T execute(JedisPool jedisPool, JedisCallback<T> callback) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return callback.doInJedis(jedis);
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}