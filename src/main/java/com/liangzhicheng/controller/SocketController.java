package com.liangzhicheng.controller;

import com.liangzhicheng.entity.User;
import com.liangzhicheng.session.SessionMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping(value = "/chat")
public class SocketController {

    @PostMapping(value = "/enter")
    public Map<String, Object> enter(String roomNo, String nickname) {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", 0);
        Set<User> users = SessionMap.instance().getGroupUsers(roomNo);
        if (users != null) {
            users.forEach(user -> {
                if (user.getNickname().equals(nickname)) {
                    resultMap.put("code", 1);
                    resultMap.put("message", "昵称已存在，请重新输入");
                }
            });
            if ((Integer) resultMap.get("code") != 0) {
                return resultMap;
            }
            resultMap.put("code", 3);
            resultMap.put("message", "房间无密码");
            return resultMap;
        }
        return resultMap;
    }

    @PostMapping(value = "/listRoom")
    public Map<String, Object> listRoom() {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("roomList", SessionMap.instance().getGroupNames());
        return resultMap;
    }

    @RequestMapping(value = "/listOnlineUser")
    public Map<String, Object> listOnlineUser(String roomNo) {
        Map<String, Object> resultMap = new HashMap<>();
        Set<User> users = SessionMap.instance().getGroupUsers(roomNo);
        List<Map<String, String>> userList = new ArrayList<>();
        if(users != null){
            users.forEach(user -> {
                Map<String, String> map = new HashMap<>();
                map.put("id", user.getId());
                map.put("nickname", user.getNickname());
                userList.add(map);
            });
            resultMap.put("onlineNum", users.size());
            resultMap.put("onlineUser", userList);
        }else{
            resultMap.put("onlineNum", 0);
            resultMap.put("onlineUser", null);
        }
        return resultMap;
    }

}
