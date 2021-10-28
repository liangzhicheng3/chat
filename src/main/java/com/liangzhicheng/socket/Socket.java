package com.liangzhicheng.socket;

import com.google.gson.reflect.TypeToken;
import com.liangzhicheng.entity.User;
import com.liangzhicheng.session.ServerSession;
import com.liangzhicheng.utils.JSONUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class Socket {

    /**
     * 单例
     */
    private Socket(){}

    private static Socket singleInstance = new Socket();

    public static Socket instance() {
        return singleInstance;
    }

    /**
     * 建立连接调用方法
     * @param session
     * @return String
     */
    public String onOpen(ServerSession session) {
        Map<String, String> result = new HashMap<String, String>();
        result.put("type", "bing");
        result.put("sendUser", "系统消息");
        result.put("id", session.getSessionId());
        log.warn("[websocket服务] 连接开启...");
        return JSONUtil.objectToString(result);
    }

    /**
     * 接收到客户端消息后调用方法
     * @param message
     * @param session
     * @return Map<String, String>
     */
    public Map<String, String> onMessage(String message, ServerSession session) {
        Map<String, String> objectMap = JSONUtil.stringToObject(
                message,
                new TypeToken<HashMap<String, String>>(){}
        );
        Map<String, String> resultMap = new HashMap<String, String>();
        User user = null;
        switch (objectMap.get("type")) {
            case "init":
                String nickname = objectMap.get("nickname");
                user = new User(session.getSessionId(), nickname);
                session.setUser(user);
                String roomNo = objectMap.get("roomNo");
                session.setGroupName(roomNo);
                resultMap.put("type", "init");
                resultMap.put("message", nickname + "成功加入房间");
                resultMap.put("sendUser", "系统消息");
                log.warn("[websocket服务] 消息处理，{}成功加入到房间，房间号:{}",
                        nickname, roomNo);
                break;
            case "message":
                String content = objectMap.get("message");
                user = session.getUser();
                String sendUser = user.getNickname();
                resultMap.put("type", "message");
                resultMap.put("message", content);
                resultMap.put("sendUser", sendUser);
                log.warn("[websocket服务] 消息处理，发送用户:{}，发送消息:{}",
                        sendUser, content);
                break;
            case "ping":
                break;
        }
        return resultMap;
    }

    /**
     * 关闭连接调用方法
     * @param session
     * @return String
     */
    public String onClose(ServerSession session) {
        User user = session.getUser();
        if(Objects.nonNull(user)){
            String nickname = user.getNickname();
            Map<String, String> result = new HashMap<String, String>();
            result.put("type", "init");
            result.put("message", nickname + "离开房间");
            result.put("sendUser", "系统消息");
            log.warn("[websocket服务] 连接关闭，用户下线:id={},nickname={}", session.getSessionId(), nickname);
            return JSONUtil.objectToString(result);
        }
        return null;
    }

    /**
     * 连接发生错误时的调用方法
     * @param session
     * @param error
     */
    /**
     * 连接发生异常调用方法
     * @param session
     * @param error
     * @return
     */
    public String onError(ServerSession session, Throwable error) {
        if(Objects.nonNull(error)){
            log.error("连接发生异常：{}", error.getMessage());
        }
        User user = session.getUser();
        if(Objects.isNull(user)){
            return null;
        }
        Map<String, String> result = new HashMap<String, String>();
        result.put("type", "init");
        result.put("message", user.getNickname() + "离开房间");
        result.put("sendUser", "系统消息");
        log.warn("[websocket服务] 连接异常:{}", error.getMessage());
        return JSONUtil.objectToString(result);
    }

}
