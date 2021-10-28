package com.liangzhicheng.session;

import com.liangzhicheng.entity.User;
import com.liangzhicheng.socket.Socket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Data
public class ServerSession {

    public static final AttributeKey<ServerSession> SESSION_KEY = AttributeKey.valueOf("session_key");

    /**
     * 通道
     */
    private Channel channel;

    /**
     * session唯一标识
     */
    private final String sessionId;

    /**
     * 用户
     */
    private User user;

    /**
     * 组
     */
    private String groupName;

    /**
     * session中存储的session变量属性值
     */
    private Map<String, Object> map = new HashMap<String, Object>();

    public ServerSession(Channel channel) {
        this.channel = channel;
        this.sessionId = this.generateId();
        channel.attr(ServerSession.SESSION_KEY).set(this);
        log.info("服务端session绑定会话：{}", channel.remoteAddress().toString());
    }

    public static ServerSession getSession(Channel channel) {
        return channel.attr(ServerSession.SESSION_KEY).get();
    }

    public static ServerSession getSession(ChannelHandlerContext context) {
        return context.channel().attr(ServerSession.SESSION_KEY).get();
    }

    public void processError(Throwable error) {
        //处理错误，得到处理结果
        String result = Socket.instance().onError(this, error);
        ////发送处理结果给用户
        SessionMap.instance().send(result, this);
        //关闭连接，关闭前发送一条通知消息
        SessionMap.instance().close(this, Socket.instance().onClose(this));
    }

    private String generateId() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
