package io.joyrpc.protocol.message;

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

import io.joyrpc.constants.Constants;
import io.joyrpc.exception.LafException;
import io.joyrpc.exception.MethodOverloadException;
import io.joyrpc.extension.MapParametric;
import io.joyrpc.extension.Parametric;
import io.joyrpc.extension.URL;
import io.joyrpc.transport.session.DefaultSession;
import io.joyrpc.util.ClassUtils;
import io.joyrpc.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.joyrpc.GenericService.GENERIC;
import static io.joyrpc.constants.Constants.*;
import static io.joyrpc.util.ClassUtils.*;

/**
 * 调用对象
 */
public class Invocation implements Serializable {

    public static final String CLAZZ_NAME = "clazzName";

    public static final String CLASS_NAME = "className";

    public static final String METHOD_NAME = "methodName";

    public static final String ALIAS = "alias";

    public static final String ARGS_TYPE = "argsType";

    public static final String METHOD_SIGN = "methodSign";

    public static final String ARGS = "args";

    public static final String ATTACHMENTS = "attachments";

    /**
     * 类名
     */
    protected String className;
    /**
     * 分组别名
     */
    protected String alias;
    /**
     * 方法名称
     */
    protected String methodName;
    /**
     * 参数类型字符串
     */
    protected String[] argsType; //考虑优化class？
    /**
     * 参数类型
     */
    protected transient Class[] argClasses;
    /**
     * 参数对象
     */
    protected Object[] args;
    /**
     * 扩展属性，默认为空，很多情况不需要，加快性能
     */
    protected Map<String, Object> attachments;
    /**
     * 方法对象，泛化调用的时候该对象可能为null
     */
    protected transient Method method;
    /**
     * 所在的类，泛化调用的时候该对象可能为null
     */
    protected transient Class clazz;
    /**
     * 调用的目标对象
     */
    protected transient Object object;
    /**
     * 是否泛化调用
     */
    protected transient Boolean generic;

    public Invocation() {
    }

    public Invocation(Map<String, Object> attachments) {
        this.attachments = attachments;
    }

    /**
     * 构造函数
     *
     * @param className
     * @param alias
     * @param methodName
     */
    public Invocation(String className, String alias, String methodName) {
        this(className, alias, methodName, null);
    }

    /**
     * 构造函数
     *
     * @param className
     * @param alias
     * @param methodName
     * @param argClasses
     */
    public Invocation(String className, String alias, String methodName, Class[] argClasses) {
        this.className = className;
        this.alias = alias;
        this.methodName = methodName;
        if (argClasses != null) {
            //TODO 占用CPU
            setArgsType(argClasses);
        }
    }

    /**
     * 构造函数
     *
     * @param iface
     * @param method
     * @param args
     */
    public Invocation(final Class iface, final Method method, final Object[] args) {
        this(iface, null, method, args, method.getParameterTypes());
    }

    /**
     * 构造函数
     *
     * @param iface
     * @param alias
     * @param method
     * @param args
     */
    public Invocation(final Class iface, final String alias, final Method method, final Object[] args) {
        this(iface, alias, method, args, method.getParameterTypes());
    }

    /**
     * 构造函数
     *
     * @param iface
     * @param alias
     * @param method
     * @param args
     * @param argTypes
     */
    public Invocation(final Class iface, final String alias, final Method method, final Object[] args, final Class[] argTypes) {
        this.clazz = iface;
        this.className = iface.getName();
        this.alias = alias;
        this.method = method;
        this.methodName = method.getName();
        this.args = args == null ? new Object[0] : args;
        setArgsType(argTypes);
    }

    public String[] getArgsType() {
        return argsType;
    }

    public void setArgsType(String[] argsType) {
        this.argsType = argsType;
        // 清空缓存
        this.argClasses = null;
    }

    /**
     * 设置参数类型，只在回调函数和解析请求的时候主动设置
     *
     * @param argsType
     */
    public void setArgsType(Class[] argsType) {
        if (argsType == null) {
            this.argsType = new String[0];
            this.argClasses = new Class[0];
        } else {
            this.argClasses = argsType;
            this.argsType = getNames(argsType);
        }
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Class[] getArgClasses() {
        if (argClasses == null) {
            //TODO 要以argsType优先，因为方法上的参数类型可能为接口
            if (method != null) {
                argClasses = method.getParameterTypes();
            } else if (argsType != null) {
                try {
                    argClasses = ClassUtils.getClasses(argsType);
                } catch (Exception e) {
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
        return argClasses;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    /**
     * 如果参数类型不存在，则进行计算
     *
     * @return
     */
    public String[] computeArgsType() {
        if (argsType == null) {
            if (argClasses != null) {
                argsType = getNames(argClasses);
            } else if (method != null) {
                argsType = getNames(method.getParameterTypes());
            }
        }
        return argsType;
    }

    /**
     * 添加扩展信息
     *
     * @param key
     * @param value
     * @return
     */
    public Invocation addAttachment(final String key, final Object value) {
        if (key != null && value != null) {
            if (attachments == null) {
                attachments = new HashMap<>();
            }
            attachments.put(key, value);
        }
        return this;
    }

    /**
     * 添加扩展属性
     *
     * @param map 参数
     */
    public void addAttachments(final Map<String, ?> map) {
        if (map != null) {
            if (attachments == null) {
                attachments = new HashMap<>();
            }
            attachments.putAll(map);
        }
    }

    /**
     * 获取扩展属性
     *
     * @param key
     * @param function
     * @param <T>
     * @return
     */
    public <T> T computeIfAbsent(final String key, final Function<String, T> function) {
        if (key == null) {
            return null;
        }
        if (attachments != null) {
            attachments = new HashMap<>();
        }
        return (T) (function == null ? attachments.get(key) : attachments.computeIfAbsent(key, function));
    }


    /**
     * 移除扩展信息
     *
     * @param key
     * @return
     */
    public Object removeAttachment(final String key) {
        return key == null || attachments == null ? null : attachments.remove(key);
    }

    /**
     * 获取扩展属性
     *
     * @return
     */
    public Map<String, Object> getAttachments() {
        return attachments;
    }

    /**
     * 获取扩展属性
     *
     * @param key
     * @return
     */
    public <T> T getAttachment(final String key) {
        return attachments == null ? null : (T) attachments.get(key);
    }

    /**
     * 获取扩展属性
     *
     * @param key
     * @param defaultValue 默认值
     * @return
     */
    public <T> T getAttachment(final String key, final T defaultValue) {
        if (attachments == null) {
            return defaultValue;
        } else {
            T result = (T) attachments.get(key);
            return result == null ? defaultValue : result;
        }
    }

    public Parametric asParametric() {
        return new MapParametric(attachments);
    }

    /**
     * 判断是否是泛型
     *
     * @return
     */
    public boolean isGeneric() {
        if (generic == null) {
            generic = GENERIC.test(clazz)
                    || Boolean.TRUE.equals(getAttachment(Constants.GENERIC_OPTION.getName()));
        }
        return generic;
    }

    /**
     * 方法调用
     *
     * @param target
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public Object invoke(final Object target) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, args);
    }

    /**
     * 从会话恢复信心
     *
     * @param session
     */
    public void apply(final DefaultSession session) {
        if (session == null) {
            return;
        }
        //类名，如果不存在则从会话里面获取
        if (className == null || className.isEmpty()) {
            className = session.getInterfaceName();
        }
        if (alias == null || alias.isEmpty()) {
            alias = session.getAlias();
        }
        if (getAttachment(HIDDEN_KEY_APPID) == null && getAttachment(HIDDEN_KEY_APPNAME) == null) {
            addAttachment(HIDDEN_KEY_APPID, session.getRemoteAppId());
            addAttachment(HIDDEN_KEY_APPNAME, session.getRemoteAppName());
            addAttachment(HIDDEN_KEY_APPINSID, session.getRemoteAppIns());
        }
    }

    /**
     * 构建HTTP请求
     *
     * @param url
     * @param headers
     * @param supplier
     * @return
     * @throws ClassNotFoundException
     * @throws NoSuchMethodException
     * @throws MethodOverloadException
     */
    public static Invocation build(final URL url, final Map<CharSequence, Object> headers, final Supplier<LafException> supplier)
            throws ClassNotFoundException, NoSuchMethodException, MethodOverloadException {
        Parametric parametric = new MapParametric(headers);
        String path = url.getPath();
        String[] parts = path == null ? new String[0] : StringUtils.split(path, '/');
        String className;
        String alias;
        String methodName;
        if (parts.length > 2) {
            className = parts[0];
            alias = parts[1];
            methodName = parts[2];
        } else if (parts.length == 2) {
            className = parts[0];
            methodName = parts[1];
            alias = parametric.getString("alias", null);
            if (alias == null || alias.isEmpty()) {
                throw supplier.get();
            }
        } else {
            throw supplier.get();
        }
        Class ifaceClass = forName(className);
        Method method = getPublicMethod(ifaceClass, methodName);
        Class[] paramTypes = method.getParameterTypes();

        Invocation invocation = new Invocation(className, alias, methodName, paramTypes).
                addAttachment(Constants.HIDDEN_KEY_TOKEN, parametric.getString(KEY_TOKEN)).
                addAttachment(HIDDEN_KEY_APPID, parametric.getString(KEY_APPID)).
                addAttachment(HIDDEN_KEY_APPNAME, parametric.getString(KEY_APPNAME)).
                addAttachment(HIDDEN_KEY_APPINSID, parametric.getString(KEY_APPINSID));
        invocation.setClazz(ifaceClass);
        invocation.setMethod(method);
        //隐式传参
        String key;
        for (Map.Entry<CharSequence, Object> entry : headers.entrySet()) {
            key = entry.getKey().toString();
            if (!key.isEmpty() && key.charAt(0) == Constants.HIDE_KEY_PREFIX) {
                invocation.addAttachment(key, entry.getValue());
            }
        }
        return invocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Invocation that = (Invocation) o;

        if (className != null ? !className.equals(that.className) : that.className != null) {
            return false;
        }
        if (alias != null ? !alias.equals(that.alias) : that.alias != null) {
            return false;
        }
        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(argsType, that.argsType)) {
            return false;
        }
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(args, that.args)) {
            return false;
        }
        return attachments != null ? attachments.equals(that.attachments) : that.attachments == null;

    }

    @Override
    public int hashCode() {
        int result = className != null ? className.hashCode() : 0;
        result = 31 * result + (alias != null ? alias.hashCode() : 0);
        result = 31 * result + (methodName != null ? methodName.hashCode() : 0);
        result = 31 * result + Arrays.hashCode(argsType);
        result = 31 * result + Arrays.hashCode(args);
        result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Invocation{");
        sb.append("className='").append(className).append('\'');
        sb.append(", alias='").append(alias).append('\'');
        sb.append(", methodName='").append(methodName).append('\'');
        sb.append(", argsType=").append(Arrays.toString(argsType));
        sb.append(", args=").append(Arrays.toString(args));
        sb.append(", attachments=").append(attachments);
        sb.append('}');
        return sb.toString();
    }
}
