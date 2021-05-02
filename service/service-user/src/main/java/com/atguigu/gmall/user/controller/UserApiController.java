package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/passport")
public class UserApiController {

    @Autowired
    UserService userService;


    @RequestMapping("findUserAddressListByUserId/{userId}")
    List<UserAddress> findUserAddressListByUserId(@PathVariable("userId")  String userId){

        return userService.findUserAddressListByUserId(userId);
    }

    @RequestMapping("/verify/{token}")
    Map<String,Object> verify(@PathVariable("token") String token){

        Map<String,Object> map = userService.verify(token);

        return map;
    }

    @RequestMapping("login")
    Result login(@RequestBody UserInfo userInfo){

        // 验证登录信息
        Map<String,Object> map = new HashMap<>();
        map = userService.login(userInfo);
        if(null==map){
            return Result.fail();
        }else {
            return Result.ok(map);
        }
    }

    @RequestMapping("ping")
    String ping(HttpServletRequest request){

        String userId = request.getHeader("userId");
        String userTempId = request.getHeader("userTempId");
        return "pong";
    }
}
