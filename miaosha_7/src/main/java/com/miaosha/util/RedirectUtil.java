package com.miaosha.util;

/**
 * @author zhaolifeng
 * @version 1.0
 * @description: TODO
 * @date 2022/8/19 22:48
 */

import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;



/**
 * @author zhaolifeng
 * @version 1.0
 * @description: Redirect工具类
 * @date 2022/8/19 22:51
 */

@Slf4j
public class RedirectUtil {
    public static void redirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl) {

        try {
            //如果是Ajax请求
            if ("XMLHttpRequest".equals(request.getHeader("X-Requested-With"))) {
                log.debug("ajax redirect");
                sendRedirect(request, response, redirectUrl);
            }
            //如果是浏览器地址栏请求
            else {
                log.debug("normal redirect ");
                response.sendRedirect(redirectUrl);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    /**
     * ajax请求时的重定向处理
     * @param response-回答
     * @param redirectUrl-重定向相对路径url
     * @author zhaolifeng
     * @date 2022/8/19 22:52
     */
    private static void sendRedirect(HttpServletRequest request, HttpServletResponse response, String redirectUrl){
        //获取当前请求的路径
        String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
        //如果request.getHeader("X-Requested-With") 返回的是"XMLHttpRequest"说明就是ajax请求，需要特殊处理
        //设置ajax跨域访问
        response.setHeader("access-control-allow-origin","*");
        //告诉ajax我是重定向
        response.setHeader("REDIRECT", "REDIRECT");
        //告诉ajax我重定向的路径
        response.setHeader("CONTENTPATH", basePath + redirectUrl);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    }
}
