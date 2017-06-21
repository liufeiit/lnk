package io.lnk.protocol.broker;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.InvokerCommand;
import io.lnk.api.ProtocolObject;
import io.lnk.api.RemoteObject;
import io.lnk.api.RemoteStub;
import io.lnk.api.broker.BrokerArg;
import io.lnk.api.broker.BrokerCommand;
import io.lnk.api.exception.transport.CommandTransportException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.broker.BrokerProtocolFactory;
import io.lnk.api.protocol.object.ObjectProtocolFactory;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.api.utils.NetUtils;
import io.lnk.protocol.Serializer;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 下午6:10:32
 */
public abstract class BasicBrokerProtocolFactory implements BrokerProtocolFactory {
    protected final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());
    protected String ip;
    protected ClassLoader classLoader;
    protected final Serializer serializer;
    protected final String protocol;

    protected BasicBrokerProtocolFactory(String protocol, Serializer serializer) {
        super();
        this.protocol = protocol;
        this.serializer = serializer;
        this.ip = NetUtils.getLocalAddress().getHostAddress();
        this.classLoader = this.getClass().getClassLoader();
    }

    @Override
    public InvokerCommand encode(BrokerCommand command, ObjectProtocolFactory objectProtocolFactory, ProtocolFactory protocolFactory) throws Throwable {
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
            invokerCommand.setArgs(objectProtocolFactory.encode(commandArgs, protocolFactory));
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
        ProtocolObject retObject = command.getRetObject();
        if (retObject != null) {
            brokerCommand.setRetObject(this.serializer.serializeAsString(protocolFactory.decode(retObject.getType(), retObject.getData())));
        }
        CommandTransportException exception = command.getException();
        if (exception != null) {
            brokerCommand.setException(exception.getMessage());
        }
        return brokerCommand;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }
}
