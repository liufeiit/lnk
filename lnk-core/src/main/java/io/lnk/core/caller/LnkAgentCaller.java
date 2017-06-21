package io.lnk.core.caller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.InvokerCommand;
import io.lnk.api.agent.AgentCaller;
import io.lnk.api.agent.AgentCommand;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkTimeoutException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.core.AgentCommandProtocolFactory;
import io.lnk.core.CommandArgProtocolFactory;
import io.lnk.core.LnkInvoker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:38:15
 */
public class LnkAgentCaller implements AgentCaller {
    private static final Logger log = LoggerFactory.getLogger(LnkAgentCaller.class.getSimpleName());
    private LnkInvoker invoker;
    private AgentCommandProtocolFactory agentCommandProtocolFactory;
    private ProtocolFactorySelector protocolFactorySelector;
    private CommandArgProtocolFactory commandArgProtocolFactory;

    @Override
    public AgentCommand sync(AgentCommand command) throws LnkException, LnkTimeoutException {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            long timeoutMillis = command.getTimeoutMillis();
            if (timeoutMillis <= 0L) {
                timeoutMillis = LnkMethod.DEFAULT_TIMEOUT_MILLIS;
            }
            InvokerCommand invokerCommand = this.invoker.sync(this.agentCommandProtocolFactory.encode(command, commandArgProtocolFactory, protocolFactory), timeoutMillis);
            return this.agentCommandProtocolFactory.decode(invokerCommand, protocolFactory);
        } catch (Throwable e) {
            log.error("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
            if (e instanceof LnkException) {
                throw (LnkException) e;
            }
            if (e instanceof LnkTimeoutException) {
                throw (LnkTimeoutException) e;
            }
            throw new LnkException("invoker sync correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void async(AgentCommand command) throws LnkException, LnkTimeoutException {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            this.invoker.async(this.agentCommandProtocolFactory.encode(command, commandArgProtocolFactory, protocolFactory));
        } catch (Throwable e) {
            log.error("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
            if (e instanceof LnkException) {
                throw (LnkException) e;
            }
            if (e instanceof LnkTimeoutException) {
                throw (LnkTimeoutException) e;
            }
            throw new LnkException("invoker async correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
        }
    }

    @Override
    public void async_multicast(AgentCommand command) {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            this.invoker.async_multicast(this.agentCommandProtocolFactory.encode(command, commandArgProtocolFactory, protocolFactory));
        } catch (Throwable e) {
            log.error("invoker async_multicast correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker async_multicast correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
        }
    }

    public void setInvoker(LnkInvoker invoker) {
        this.invoker = invoker;
    }

    public void setAgentCommandProtocolFactory(AgentCommandProtocolFactory agentCommandProtocolFactory) {
        this.agentCommandProtocolFactory = agentCommandProtocolFactory;
    }
    
    public void setProtocolFactorySelector(ProtocolFactorySelector protocolFactorySelector) {
        this.protocolFactorySelector = protocolFactorySelector;
    }
    
    public void setCommandArgProtocolFactory(CommandArgProtocolFactory commandArgProtocolFactory) {
        this.commandArgProtocolFactory = commandArgProtocolFactory;
    }
}
