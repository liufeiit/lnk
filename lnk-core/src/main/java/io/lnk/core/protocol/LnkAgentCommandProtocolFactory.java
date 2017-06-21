package io.lnk.core.protocol;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.InitializingBean;

import io.lnk.api.CommandReturnObject;
import io.lnk.api.InvokerCommand;
import io.lnk.api.agent.AgentArg;
import io.lnk.api.agent.AgentCommand;
import io.lnk.api.exception.transport.CommandTransportException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.utils.CorrelationIds;
import io.lnk.core.AgentCommandProtocolFactory;
import io.lnk.core.CommandArgProtocolFactory;
import io.lnk.protocol.Serializer;
import io.lnk.protocol.jackson.JacksonSerializer;
import io.lnk.remoting.utils.RemotingUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:50:18
 */
public class LnkAgentCommandProtocolFactory implements AgentCommandProtocolFactory, InitializingBean, BeanClassLoaderAware {
    private String ip;
    private ClassLoader classLoader;
    private Serializer serializer;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.ip = RemotingUtils.getLocalAddress();
        this.serializer = new JacksonSerializer();
    }

    @Override
    public InvokerCommand encode(AgentCommand command, CommandArgProtocolFactory commandArgProtocolFactory, ProtocolFactory protocolFactory) throws Throwable {
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
        AgentArg[] args = command.getArgs();
        if (ArrayUtils.isNotEmpty(args)) {
            int argsLength = args.length;
            Object[] commandArgs = new Object[argsLength];
            for (int i = 0; i < argsLength; i++) {
                AgentArg arg = args[i];
                Object obj = this.serializer.deserialize(this.classLoader.loadClass(arg.getType()), arg.getArg());
                commandArgs[i] = obj;
            }
            invokerCommand.setArgs(commandArgProtocolFactory.encode(commandArgs, protocolFactory));
        }
        return invokerCommand;
    }

    @Override
    public AgentCommand decode(InvokerCommand command, ProtocolFactory protocolFactory) throws Throwable {
        AgentCommand agentCommand = new AgentCommand();
        agentCommand.setId(command.getId());
        agentCommand.setIp(command.getIp());
        agentCommand.setApplication(command.getApplication());
        agentCommand.setVersion(command.getVersion());
        agentCommand.setProtocol(command.getProtocol());
        agentCommand.setServiceGroup(command.getServiceGroup());
        agentCommand.setServiceId(command.getServiceId());
        agentCommand.setMethod(command.getMethod());
        CommandReturnObject retObject = command.getRetObject();
        if (retObject != null) {
            agentCommand.setRetObject(this.serializer.serializeAsString(protocolFactory.decode(retObject.getType(), retObject.getRetObject())));
        }
        CommandTransportException exception = command.getException();
        if (exception != null) {
            agentCommand.setException(exception.getMessage());
        }
        return agentCommand;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
