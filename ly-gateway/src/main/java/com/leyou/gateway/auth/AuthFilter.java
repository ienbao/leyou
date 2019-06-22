package com.leyou.gateway.auth;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.common.utils.CookieUtils;
import com.leyou.gateway.config.FilterProperties;
import com.leyou.gateway.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.sun.scenario.effect.FilterContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;

@Component
@EnableConfigurationProperties({JwtProperties.class,FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterProp;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE;//过滤器类型，前置过滤器
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1;//过滤器的顺序
    }

    @Override
    public boolean shouldFilter() {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取request
        HttpServletRequest request = ctx.getRequest();
        //获取请求路径
        String path = request.getRequestURI();
        //判断是否在白名单中，如果存在就放行，不存在过滤

        return !isAllowPath(path);//是否过滤
    }

    private boolean isAllowPath(String path) {
        //遍历白名单
        for (String filter : filterProp.getAllowPaths()) {
            //判断是否允许
            if (path.startsWith(filter)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
        //获取上下文
        RequestContext ctx = RequestContext.getCurrentContext();
        //获取Request
        HttpServletRequest request = ctx.getRequest();
        //获取cookie 中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
            //解析token
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
            //TODO 校验权限

        }catch (Exception e){
            //解析token失败，未登录，拦截
            ctx.setSendZuulResponse(true);
            ctx.setResponseStatusCode(403);
        }
        return null;
    }
}
