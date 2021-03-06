/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.dubbo.rpc.protocol.injvm;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.utils.NetUtils;
import org.apache.dubbo.rpc.Exporter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.protocol.AbstractInvoker;

import java.util.Map;

/**
 * InjvmInvoker
 *
 * 实现 AbstractInvoker 抽象类，Injvm Invoker 实现类
 *
 */
class InjvmInvoker<T> extends AbstractInvoker<T> {

    /**
     * 服务键
     */
    private final String key;

    /**
     * Exporter 集合
     *
     * key: 服务键
     *
     * 该值实际就是 {@link com.alibaba.dubbo.rpc.protocol.AbstractProtocol#exporterMap}
     */
    private final Map<String, Exporter<?>> exporterMap;

    InjvmInvoker(Class<T> type, URL url, String key, Map<String, Exporter<?>> exporterMap) {
        super(type, url);
        this.key = key;
        this.exporterMap = exporterMap;
    }
    
    /** 
    * @Description: 是否可用
     *
     * 开启 启动时检查 时，调用该方法，判断该 Invoker 对象，是否有对应的 Exporter
     *
    * @Param: [] 
    * @return: boolean 
    * @Author: wangsc
    * @Date: 2018/9/18 
    */ 
    @Override
    public boolean isAvailable() {
        // 判断是否有 Exporter 对象
        InjvmExporter<?> exporter = (InjvmExporter<?>) exporterMap.get(key);
        if (exporter == null) {
            return false;
        } else {
            return super.isAvailable();
        }
    }

    @Override
    public Result doInvoke(Invocation invocation) throws Throwable {
        Exporter<?> exporter = InjvmProtocol.getExporter(exporterMap, getUrl());
        if (exporter == null) {
            throw new RpcException("Service [" + key + "] not found.");
        }
        RpcContext.getContext().setRemoteAddress(NetUtils.LOCALHOST, 0);
        return exporter.getInvoker().invoke(invocation);
    }
}
