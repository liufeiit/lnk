package io.lnk.core.processor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import io.lnk.api.CommandReturnObject;
import io.lnk.api.InvokerCommand;
import io.lnk.api.app.Application;
import io.lnk.api.exception.transport.CommandTransportException;
import io.lnk.api.flow.FlowController;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.ProtocolFactorySelector;
import io.lnk.api.track.Tracker;
import io.lnk.core.CommandArgProtocolFactory;
import io.lnk.core.ServiceObjectFinder;
import io.lnk.remoting.CommandProcessor;
import io.lnk.remoting.protocol.RemotingCommand;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月22日 下午4:09:36
 */
public class LnkCommandProcessor implements CommandProcessor {
    private static final Logger log = LoggerFactory.getLogger(LnkCommandProcessor.class.getSimpleName());
    private ProtocolFactorySelector protocolFactorySelector;
    private ServiceObjectFinder serviceObjectFinder;
    private FlowController flowController;
    private Application application;
    private Tracker tracker;
    private CommandArgProtocolFactory commandArgProtocolFactory;

    @Override
    public RemotingCommand processCommand(RemotingCommand request) throws Throwable {
        long startMillis = System.currentTimeMillis();
        ProtocolFactory protocolFactory = protocolFactorySelector.select(request.getProtocol());
        InvokerCommand command = protocolFactory.decode(InvokerCommand.class, request.getBody());
        this.trackInvokeBefore(command);
        Object serviceObject = serviceObjectFinder.getServiceObject(command);
        try {
            Method serviceMethod = ReflectionUtils.findMethod(serviceObject.getClass(), command.getMethod(), command.getSignature());
            Object retObject = serviceMethod.invoke(serviceObject, this.commandArgProtocolFactory.decode(command.getArgs(), protocolFactory));
            if (retObject != null) {
                CommandReturnObject commandReturnObject = new CommandReturnObject();
                commandReturnObject.setType(retObject.getClass());
                commandReturnObject.setRetObject(protocolFactory.encode(retObject));
                command.setRetObject(commandReturnObject);
            }
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException) e).getCause();
            }
            command.setException(new CommandTransportException(e));
            log.error("invoke correlationId<" + command.getId() + ">, serviceId<" + command.commandSignature() + "> Error.", e);
        }
        this.trackInvokeAfter(command);
        RemotingCommand response = RemotingCommand.replyCommand(request, request.getCode());
        response.setBody(protocolFactory.encode(command));
        long endMillis = System.currentTimeMillis();
        log.info("server invoker correlationId<{}>, serviceId<{}>, used {}(ms) success.", new Object[] {command.getId(), command.commandSignature(), (endMillis - startMillis)});
        return response;
    }
    
    private void trackInvokeBefore(InvokerCommand command) {
        if (tracker == null) {
            return;
        }
        try {
            tracker.trackInvokeBefore(command, application);
        } catch (Throwable e) {
            log.error("track serviceId<" + command.commandSignature() + "> invoke on application " + application.getApp() + " Error.", e);
        }
    }
    
    private void trackInvokeAfter(InvokerCommand command) {
        if (tracker == null) {
            return;
        }
        try {
            tracker.trackInvokeAfter(command, application);
        } catch (Throwable e) {
            log.error("track serviceId<" + command.commandSignature() + "> invoke on application " + application.getApp() + " Error.", e);
        }
    }

    @Override
    public boolean tryAcquireFailure(long timeoutMillis) {
        if (flowController == null) {
            return false;
        }
        return flowController.tryAcquireFailure(timeoutMillis);
    }

    @Override
    public void release() {
        if (flowController == null) {
            return;
        }
        flowController.release();
    }

    public void setProtocolFactorySelector(ProtocolFactorySelector protocolFactorySelector) {
        this.protocolFactorySelector = protocolFactorySelector;
    }

    public void setServiceObjectFinder(ServiceObjectFinder serviceObjectFinder) {
        this.serviceObjectFinder = serviceObjectFinder;
    }

    public void setFlowController(FlowController flowController) {
        this.flowController = flowController;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public void setTracker(Tracker tracker) {
        this.tracker = tracker;
    }
    
    public void setCommandArgProtocolFactory(CommandArgProtocolFactory commandArgProtocolFactory) {
        this.commandArgProtocolFactory = commandArgProtocolFactory;
    }
}
