package io.joyrpc.config;

/*-
 * #%L
 * joyrpc
 * %%
 * Copyright (C) 2019 joyrpc.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import io.joyrpc.cache.CacheFactory;
import io.joyrpc.config.validator.ValidatePlugin;
import io.joyrpc.constants.Constants;
import io.joyrpc.filter.cache.CacheKeyGenerator;

import java.util.HashMap;
import java.util.Map;

import static io.joyrpc.constants.Constants.*;

/**
 * 方法配置，用于指定方法下的配置，覆盖接口配置
 */
public class MethodConfig extends AbstractConfig {

    /**
     * 方法名称，无法做到重载方法的配置
     */
    protected String name;
    /**
     * The Parameters. 自定义参数
     */
    protected Map<String, String> parameters;
    /**
     * The Timeout. 远程调用超时时间(毫秒)
     */
    protected Integer timeout;
    /**
     * The Retries. 失败后重试次数
     */
    protected Integer retries;
    /**
     * The Validation. 是否jsr303验证
     */
    protected Boolean validation;
    /**
     * The concurrency. 最大并发执行（不管服务端还是客户端）
     */
    protected Integer concurrency;
    /**
     * 结果缓存插件名称
     */
    @ValidatePlugin(extensible = CacheFactory.class, name = "CACHE", defaultValue = DEFAULT_CACHE_PROVIDER)
    protected String cacheProvider;
    /**
     * cache key 生成器
     */
    @ValidatePlugin(extensible = CacheKeyGenerator.class, name = "CACHE_KEY_GENERATOR", defaultValue = DEFAULT_CACHE_KEY_GENERATOR)
    protected String cacheKeyGenerator;
    /**
     * 是否启动结果缓存
     */
    protected Boolean cache;
    /**
     * cache过期时间
     */
    protected Long cacheExpireTime;
    /**
     * cache最大容量
     */
    protected Integer cacheCapacity;
    /**
     * 是否启动压缩
     */
    protected String compress;

    /**
     * 目标参数（机房/分组）索引，第一个参数从0开始
     */
    protected Integer dstParam;

    /**
     * 缓存值是否可空
     */
    protected Boolean cacheNullable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getRetries() {
        return retries;
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Integer getConcurrency() {
        return concurrency;
    }

    public void setConcurrency(Integer concurrency) {
        this.concurrency = concurrency;
    }

    public Boolean getCache() {
        return cache;
    }

    public void setCache(Boolean cache) {
        this.cache = cache;
    }

    public void setValidation(Boolean validation) {
        this.validation = validation;
    }

    public Boolean getValidation() {
        return validation;
    }

    public String getCompress() {
        return compress;
    }

    public void setCompress(String compress) {
        this.compress = compress;
    }

    public String getCacheKeyGenerator() {
        return cacheKeyGenerator;
    }

    public void setCacheKeyGenerator(String cacheKeyGenerator) {
        this.cacheKeyGenerator = cacheKeyGenerator;
    }

    public Long getCacheExpireTime() {
        return cacheExpireTime;
    }

    public void setCacheExpireTime(Long cacheExpireTime) {
        this.cacheExpireTime = cacheExpireTime;
    }

    public Integer getCacheCapacity() {
        return cacheCapacity;
    }

    public void setCacheCapacity(Integer cacheCapacity) {
        this.cacheCapacity = cacheCapacity;
    }

    public Integer getDstParam() {
        return dstParam;
    }

    public void setDstParam(Integer dstParam) {
        this.dstParam = dstParam;
    }

    public Boolean getCacheNullable() {
        return cacheNullable;
    }

    public void setCacheNullable(Boolean cacheNullable) {
        this.cacheNullable = cacheNullable;
    }

    public String getCacheProvider() {
        return cacheProvider;
    }

    public void setCacheProvider(String cacheProvider) {
        this.cacheProvider = cacheProvider;
    }

    /**
     * Sets parameter.
     *
     * @param key   the key
     * @param value the value
     */
    public void setParameter(String key, String value) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        parameters.put(key, value);
    }

    /**
     * Gets parameter.
     *
     * @param key the key
     * @return the value
     */
    public String getParameter(String key) {
        return parameters == null ? null : parameters.get(key);
    }

    @Override
    protected Map<String, String> addAttribute2Map(final Map<String, String> params) {
        super.addAttribute2Map(params);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.RETRIES_OPTION.getName()), retries);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.TIMEOUT_OPTION.getName()), timeout);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.VALIDATION_OPTION.getName()), validation);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CONCURRENCY_OPTION.getName()), concurrency);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.COMPRESS_OPTION.getName()), compress);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.DST_PARAM_OPTION.getName()), dstParam);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_OPTION.getName()), cache);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_PROVIDER_OPTION.getName()), cacheProvider);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_KEY_GENERATOR_OPTION.getName()), cacheKeyGenerator);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_EXPIRE_TIME_OPTION.getName()), cacheExpireTime);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_CAPACITY_OPTION.getName()), cacheCapacity);
        addElement2Map(params, METHOD_KEY.apply(name, Constants.CACHE_NULLABLE_OPTION.getName()), cacheNullable);

        if (null != parameters) {
            parameters.forEach((k, v) -> addElement2Map(params, METHOD_KEY.apply(name, k), v));
        }
        return params;
    }
}
