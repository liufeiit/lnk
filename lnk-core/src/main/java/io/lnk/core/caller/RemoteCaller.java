package io.lnk.core.caller;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.ReflectionUtils;

import io.lnk.api.InvokeType;
import io.lnk.api.InvokerCommand;
import io.lnk.api.ProtocolObject;
import io.lnk.api.RemoteObject;
import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.RemoteStub;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.exception.AppBizException;
import io.lnk.api.exception.AppBizRtException;
import io.lnk.api.exception.ex.SimpleLnkException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.protocol.object.ObjectProtocolFactory;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.core.LnkEndpoint;
import io.lnk.core.MdcCaller;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午9:12:40
 */
public class RemoteCaller implements InvocationHandler {
    private final LnkEndpoint endpoint;
    private final RemoteStub remoteObject;
    private final ProtocolFactorySelector protocolFactorySelector;
    private RemoteObjectFactory remoteObjectFactory;
    private ProtocolFactory protocolFactory;
    private ObjectProtocolFactory objectProtocolFactory;

    public RemoteCaller(LnkEndpoint endpoint, String serializeStub, ProtocolFactorySelector protocolFactorySelector) {
        this(endpoint, new RemoteStub(serializeStub), protocolFactorySelector);
    }

    public RemoteCaller(LnkEndpoint endpoint, RemoteStub remoteObject, ProtocolFactorySelector protocolFactorySelector) {
        super();
        this.endpoint = endpoint;
        this.remoteObject = remoteObject;
        this.protocolFactorySelector = protocolFactorySelector;
        this.protocolFactory = this.protocolFactorySelector.select(this.remoteObject.getProtocol());
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Class<?> declaringClass = method.getDeclaringClass();
        if (declaringClass.equals(RemoteObjectFactory.class)) {
            return method.invoke(this.remoteObjectFactory, args);
        }
        if (declaringClass.equals(RemoteObject.class)) {
            return method.invoke(this.remoteObject, args);
        }
        if (ReflectionUtils.isToStringMethod(method)) {
            return this.remoteObject.getServiceId() + "@" + Integer.toHexString(hashCode());
        }
        if (declaringClass.equals(Object.class)) {
            return method.invoke(this, args);
        }
        InvokeType type = InvokeType.SYNC;
        long timeoutMillis = LnkMethod.DEFAULT_TIMEOUT_MILLIS;
        if (method.isAnnotationPresent(LnkMethod.class)) {
            LnkMethod lnkMethod = method.getAnnotation(LnkMethod.class);
            type = lnkMethod.type();
            timeoutMillis = lnkMethod.timeoutMillis();
        }
        final Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            if (type != InvokeType.MULTICAST) {
                type = InvokeType.ASYNC;
            }
        }
        String id = CorrelationIds.buildGuid();
        MdcCaller.setTrackingCode(id);
        InvokerCommand command = new InvokerCommand();
        command.setId(id);
        command.setVersion(this.remoteObject.getVersion());
        command.setProtocol(this.remoteObject.getProtocol());
        command.setServiceGroup(this.remoteObject.getServiceGroup());
        command.setServiceId(this.remoteObject.getServiceId());
        command.setMethod(method.getName());
        command.setSignature(method.getParameterTypes());
        command.setArgs(this.objectProtocolFactory.encode(args, protocolFactory));
        try {
            switch (type) {
                case SYNC: {
                    return this.sync(command, timeoutMillis);
                }
                case ASYNC: {
                    this.endpoint.async(command);
                }
                    break;
                case MULTICAST: {
                    this.endpoint.multicast(command);
                }
                    break;
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getCause();
            }
            throw e;
        }
        return null;
    }

    private Object sync(InvokerCommand command, long timeoutMillis) throws Throwable {
        InvokerCommand response = this.endpoint.sync(command, timeoutMillis);
        SimpleLnkException exception = response.getException();
        if (exception != null) {
            Throwable e;
            @SuppressWarnings("unchecked")
            Class<? extends Throwable> eClass = (Class<? extends Throwable>) Class.forName(exception.getClassName());
            Constructor<? extends Throwable> constructor = eClass.getConstructor(String.class);
            if (constructor == null || exception.getMessage() == null) {
                e = eClass.newInstance();
            } else {
                e = constructor.newInstance(exception.getMessage());
            }
            e.setStackTrace(exception.buildStackTraceElement());
            if (e instanceof AppBizException) {
                ((AppBizException) e).setCode(exception.getCode());
            }
            if (e instanceof AppBizRtException) {
                ((AppBizRtException) e).setCode(exception.getCode());
            }
            throw e;
        }
        ProtocolObject retObject = response.getRetObject();
        if (retObject != null) {
            byte[] retObjectBytes = retObject.getData();
            if (ArrayUtils.isNotEmpty(retObjectBytes)) {
                return protocolFactory.decode(retObject.getType(), retObjectBytes);
            }
        }
        return null;
    }

    public RemoteStub getRemoteObject() {
        return remoteObject;
    }

    public void setRemoteObjectFactory(RemoteObjectFactory remoteObjectFactory) {
        this.remoteObjectFactory = remoteObjectFactory;
    }

    public void setObjectProtocolFactory(ObjectProtocolFactory objectProtocolFactory) {
        this.objectProtocolFactory = objectProtocolFactory;
    }
}
