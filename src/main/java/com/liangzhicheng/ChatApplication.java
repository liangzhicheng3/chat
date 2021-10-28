package com.liangzhicheng;

import com.liangzhicheng.netty.NettyServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication
public class ChatApplication {

    public static void main(String[] args) throws UnknownHostException {
        ConfigurableApplicationContext application = SpringApplication.run(ChatApplication.class, args);
        String host = InetAddress.getLocalHost().getHostAddress();
        String port = application.getEnvironment().getProperty("server.port");
        log.info("聊天窗口启动成功！点击进入:\t http://{}:{}", host, port);
        NettyServer.instance().start();
    }

}
