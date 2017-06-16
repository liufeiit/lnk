package io.lnk.core.protocol;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import io.lnk.api.CommandArg;
import io.lnk.api.RemoteObject;
import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.core.CommandArgProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月30日 下午12:15:03
 */
public class LnkCommandArgProtocolFactory implements CommandArgProtocolFactory {
    private static final int COMMON_OBJECT_ARG = 1;
    private static final int REMOTE_OBJECT_ARG = 2;
    private final RemoteObjectFactory remoteObjectFactory;

    public LnkCommandArgProtocolFactory(RemoteObjectFactory remoteObjectFactory) {
        super();
        this.remoteObjectFactory = remoteObjectFactory;
    }

    @Override
    public CommandArg[] encode(final Object[] args, final ProtocolFactory protocolFactory) throws Throwable {
        CommandArg[] commandArgs = null;
        if (ArrayUtils.isEmpty(args)) {
            return commandArgs;
        }
        int argsNum = args.length;
        commandArgs = new CommandArg[argsNum];
        for (int i = 0; i < argsNum; i++) {
            Object arg = args[i];
            CommandArg commandArg = new CommandArg();
            if (arg == null) {
                commandArgs[i] = commandArg;
                continue;
            }
            commandArg.setType(arg.getClass());
            int type = COMMON_OBJECT_ARG;
            byte[] argBytes = null;
            if (arg instanceof RemoteObject) {
                commandArg.setType(RemoteObject.class);
                argBytes = protocolFactory.encode(((RemoteObject) arg).serializeStub());
                type = REMOTE_OBJECT_ARG;
            } else {
                argBytes = protocolFactory.encode(arg);
            }
            ByteBuffer argByteBuffer = ByteBuffer.allocate(argBytes.length + 4);
            argByteBuffer.putInt(type);
            argByteBuffer.put(argBytes);
            argByteBuffer.flip();
            commandArg.setArg(argByteBuffer.array());
            commandArgs[i] = commandArg;
        }
        return commandArgs;
    }

    @Override
    public Object[] decode(final CommandArg[] args, final ProtocolFactory protocolFactory) throws Throwable {
        Object[] invokeArgs = null;
        if (ArrayUtils.isEmpty(args)) {
            return invokeArgs;
        }
        int argsNum = args.length;
        invokeArgs = new Object[argsNum];
        for (int i = 0; i < argsNum; i++) {
            CommandArg arg = args[i];
            Class<?> argType = arg.getType();
            byte[] argData = arg.getArg();
            if (argType == null || ArrayUtils.isEmpty(argData)) {
                invokeArgs[i] = null;
                continue;
            }
            ByteBuffer argByteBuffer = ByteBuffer.wrap(argData);
            int type = argByteBuffer.getInt();
            byte[] argBytes = new byte[argData.length - 4];
            argByteBuffer.get(argBytes);
            switch (type) {
                case COMMON_OBJECT_ARG:
                    invokeArgs[i] = protocolFactory.decode(argType, argBytes);
                    break;
                case REMOTE_OBJECT_ARG:
                    invokeArgs[i] = remoteObjectFactory.getServiceObject(protocolFactory.decode(String.class, argBytes));
                    break;
                default:
                    throw new RuntimeException("can't read arg from arg : " + arg + " with type : " + type);
            }
        }
        return invokeArgs;
    }
}
