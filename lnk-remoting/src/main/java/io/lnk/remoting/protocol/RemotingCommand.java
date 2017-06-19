package io.lnk.remoting.protocol;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.lnk.api.ProtocolVersion;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年5月18日 下午3:42:41
 */
public class RemotingCommand implements Serializable {
    private static final long serialVersionUID = 4491438068883310566L;
    public static final int CODE_LENGTH = 4;
    public static final int VERSION_LENGTH = 4;
    public static final int COMMAND_LENGTH = 4;
    public static final int PROTOCOL_LENGTH = 4;
    public static final int OPAQUE_LENGTH = 8;
    public static final int BODY_LENGTH = 4;
    public static final int COMMAND_LENGTH_LENGTH = CODE_LENGTH + VERSION_LENGTH + COMMAND_LENGTH + PROTOCOL_LENGTH + OPAQUE_LENGTH + BODY_LENGTH;
    private static final int RPC = 0;
    private static final int ONEWAY = 1;
    private static final AtomicLong REQ_ID = new AtomicLong(1);
    private int code = 0;
    private int version = 0;
    private int command = 0;
    private int protocol = ProtocolVersion.DEFAULT_PROTOCOL;
    private long opaque = REQ_ID.getAndIncrement();
    private transient byte[] body;

    public static RemotingCommand replyCommand(RemotingCommand request, int code) {
        RemotingCommand command = new RemotingCommand();
        command.setCode(code);
        command.setVersion(request.getVersion());
        command.setReply();
        if (request.isOneway()) {
            command.setOneway();
        }
        command.setProtocol(request.getProtocol());
        command.setOpaque(request.getOpaque());
        return command;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public int getProtocol() {
        return protocol;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public long getOpaque() {
        return opaque;
    }

    public void setOpaque(long opaque) {
        this.opaque = opaque;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    @JsonIgnore
    public boolean isOneway() {
        int bits = 1 << ONEWAY;
        return (this.command & bits) == bits;
    }

    @JsonIgnore
    public void setOneway() {
        int bits = 1 << ONEWAY;
        this.command |= bits;
    }

    @JsonIgnore
    public boolean isReply() {
        int bits = 1 << RPC;
        return (this.command & bits) == bits;
    }

    @JsonIgnore
    public void setReply() {
        int bits = 1 << RPC;
        this.command |= bits;
    }

    @Override
    public String toString() {
        int bodyLength = 0;
        if (body != null) {
            bodyLength = body.length;
        }
        return "RemotingCommand[code=" + code + ", version=" + version + ", command=" + command + ", protocol=" + protocol + ", opaque=" + opaque + ", body=" + bodyLength + "]";
    }
}
