package com.z2zz.mq.common.util;


import com.github.houbb.heaven.annotation.CommonEager;
import com.github.houbb.heaven.util.lang.StringUtil;
import com.github.houbb.heaven.util.util.CollectionUtil;
import com.z2zz.loadbalance.api.ILoadBalance;
import com.z2zz.loadbalance.api.impl.LoadBalanceContext;
import com.z2zz.loadbalance.support.server.IServer;

import java.util.List;
import java.util.Objects;

@CommonEager
public class RandomUtils {

    /**
     * 负载均衡
     *
     * @param list 列表
     * @param key 分片键
     * @return
     */
    public static <T extends IServer> T loadBalance(final ILoadBalance<T> loadBalance,
                                                    final List<T> list, String key){
        if(CollectionUtil.isEmpty(list)){
            return null;
        }

        if(StringUtil.isEmpty(key)){
            LoadBalanceContext<T> loadBalanceContext = LoadBalanceContext.<T>newInstance().servers(list);
            return loadBalance.select(loadBalanceContext);
        }

        //获取code
        int hashCode = Objects.hash(key);
        int index = hashCode%list.size();
        return list.get(index);
    }


}
