package com.xuecheng.govern.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import io.netty.util.internal.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

//@Component
public class LoginFilterTest extends ZuulFilter {
    //设置过滤器的类型
    @Override
    public String filterType() {
        return "pre";
    }

    //过滤器序号 ,越小越优先执行
    @Override
    public int filterOrder() {
        return 0;
    }

    //false 代表当前过滤器失效
    @Override
    public boolean shouldFilter() {
        return true;
    }

    //过滤器内容编写
    @Override
    public Object run() throws ZuulException {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        HttpServletRequest request = requestContext.getRequest();
        //得到Authorization头
        String authorization = request.getHeader("authorization");
        if(StringUtils.isEmpty(authorization)){
            //拒绝访问
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(200);
            ResponseResult responseResult = new ResponseResult(CommonCode.UNAUTHENTICATED);
            String jsonString = JSON.toJSONString(responseResult);
            requestContext.setResponseBody(jsonString);
            response.setContentType("application/json;charset=utf-8");
            return null;
        }

        return null;
    }
}
