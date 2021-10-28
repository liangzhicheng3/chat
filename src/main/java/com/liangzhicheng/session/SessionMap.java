package com.liangzhicheng.session;

import com.liangzhicheng.entity.User;
import com.liangzhicheng.utils.JSONUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SessionMap {

    /**
     * 单例
     */
    private SessionMap(){}

    private static SessionMap singleInstance = new SessionMap();

    public static SessionMap instance() {
        return singleInstance;
    }

    /**
     * 会话集合
     */
    private ConcurrentHashMap<String, ServerSession> sessionMap = new ConcurrentHashMap<String, ServerSession>();

    /**
     * 组集合
     */
    private ConcurrentHashMap<String, ChannelGroup> groupMap = new ConcurrentHashMap<String, ChannelGroup>();

    /**
     * 发送消息
     * @param resultMap
     * @param session
     */
    public void send(Map<String, String> resultMap, ServerSession session) {
        ChannelGroup channelGroup = groupMap.get(session.getGroupName());
        if (Objects.isNull(channelGroup)) {
            return;
        }
        String value = JSONUtil.objectToString(resultMap);
        Channel channel = session.getChannel();
        //从组中移除通道（不返回给自己发的消息）
        channelGroup.remove(channel);
        ChannelGroupFuture future = channelGroup.writeAndFlush(new TextWebSocketFrame(value));
        future.addListener(f -> {
            log.debug("send finish：{}", value);
            channelGroup.add(channel);
        });
    }

    /**
     * 发送所有
     * @param value
     * @param session
     */
    public void send(String value, ServerSession session) {
        ChannelGroup channelGroup = groupMap.get(session.getGroupName());
        if (Objects.isNull(channelGroup)) {
            return;
        }
        ChannelGroupFuture future = channelGroup.writeAndFlush(new TextWebSocketFrame(value));
        future.addListener(f -> {
            log.debug("send all finish：{}", value);
        });
    }

    /**
     * 发送消息
     * @param context
     * @param message
     */
    public void sendMessage(ChannelHandlerContext context, String message) {
        ChannelFuture future = context.writeAndFlush(new TextWebSocketFrame(message));
        future.addListener(f -> {
            log.debug("send message finish:{}", message);
        });
    }

    /**
     *
     * @param resultMap
     * @param session
     */
    public void set(Map<String, String> resultMap, ServerSession session){
        sessionMap.put(session.getSessionId(), session);
        log.info("用户上线:id={},nickname={},在线总数:{}",
                session.getUser().getId(),
                session.getUser().getNickname(),
                sessionMap.size()
        );
        this.setChannelGroup(session);
        this.send(JSONUtil.objectToString(resultMap), session);
    }

    /**
     * 解除绑定关系
     * @param session
     * @return ServerSession
     */
    public ServerSession remove(ServerSession session) {
        this.removeSession(session);
        this.removeChannelGroup(session);
        return session;
    }

    /**
     * 关闭连接， 关闭前发送一条通知消息
     * @param session
     * @param content
     */
    public void close(ServerSession session, String content) {
        ChannelFuture future = session.getChannel().writeAndFlush(new TextWebSocketFrame(content));
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                log.debug("last package send finish:{},channel will closed", content);
                future.channel().close();
            }
        });
    }

    /**
     * 关闭连接
     * @param session
     */
    public void close(ServerSession session) {
        ChannelFuture future = session.getChannel().close();
        future.addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture future) {
                log.debug("close session finish,nickname:{}", session.getUser().getNickname());
            }
        });
    }

    public void shutdownGracefully() {
        Iterator<ChannelGroup> groupIterator = groupMap.values().iterator();
        while (groupIterator.hasNext()) {
            groupIterator.next().close();
        }
    }

    /**
     * 获取组中用户
     * @param groupName
     * @return Set<User>
     */
    public Set<User> getGroupUsers(String groupName) {
        ChannelGroup channelGroup = groupMap.get(groupName);
        if (Objects.isNull(channelGroup)) {
            return null;
        }
        Set<User> users = new HashSet<>();
        Iterator<Channel> it = channelGroup.iterator();
        while (it.hasNext()) {
            users.add(ServerSession.getSession(it.next()).getUser());
        }
        return users;

    }

    /**
     * 获取组
     * @return List<String>
     */
    public List<String> getGroupNames() {
        List<String> groupNames = new ArrayList<>();
        for (String key : groupMap.keySet()) {
            groupNames.add(key);
        }
        return groupNames;
    }

    /**
     * 写入session的通道到组
     * @param session
     */
    private void setChannelGroup(ServerSession session) {
        String groupName = session.getGroupName();
        if (StringUtils.isEmpty(groupName)) {
            return;
        }
        ChannelGroup channelGroup = groupMap.get(groupName);
        if (Objects.isNull(channelGroup)) {
            channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
            groupMap.put(groupName, channelGroup);
        }
        channelGroup.add(session.getChannel());
    }

    /**
     * 删除session
     * @param session
     */
    private void removeSession(ServerSession session) {
        String sessionId = session.getSessionId();
        if(!sessionMap.containsKey(sessionId)){
            return;
        }
        session = sessionMap.get(sessionId);
        sessionMap.remove(sessionId);
        log.info("用户下线:id={},nickname={},在线总数:{}",
                session.getUser().getId(),
                session.getUser().getNickname(),
                sessionMap.size()
        );
    }

    /**
     * 删除通道组
     * @param session
     */
    private void removeChannelGroup(ServerSession session) {
        Channel channel = session.getChannel();
        ChannelGroup channelGroup = groupMap.get(session.getGroupName());
        if (Objects.nonNull(channelGroup)) {
            channelGroup.remove(channel);
        }
    }

}
