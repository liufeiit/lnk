package io.lnk.core.protocol;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;

import io.lnk.api.CommandReturnObject;
import io.lnk.api.InvokerCommand;
import io.lnk.api.RemoteObject;
import io.lnk.api.broker.BrokerArg;
import io.lnk.api.broker.BrokerCommand;
import io.lnk.api.exception.transport.CommandTransportException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.core.BrokerCommandProtocolFactory;
import io.lnk.core.CommandArgProtocolFactory;
import io.lnk.core.caller.RemoteStub;
import io.lnk.protocol.Serializer;
import io.lnk.protocol.jackson.JacksonSerializer;
import io.lnk.remoting.utils.RemotingUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:50:18
 */
public class LnkBrokerCommandProtocolFactory implements BrokerCommandProtocolFactory, InitializingBean, BeanClassLoaderAware {
    private String ip;
    private ClassLoader classLoader;
    private Serializer serializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.ip = RemotingUtils.getLocalAddress();
        this.serializer = new JacksonSerializer();
    }

    @Override
    public InvokerCommand encode(BrokerCommand command, CommandArgProtocolFactory commandArgProtocolFactory, ProtocolFactory protocolFactory) throws Throwable {
        InvokerCommand invokerCommand = new InvokerCommand();
        invokerCommand.setId(StringUtils.defaultIfBlank(command.getId(), CorrelationIds.buildGuid()));
        invokerCommand.setIp(StringUtils.defaultIfBlank(command.getIp(), this.ip));
        invokerCommand.setApplication(command.getApplication());
        invokerCommand.setVersion(command.getVersion());
        invokerCommand.setProtocol(command.getProtocol());
        invokerCommand.setServiceGroup(command.getServiceGroup());
        invokerCommand.setServiceId(command.getServiceId());
        invokerCommand.setMethod(command.getMethod());
        String[] signature = command.getSignature();
        if (ArrayUtils.isNotEmpty(signature)) {
            int signatureLength = signature.length;
            Class<?>[] methodSignature = new Class<?>[signatureLength];
            for (int i = 0; i < signatureLength; i++) {
                methodSignature[i] = this.classLoader.loadClass(signature[i]);
            }
            invokerCommand.setSignature(methodSignature);
        }
        BrokerArg[] args = command.getArgs();
        if (ArrayUtils.isNotEmpty(args)) {
            int argsLength = args.length;
            Object[] commandArgs = new Object[argsLength];
            for (int i = 0; i < argsLength; i++) {
                BrokerArg arg = args[i];
                String type = arg.getType();
                Object obj = null;
                if (StringUtils.equals(type, RemoteObject.class.getName())) {
                    obj = new RemoteStub(arg.getArg());
                } else {
                    obj = this.serializer.deserialize(this.classLoader.loadClass(type), arg.getArg());
                }
                commandArgs[i] = obj;
            }
            invokerCommand.setArgs(commandArgProtocolFactory.encode(commandArgs, protocolFactory));
        }
        return invokerCommand;
    }

    @Override
    public BrokerCommand decode(InvokerCommand command, ProtocolFactory protocolFactory) throws Throwable {
        BrokerCommand brokerCommand = new BrokerCommand();
        brokerCommand.setId(command.getId());
        brokerCommand.setIp(command.getIp());
        brokerCommand.setApplication(command.getApplication());
        brokerCommand.setVersion(command.getVersion());
        brokerCommand.setProtocol(command.getProtocol());
        brokerCommand.setServiceGroup(command.getServiceGroup());
        brokerCommand.setServiceId(command.getServiceId());
        brokerCommand.setMethod(command.getMethod());
        CommandReturnObject retObject = command.getRetObject();
        if (retObject != null) {
            brokerCommand.setRetObject(this.serializer.serializeAsString(protocolFactory.decode(retObject.getType(), retObject.getRetObject())));
        }
        CommandTransportException exception = command.getException();
        if (exception != null) {
            brokerCommand.setException(exception.getMessage());
        }
        return brokerCommand;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
