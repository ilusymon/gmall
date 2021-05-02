package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.result.Result;
import com.atguigu.gmall.result.ResultCodeEnum;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class AuthFilter implements GlobalFilter {

    @Autowired
    UserFeignClient userFeignClient;

    @Value("${authUrls.url}")
    String authUrls;

    AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();

        ServerHttpResponse response = exchange.getResponse();

        String uri = request.getURI().toString();


        if (uri.contains("passport") || uri.contains("jpg") || uri.contains("png") || uri.contains("js") || uri.contains("css") || uri.contains("ico") || uri.contains("woff")) {
            return chain.filter(exchange);
        }
        // 请求鉴权，sso系统

        // 黑名单
        if (antPathMatcher.match("**/inner/**", uri)) {
            Mono<Void> voidMono = response.setComplete();
            ResultCodeEnum permission = ResultCodeEnum.PERMISSION;
            return out(response, permission);
        }

        // 如果请求的url在白名单中，那么需要认证token才能访问，否则踢回认证中心
        String token = getCookieValue(request,"token");
        String userId = "";
        if(!StringUtils.isEmpty(token)){
            Map<String, Object> verifyMap = userFeignClient.verify(token);
            userId = (String) verifyMap.get("userId");
        }

        if (antPathMatcher.match("**/auth/**", uri)) {
            if(StringUtils.isEmpty(userId)){
                // 判断当前请求是否登录，如果登录可以访问
                Mono<Void> voidMono = response.setComplete();
                ResultCodeEnum permission = ResultCodeEnum.PERMISSION;
                return out(response, permission);
            }
        }


        // 白名单
        String[] authUrlsArray = authUrls.split(",");
        for (String authUrl : authUrlsArray) {
            if (uri.contains(authUrl)) {
                if (null == userId || userId.equals("")) {
                    response.setStatusCode(HttpStatus.SEE_OTHER);
                    response.getHeaders().set(HttpHeaders.LOCATION, "http://passport.gmall.com/login.html?originUrl=" + uri);// 网关将原始请求地址带给认证中心
                    Mono<Void> voidMono = response.setComplete();
                    return voidMono;
                }
            }
        }

        if(null!=userId&&!userId.equals("")){
            // 将userId传递到后台
            request.mutate().header("userId",userId);
            exchange.mutate().request(request);
        }

        String userTempId = getCookieValue(request,"userTempId");

        if(!StringUtils.isEmpty(userTempId)){
            // 将userTempId传递到后台
            request.mutate().header("userTempId",userTempId);
            exchange.mutate().request(request);
        }

        return chain.filter(exchange);
    }

    private String getCookieValue(ServerHttpRequest request,String cookieName) {

        String result = "";

        MultiValueMap<String, HttpCookie> cookies = request.getCookies();

        /***
         * 普通的同步请求
         */
        if (null != cookies) {
            List<HttpCookie> tokens = cookies.get(cookieName);
            if(null!=tokens&&tokens.size()>0){
                for (HttpCookie tokenCookie : tokens) {
                    String name = tokenCookie.getName();
                    if (name.equals(cookieName)) {
                        result = tokenCookie.getValue();
                    }
                }
            }
        }

        /***
         * 购物车列表异步请求
         */
        if (StringUtils.isEmpty(result)) {
            List<String> strings = request.getHeaders().get(cookieName);
            if(null!=strings&&strings.size()>0){
                result = strings.get(0);
            }
        }

        return result;
    }

    // 接口鉴权失败返回数据
    private Mono<Void> out(ServerHttpResponse response, ResultCodeEnum resultCodeEnum) {
        // 返回用户没有权限登录
        Result<Object> result = Result.build(null, resultCodeEnum);
        byte[] bits = JSONObject.toJSONString(result).getBytes(StandardCharsets.UTF_8);
        DataBuffer wrap = response.bufferFactory().wrap(bits);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        Mono<DataBuffer> just = Mono.just(wrap);
        Mono<Void> voidMono = response.writeWith(just);

        // 输入到页面
        return voidMono;
    }

}
