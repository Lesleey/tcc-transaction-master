package org.mengyun.tcctransaction.repository;

import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.repository.helper.TransactionSerializer;
import org.mengyun.tcctransaction.serializer.JdkSerializationSerializer;
import org.mengyun.tcctransaction.serializer.ObjectSerializer;

import javax.transaction.xa.Xid;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * File 事务存储器， 该事务通过文件进行存储
 *
 * Created by changming.xie on 2/24/16.
 * this repository is suitable for single node, not for cluster nodes
 */
public class FileSystemTransactionRepository extends CachableTransactionRepository {

    /**
     * TCC 存储文件根目录
     */
    private String rootPath = "/tcc";
    /**
     * 是否初始化了文件根目录
     */
    private volatile boolean initialized;
    /**
     * 序列化
     */
    private ObjectSerializer serializer = new JdkSerializationSerializer();

    public void setSerializer(ObjectSerializer serializer) {
        this.serializer = serializer;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    @Override
    protected int doCreate(Transaction transaction) {
        writeFile(transaction);
        return 1;
    }

    @Override
    protected int doUpdate(Transaction transaction) {
        // 设置最后更新时间 和 最新版本号
        transaction.updateVersion();
        transaction.updateTime();
        // 写入文件
        writeFile(transaction);
        return 1;
    }

    @Override
    protected int doDelete(Transaction transaction) {
        // 获得事务存储路径
        String fullFileName = getFullFileName(transaction.getXid());
        // 删除文件
        File file = new File(fullFileName);
        if (file.exists()) {
            return file.delete() ? 1 : 0;
        }
        return 1;
    }

    @Override
    protected Transaction doFindOne(Xid xid) {
        String fullFileName = getFullFileName(xid);
        File file = new File(fullFileName);
        if (file.exists()) {
            return readTransaction(file);
        }
        return null;
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

    protected List<Transaction> doFindAll() {
        List<Transaction> transactions = new ArrayList<Transaction>();
        File path = new File(rootPath);
        File[] files = path.listFiles();
        for (File file : files) {
            Transaction transaction = readTransaction(file);
            transactions.add(transaction);
        }
        return transactions;
    }

    /**
     * 获得事务路径
     *
     * @param xid 事务编号
     * @return 路径
     */
    private String getFullFileName(Xid xid) {
        return String.format("%s/%s", rootPath, xid);
    }

    /**
     * 初始化文件目录
     */
    private void makeDirIfNecessary() {
        if (!initialized) {
            synchronized (FileSystemTransactionRepository.class) {
                if (!initialized) {
                    File rootPathFile = new File(rootPath);
                    if (!rootPathFile.exists()) {
                        // 创建文件
                        boolean result = rootPathFile.mkdir();
                        if (!result) {
                            throw new TransactionIOException("cannot create root path, the path to create is:" + rootPath);
                        }
                        initialized = true;
                    } else if (!rootPathFile.isDirectory()) {
                        throw new TransactionIOException("rootPath is not directory");
                    }
                }
            }
        }
    }

    private void writeFile(Transaction transaction) {
        // 初始化文件目录
        makeDirIfNecessary();
        // 写文件
        String file = getFullFileName(transaction.getXid());
        FileChannel channel = null;
        RandomAccessFile raf = null;
        byte[] content = TransactionSerializer.serialize(serializer, transaction);
        try {
            raf = new RandomAccessFile(file, "rw");
            channel = raf.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(content.length);
            buffer.put(content);
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            channel.force(true);
        } catch (Exception e) {
            throw new TransactionIOException(e);
        } finally {
            if (channel != null && channel.isOpen()) {
                try {
                    channel.close();
                } catch (IOException e) {
                    throw new TransactionIOException(e);
                }
            }
        }
    }

    private Transaction readTransaction(File file) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            byte[] content = new byte[(int) file.length()];
            fis.read(content);
            if (content != null) {
                return TransactionSerializer.deserialize(serializer, content);
            }
        } catch (Exception e) {
            throw new TransactionIOException(e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    throw new TransactionIOException(e);
                }
            }
        }
        return null;
    }

}
