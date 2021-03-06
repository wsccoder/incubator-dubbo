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
package org.apache.dubbo.rpc.filter;

import org.apache.dubbo.common.Constants;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Filter;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.RpcStatus;

import java.util.concurrent.Semaphore;

/**
 * ThreadLimitInvokerFilter
 *
 * 服务提供者每服务、每方法的最大可并行执行请求数的过滤器实现类。
 *
 */
@Activate(group = Constants.PROVIDER, value = Constants.EXECUTES_KEY)
public class ExecuteLimitFilter implements Filter {

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        URL url = invoker.getUrl();
        String methodName = invocation.getMethodName();
        // 信号量
        Semaphore executesLimit = null;
        // 是否获得信号量
        boolean acquireResult = false;
        // 获得服务提供者每服务每方法最大可并行执行请求数
        int max = url.getMethodParameter(methodName, Constants.EXECUTES_KEY, 0);
        if (max > 0) {
            // 获得 RpcStatus 对象，基于服务 URL + 方法维度
            RpcStatus count = RpcStatus.getStatus(url, invocation.getMethodName());
            // 获得信号量。若获得不到，抛出异常。
//            if (count.getActive() >= max) {
            /**
             * http://manzhizhen.iteye.com/blog/2386408
             * use semaphore for concurrency control (to limit thread number)
             */
            executesLimit = count.getSemaphore(max);
            if(executesLimit != null && !(acquireResult = executesLimit.tryAcquire())) {
                throw new RpcException("Failed to invoke method " + invocation.getMethodName() + " in provider " + url + ", cause: The service using threads greater than <dubbo:service executes=\"" + max + "\" /> limited.");
            }
        }
        long begin = System.currentTimeMillis();
        boolean isSuccess = true;
        // 调用开始的计数
        RpcStatus.beginCount(url, methodName);
        try {
            // 服务调用
            Result result = invoker.invoke(invocation);
            return result;
        } catch (Throwable t) {
            // 标记失败
            isSuccess = false;
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            } else {
                throw new RpcException("unexpected exception when ExecuteLimitFilter", t);
            }
        } finally {
            // 调用结束的计数（成功）（失败）
            RpcStatus.endCount(url, methodName, System.currentTimeMillis() - begin, isSuccess);
            // 释放信号量
            if(acquireResult) {
                executesLimit.release();
            }
        }
    }

}
