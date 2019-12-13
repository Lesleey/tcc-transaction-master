package org.mengyun.tcctransaction.repository;

import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.repository.helper.JedisCallback;
import org.mengyun.tcctransaction.repository.helper.RedisHelper;
import org.mengyun.tcctransaction.repository.helper.TransactionSerializer;
import org.mengyun.tcctransaction.serializer.JdkSerializationSerializer;
import org.mengyun.tcctransaction.serializer.ObjectSerializer;
import org.mengyun.tcctransaction.utils.ByteUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Redis 事务存储器
 *
 * Created by changming.xie on 2/24/16.
 * <p/>
 * As the storage of transaction need safely durable,make sure the redis server is set as AOF mode and always fsync.
 * set below directives in your redis.conf
 * appendonly yes
 * appendfsync always
 */
public class RedisTransactionRepository extends CachableTransactionRepository {

    /**
     * Jedis Pool
     */
    private JedisPool jedisPool;
    /**
     * key 前缀
     */
    private String keyPrefix = "TCC:";
    /**
     * 序列化
     */
    private ObjectSerializer serializer = new JdkSerializationSerializer();

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public void setJedisPool(JedisPool jedisPool) {
        this.jedisPool = jedisPool;
    }

    @Override
    protected int doCreate(final Transaction transaction) {
        try {
            // 创建事务的 Redis Key
            final byte[] key = RedisHelper.getRedisKey(keyPrefix, transaction.getXid());
            // 执行 Redis hsetnx
            Long statusCode = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {

                @Override
                public Long doInJedis(Jedis jedis) {
                    // https://redis.io/commands/hsetnx
                    return jedis.hsetnx(key, ByteUtils.longToBytes(transaction.getVersion()), TransactionSerializer.serialize(serializer, transaction));
                }

            });
            // 返回插入条数
            return statusCode.intValue();
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doUpdate(final Transaction transaction) {
        try {
            // 创建事务的 Redis Key
            final byte[] key = RedisHelper.getRedisKey(keyPrefix, transaction.getXid());
            // 执行 Redis hsetnx
            Long statusCode = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {
                @Override
                public Long doInJedis(Jedis jedis) {
                    // 设置最后更新时间 和 最新版本号
                    transaction.updateTime();
                    transaction.updateVersion();
                    // https://redis.io/commands/hsetnx
                    return jedis.hsetnx(key, ByteUtils.longToBytes(transaction.getVersion()), TransactionSerializer.serialize(serializer, transaction));
                    // TODO add by 芋艿：参照 JdbcTransactionRepository#doUpdate，如果 redis 更新发生异常（例如，网络），设置 transaction 回老版本。
                }
            });
            // 返回插入条数
            return statusCode.intValue();
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected int doDelete(Transaction transaction) {
        try {
            // 创建事务的 Redis Key
            final byte[] key = RedisHelper.getRedisKey(keyPrefix, transaction.getXid());
            // 执行 Redis 删除
            Long result = RedisHelper.execute(jedisPool, new JedisCallback<Long>() {
                @Override
                public Long doInJedis(Jedis jedis) {
                    return jedis.del(key);
                }
            });
            // 返回删除条数
            return result.intValue();
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected Transaction doFindOne(Xid xid) {
        try {
            // 创建事务的 Redis Key
            final byte[] key = RedisHelper.getRedisKey(keyPrefix, xid);
            // 查询 Redis
            byte[] content = RedisHelper.getKeyValue(jedisPool, key);
            if (content != null) {
                return TransactionSerializer.deserialize(serializer, content);
            }
            return null;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }

    @Override
    protected List<Transaction> doFindAllUnmodifiedSince(Date date) {
        // 获得所有事务
        List<Transaction> allTransactions = doFindAll();
        // 过滤时间
        List<Transaction> allUnmodifiedSince = new ArrayList<Transaction>();
        for (Transaction transaction : allTransactions) {
            if (transaction.getLastUpdateTime().compareTo(date) < 0) {
                allUnmodifiedSince.add(transaction);
            }
        }
        return allUnmodifiedSince;
    }

    /**
     * 获得所有事务
     *
     * @return 事务数组
     */
    protected List<Transaction> doFindAll() {
        try {
            List<Transaction> transactions = new ArrayList<Transaction>();
            // https://redis.io/commands/keys
            Set<byte[]> keys = RedisHelper.execute(jedisPool, new JedisCallback<Set<byte[]>>() {
                @Override
                public Set<byte[]> doInJedis(Jedis jedis) {
                    return jedis.keys((keyPrefix + "*").getBytes());
                }
            });
            // 反序列化 Transaction
            for (final byte[] key : keys) {
                byte[] content = RedisHelper.getKeyValue(jedisPool, key);
                if (content != null) {
                    transactions.add(TransactionSerializer.deserialize(serializer, content));
                }
            }
            return transactions;
        } catch (Exception e) {
            throw new TransactionIOException(e);
        }
    }
}
