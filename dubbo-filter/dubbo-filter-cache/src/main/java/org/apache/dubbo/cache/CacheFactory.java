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
package org.apache.dubbo.cache;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.extension.Adaptive;
import org.apache.dubbo.common.extension.SPI;
import org.apache.dubbo.rpc.Invocation;

/**
 * CacheFactory
 *
 * Cache 工厂接口  默认为lru
 */
@SPI("lru")
public interface CacheFactory {

    /**
     * 获得缓存对象
     *
     * @param url URL 对象
     * @return 缓存对象
     */
    //基于 Dubbo SPI Adaptive 机制，加载对应的 Cache 实现
    @Adaptive("cache")
    Cache getCache(URL url, Invocation invocation);

}
