package com.liangzhicheng.netty;

import com.liangzhicheng.session.ServerSession;
import com.liangzhicheng.session.SessionMap;
import com.liangzhicheng.socket.Socket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

@Slf4j
public class NettyServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        //判断握手是否成功，升级为WebSocket协议
        if (event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            //握手成功，移除HttpRequestHandler，因此将不会接收到任何消息，并把握手成功后的Channel写入到ChannelGroup中
            ServerSession session = new ServerSession(context.channel());
            String message = Socket.instance().onOpen(session);
            SessionMap.instance().sendMessage(context, message);
            log.info("执行userEventTriggered，event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE");
        } else if (event instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) event;
            if (stateEvent.state() == IdleState.READER_IDLE) {
                ServerSession session = ServerSession.getSession(context);
                SessionMap.instance().remove(session);
                session.processError(null);
            }
            log.info("执行userEventTriggered，event instanceof IdleStateEvent");
        } else {
            super.userEventTriggered(context, event);
            log.info("执行userEventTriggered，调用父类userEventTriggered");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame message) throws Exception {
        //增加保留消息，并写入到ChannelGroup中所有已经连接的客户端
        ServerSession session = ServerSession.getSession(context);
        Map<String, String> result = Socket.instance().onMessage(message.text(), session);
        if (Objects.nonNull(result) && Objects.nonNull(result.get("type"))) {
            switch (result.get("type")) {
                case " ":
                    SessionMap.instance().set(result, session);
                    break;
                case "message":
                    SessionMap.instance().send(result, session);
                    break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        ServerSession session = ServerSession.getSession(context);
        SessionMap.instance().remove(session);
//        Socket.instance().onClose(session);
    }

}
