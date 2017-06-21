package io.lnk.protocol.object;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.ArrayUtils;

import io.lnk.api.ProtocolObject;
import io.lnk.api.RemoteObject;
import io.lnk.api.RemoteObjectFactory;
import io.lnk.api.protocol.ProtocolFactory;
import io.lnk.api.protocol.object.ObjectProtocolFactory;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月30日 下午12:15:03
 */
public class LnkObjectProtocolFactory implements ObjectProtocolFactory {
    private static final int COMMON_OBJECT_ARG = 1;
    private static final int REMOTE_OBJECT_ARG = 2;
    private RemoteObjectFactory remoteObjectFactory;

    @Override
    public ProtocolObject[] encode(final Object[] objs, final ProtocolFactory protocolFactory) throws Throwable {
        ProtocolObject[] commandObjs = null;
        if (ArrayUtils.isEmpty(objs)) {
            return commandObjs;
        }
        int objsNum = objs.length;
        commandObjs = new ProtocolObject[objsNum];
        for (int i = 0; i < objsNum; i++) {
            Object obj = objs[i];
            ProtocolObject protocolObject = new ProtocolObject();
            if (obj == null) {
                commandObjs[i] = protocolObject;
                continue;
            }
            protocolObject.setType(obj.getClass());
            int type = COMMON_OBJECT_ARG;
            byte[] objBytes = null;
            if (obj instanceof RemoteObject) {
                protocolObject.setType(RemoteObject.class);
                objBytes = protocolFactory.encode(((RemoteObject) obj).serializeStub());
                type = REMOTE_OBJECT_ARG;
            } else {
                objBytes = protocolFactory.encode(obj);
            }
            ByteBuffer objByteBuffer = ByteBuffer.allocate(objBytes.length + 4);
            objByteBuffer.putInt(type);
            objByteBuffer.put(objBytes);
            objByteBuffer.flip();
            protocolObject.setData(objByteBuffer.array());
            commandObjs[i] = protocolObject;
        }
        return commandObjs;
    }

    @Override
    public Object[] decode(final ProtocolObject[] objs, final ProtocolFactory protocolFactory) throws Throwable {
        Object[] invokeObjs = null;
        if (ArrayUtils.isEmpty(objs)) {
            return invokeObjs;
        }
        int argsNum = objs.length;
        invokeObjs = new Object[argsNum];
        for (int i = 0; i < argsNum; i++) {
            ProtocolObject obj = objs[i];
            Class<?> objType = obj.getType();
            byte[] objData = obj.getData();
            if (objType == null || ArrayUtils.isEmpty(objData)) {
                invokeObjs[i] = null;
                continue;
            }
            ByteBuffer objByteBuffer = ByteBuffer.wrap(objData);
            int type = objByteBuffer.getInt();
            byte[] objBytes = new byte[objData.length - 4];
            objByteBuffer.get(objBytes);
            switch (type) {
                case COMMON_OBJECT_ARG:
                    invokeObjs[i] = protocolFactory.decode(objType, objBytes);
                    break;
                case REMOTE_OBJECT_ARG:
                    invokeObjs[i] = remoteObjectFactory.getServiceObject(protocolFactory.decode(String.class, objBytes));
                    break;
                default:
                    throw new RuntimeException("can't read obj from : " + obj + " with type : " + type);
            }
        }
        return invokeObjs;
    }
    
    public void setRemoteObjectFactory(RemoteObjectFactory remoteObjectFactory) {
        this.remoteObjectFactory = remoteObjectFactory;
    }
}
