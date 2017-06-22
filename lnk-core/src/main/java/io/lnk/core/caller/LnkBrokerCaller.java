package io.lnk.core.caller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.lnk.api.BrokerProtocols;
import io.lnk.api.InvokerCommand;
import io.lnk.api.annotation.LnkMethod;
import io.lnk.api.broker.BrokerCaller;
import io.lnk.api.broker.BrokerCommand;
import io.lnk.api.exception.LnkException;
import io.lnk.api.exception.LnkTimeoutException;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.protocol.Serializer;
import io.lnk.api.protocol.broker.BrokerProtocolFactory;
import io.lnk.api.protocol.broker.BrokerProtocolFactorySelector;
import io.lnk.api.protocol.object.ObjectProtocolFactory;
import io.lnk.core.LnkInvoker;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年6月21日 上午11:38:15
 */
public class LnkBrokerCaller implements BrokerCaller {
    private static final Logger log = LoggerFactory.getLogger(LnkBrokerCaller.class.getSimpleName());
    private LnkInvoker invoker;
    private BrokerProtocolFactorySelector brokerProtocolFactorySelector;
    private ProtocolFactorySelector protocolFactorySelector;
    private ObjectProtocolFactory objectProtocolFactory;

    @Override
    public String invoke(String command) throws LnkException, LnkTimeoutException {
        BrokerProtocolFactory brokerProtocolFactory = null;
        if (StringUtils.startsWith(command, "{") && StringUtils.endsWith(command, "}")) {
            brokerProtocolFactory = this.brokerProtocolFactorySelector.select(BrokerProtocols.JSON);
            Serializer serializer = brokerProtocolFactory.serializer();
            BrokerCommand response = this.invoke(serializer.deserialize(BrokerCommand.class, command));
            return serializer.serializeAsString(response);
        }
        return null;
    }

    @Override
    public BrokerCommand invoke(BrokerCommand command) throws LnkException, LnkTimeoutException {
        switch (command.getInvokeType()) {
            case BrokerCommand.SYNC:
                return this.sync(command);
            case BrokerCommand.ASYNC:
                this.async(command);
                break;
            case BrokerCommand.MULTICAST:
                this.multicast(command);
                break;
            default:
                break;
        }
        return BrokerCommand.NULL;
    }
    
    public BrokerCommand sync(BrokerCommand command) throws LnkException, LnkTimeoutException {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            long timeoutMillis = command.getTimeoutMillis();
            if (timeoutMillis <= 0L) {
                timeoutMillis = LnkMethod.DEFAULT_TIMEOUT_MILLIS;
            }
            BrokerProtocolFactory brokerProtocolFactory = this.brokerProtocolFactorySelector.select(command.getBrokerProtocol());
            InvokerCommand invokerCommand = this.invoker.sync(brokerProtocolFactory.encode(command, objectProtocolFactory, protocolFactory), timeoutMillis);
            return brokerProtocolFactory.decode(invokerCommand, protocolFactory);
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

    public void async(BrokerCommand command) throws LnkException, LnkTimeoutException {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            BrokerProtocolFactory brokerProtocolFactory = this.brokerProtocolFactorySelector.select(command.getBrokerProtocol());
            this.invoker.async(brokerProtocolFactory.encode(command, objectProtocolFactory, protocolFactory));
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

    public void multicast(BrokerCommand command) {
        try {
            ProtocolFactory protocolFactory = protocolFactorySelector.select(command.getProtocol());
            BrokerProtocolFactory brokerProtocolFactory = this.brokerProtocolFactorySelector.select(command.getBrokerProtocol());
            this.invoker.multicast(brokerProtocolFactory.encode(command, objectProtocolFactory, protocolFactory));
        } catch (Throwable e) {
            log.error("invoker multicast correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
            throw new LnkException("invoker multicast correlationId<" + command.getId() + ">, serviceId<" + command.getServiceId() + "> " + e.getLocalizedMessage(), e);
        }
    }

    public void setInvoker(LnkInvoker invoker) {
        this.invoker = invoker;
    }

    public void setBrokerProtocolFactorySelector(BrokerProtocolFactorySelector brokerProtocolFactorySelector) {
        this.brokerProtocolFactorySelector = brokerProtocolFactorySelector;
    }
    
    public void setProtocolFactorySelector(ProtocolFactorySelector protocolFactorySelector) {
        this.protocolFactorySelector = protocolFactorySelector;
    }
    
    public void setObjectProtocolFactory(ObjectProtocolFactory objectProtocolFactory) {
        this.objectProtocolFactory = objectProtocolFactory;
    }
}
