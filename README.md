
# 个人博客

[http://www.iocoder.cn](http://www.iocoder.cn/?github)

-------

![](http://www.iocoder.cn/images/common/wechat_mp.jpeg)

> 🙂🙂🙂关注**微信公众号：【芋艿的后端小屋】**有福利：  
> 1. RocketMQ / MyCAT / Sharding-JDBC **所有**源码分析文章列表  
> 2. RocketMQ / MyCAT / Sharding-JDBC **中文注释源码 GitHub 地址**  
> 3. 您对于源码的疑问每条留言**都**将得到**认真**回复。**甚至不知道如何读源码也可以请教噢**。  
> 4. **新的**源码解析文章**实时**收到通知。**每周更新一篇左右**。

-------

* 知识星球：![知识星球](http://www.iocoder.cn/images/Architecture/2017_12_29/01.png)
* TCC事务中间件 **TCC-Transaction**
    * [《TCC-Transaction 源码分析 —— 调试环境搭建》](http://www.iocoder.cn/TCC-Transaction/build-debugging-environment?github&1606)
    * [《TCC-Transaction 源码分析 —— TCC 实现》](http://www.iocoder.cn/TCC-Transaction/tcc-core?github&1606)
    * [《TCC-Transaction 源码分析 —— 事务存储器》](http://www.iocoder.cn/TCC-Transaction/transaction-repository?github&1606)
    * [《TCC-Transaction 源码分析 —— 事务恢复》](http://www.iocoder.cn/TCC-Transaction/transaction-recovery?github&1606)
    * [《TCC-Transaction 源码分析 —— 运维平台》](http://www.iocoder.cn/TCC-Transaction/console?github&1606)
    * [《TCC-Transaction 源码分析 —— Dubbo 支持》](http://www.iocoder.cn/TCC-Transaction/dubbo-support?github&1606)
    * [《TCC-Transaction 源码分析 —— 项目实战》](http://www.iocoder.cn/TCC-Transaction/http-sample?github&1606)

使用指南1.1.x:https://github.com/changmingxie/tcc-transaction/wiki/%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%971.1.x

1.1.x 源码分支：https://github.com/changmingxie/tcc-transaction/tree/master

使用指南1.2.x:https://github.com/changmingxie/tcc-transaction/wiki/%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%971.2.x

1.2.x 源码分支：https://github.com/changmingxie/tcc-transaction/tree/master-1.2.x

1.2.x 版本不向下兼容1.1.x，主要在声明tcc服务方法的注解有改变。1.2.x不同于1.1.x主要的地方在于发布服务时不再强制要求服务方法参数必须有TransactionContext参数，从而减少对业务代码的侵入。



Try: 尝试执行业务

    完成所有业务检查（一致性）

    预留必须业务资源（准隔离性）

Confirm: 确认执行业务

    真正执行业务

    不作任何业务检查

    只使用Try阶段预留的业务资源

    Confirm操作满足幂等性

Cancel: 取消执行业务

    释放Try阶段预留的业务资源

    Cancel操作满足幂等性


示例说明:

tcc-transaction不和底层使用的rpc框架耦合，也就是使用doubbo,thrift,web service,http等都可。tcc-transaction-http-sample示例演示了不依赖底层rpc框架情况下如何使用tcc-transaction。

在底层rpc框架为dubbo情况下，可利用tcc-trnansaction-dubbo jar提供的便利，方便应用使用tcc-transaction。tcc-transaction-dubbo-sample示例演示了在使用dubbo作为rpc调用情况下如何使用tcc-transaction。

示例演示在下完订单后,使用红包帐户和资金帐户来付款，红包帐户服务和资金帐户服务在不同的系统中。示例中，有两个SOA提供方，一个是CapitalTradeOrderService，代表着资金帐户服务,另一个是RedPacketTradeOrderService,代表着红包帐户服务。

下完订单后，订单状态为DRAFT，在TCC事务中TRY阶段，订单支付服务将订单状态变成PAYING，同时远程调用红包帐户服务和资金帐户服务,将付款方的余额减掉（预留业务资源);如果在TRY阶段，任何一个服务失败，tcc-transaction将自动调用这些服务对应的cancel方法，订单支付服务将订单状态变成PAY_FAILED,同时远程调用红包帐户服务和资金帐户服务,将付款方余额减掉的部分增加回去；如果TRY阶段正常完成，则进入CONFIRM阶段，在CONFIRM阶段（tcc-transaction自动调用）,订单支付服务将订单状态变成CONFIRMED,同时远程调用红包帐户服务和资金帐户服务对应的CONFIRM方法，将收款方的余额增加。特别说明下，由于是示例，在CONFIRM和CANCEL方法中没有实现幂等性，如果在真实项目中使用，需要保证CONFIRM和CANCEL方法的幂等性。

在运行sample前，需搭建好db环境，运行dbscripts目录下的create_db.sql建立数据库实例及表；还需修改各种项目中jdbc.properties文件中的jdbc连接信息。

如有问题可以在本项目的github issues中提问。或是加微信:changmingxie，为便于识别，麻烦在备注中写下 在tcc-transaction中声明一个tcc的方法时需要加的注解类的名字，作者尽量回答疑问。 


### Donate
You can show your appreciation for TCC-TRANSACTION and support future development by donating! We strongly feel that TCC-TRANSACTION should remain free of charge as our gift to the online community. We ask for a donation of  10.00 RMB, but of course we appreciate any amount, thanks.
![捐款微信二维码](https://github.com/changmingxie/aggregate-framework/blob/master/doc/weixin-donate.png)
