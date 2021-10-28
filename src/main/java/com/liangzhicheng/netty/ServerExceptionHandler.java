package com.liangzhicheng.netty;

import com.liangzhicheng.session.ServerSession;
import com.liangzhicheng.session.SessionMap;
import com.liangzhicheng.socket.Socket;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class ServerExceptionHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelReadComplete(ChannelHandlerContext context) throws Exception {
        context.flush();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) throws Exception {
        ServerSession session = ServerSession.getSession(context);
        //处理关闭，得到处理结果
        String content = Socket.instance().onClose(session);
        //发送处理结果给用户
        SessionMap.instance().send(content, session);
        SessionMap.instance().remove(session);
        SessionMap.instance().close(session);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) throws Exception {
        ServerSession session = ServerSession.getSession(context);
        SessionMap.instance().remove(session);
        session.processError(cause);
    }

}
