package com.miaosha.interceptor;

import com.alibaba.fastjson.JSON;
import com.miaosha.access.AccessLimit;
import com.miaosha.access.UserContext;
import com.miaosha.domain.MiaoshaUser;
import com.miaosha.redis.AccessKey;
import com.miaosha.redis.RedisService;
import com.miaosha.result.CodeMsg;
import com.miaosha.result.Result;
import com.miaosha.service.MiaoshaUserService;
import com.miaosha.util.RedirectUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: 访问控制过滤器
 * @date 2022/8/19 18:59
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    MiaoshaUserService userService;
    RedisService redisService;

    @Autowired
    public void setRedisService(RedisService redisService) {
        this.redisService = redisService;
    }

    @Autowired
    public void setUserService(MiaoshaUserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod){
            MiaoshaUser user = getUser(request, response);
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod) handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit==null){
                return true;
            }
            int seconds = accessLimit.seconds();
            int maxCount = accessLimit.maxCount();
            boolean needLogin = accessLimit.needLogin();
            String key = request.getRequestURI();
            if (needLogin){
                if (user==null){
                    RedirectUtil.redirect(request,response,"/");
                    render(response, CodeMsg.USER_NOT_LOGIN);
                    return false;
                }
                key += "_" + user.getId();
            }
            if (seconds==-1){
                AccessKey ak = AccessKey.withExpire(seconds);
                Integer count = redisService.get(ak,key,Integer.class);
                if (count==null){
                    redisService.set(ak,key,1);
                }else if (count<maxCount){
                    redisService.incr(ak,key);
                }else {
                    render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                    return false;
                }
            }
        }
        return super.preHandle(request, response, handler);
    }

    private MiaoshaUser getUser(HttpServletRequest request,HttpServletResponse response){
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_NAME_TOKEN);


        if (StringUtils.isEmpty(cookieToken)&&StringUtils.isEmpty(paramToken)){
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return  userService.getByToken(response,token);
    }

    @SuppressWarnings("all")
    private String getCookieValue(HttpServletRequest request, String cookieNameToken) {
        Cookie[] cookies = request.getCookies();
        if (cookies==null||cookies.length<=0){
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieNameToken)){
                return cookie.getValue();
            }
        }
        return null;
    }

    private void render(HttpServletResponse response, CodeMsg cm) throws Exception{
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(cm));
        out.write(str.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
    }

}
