package com.liangzhicheng.netty;

import com.liangzhicheng.session.SessionMap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class NettyServer {

    /**
     * 单例
     */
    private NettyServer(){}

    private static NettyServer singleInstance = new NettyServer();

    public static NettyServer instance() {
        return singleInstance;
    }

    /**
     * NIO事件循环组（线程组）
     */
    private final EventLoopGroup group = new NioEventLoopGroup();

    /**
     * 通道
     */
    private Channel channel;

    public void start(){
        final NettyServer nettyServer = new NettyServer();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChatServerInitializer());
        InetSocketAddress address = new InetSocketAddress(8900);
        ChannelFuture channelFuture = bootstrap.bind(address);
        channelFuture.syncUninterruptibly();
        channel = channelFuture.channel();
        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                nettyServer.stop();
            }
        });
        channelFuture.channel().closeFuture().syncUninterruptibly();
    }

    public void stop(){
        if(Objects.nonNull(channel)){
            channel.close();
        }
        SessionMap.instance().shutdownGracefully();
        group.shutdownGracefully();
    }

    class ChatServerInitializer extends ChannelInitializer<Channel>{

        private static final int MAX_CONTENT_LENGTH = 64 * 1024; //最大内容长度
        private static final int MAX_FRAME_SIZE = 10 * 1024; //最大结构大小
        private static final int READ_IDLE_TIME_OUT = 60; //读超时
        private static final int WRITE_IDLE_TIME_OUT = 0; //写超时
        private static final int ALL_IDLE_TIME_OUT = 0; //读写超时

        @Override
        protected void initChannel(Channel channel) throws Exception {
            ChannelPipeline pipeline = channel.pipeline();
            /**
             * Netty中的http解码器和编码器
             */
            pipeline.addLast(new HttpServerCodec());
            /**
             * ChunkedWriteHandler用于大数据的分区传输（文件过大消耗内存）
             */
            pipeline.addLast(new ChunkedWriteHandler());
            /**
             * HttpObjectAggregator用于解析Http请求消息体（把多个消息转换为一个单一的完全FullHttpRequest或FullHttpResponse）
             * Http解码器会在每个Http消息中生成多个消息对象HttpRequest/HttpResponse，HttpContent，LastHttpContent
             */
            pipeline.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
            /**
             * WebSocketServerCompressionHandler用于数据压缩
             */
            pipeline.addLast(new WebSocketServerCompressionHandler());
            /**
             * WebSocketServerProtocolHandler用于配置websocket的监听地址/协议包长度限制
             */
            pipeline.addLast(new WebSocketServerProtocolHandler("/chat", null, true, MAX_FRAME_SIZE));
            /**
             * 当连接在60秒内没有接收到消息时，就会触发一个IdleStateEvent事件，该事件会被HeartbeatHandler的userEventTriggered方法处理
             */
            pipeline.addLast(new IdleStateHandler(READ_IDLE_TIME_OUT, WRITE_IDLE_TIME_OUT, ALL_IDLE_TIME_OUT, TimeUnit.SECONDS));
            /**
             * 自定义逻辑处理器
             */
            pipeline.addLast(new NettyServerHandler());
        }

    }

}
