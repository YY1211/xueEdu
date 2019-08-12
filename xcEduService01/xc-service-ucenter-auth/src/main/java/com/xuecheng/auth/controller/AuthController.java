package com.xuecheng.auth.controller;

import com.alibaba.druid.util.StringUtils;
import com.xuecheng.api.auth.AuthControllerApi;
import com.xuecheng.auth.service.AuthService;
import com.xuecheng.framework.domain.ucenter.ext.AuthToken;
import com.xuecheng.framework.domain.ucenter.request.LoginRequest;
import com.xuecheng.framework.domain.ucenter.response.AuthCode;
import com.xuecheng.framework.domain.ucenter.response.JwtResult;
import com.xuecheng.framework.domain.ucenter.response.LoginResult;
import com.xuecheng.framework.exception.ExceptionCast;
import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.utils.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/")
public class AuthController implements AuthControllerApi {

    @Value("${auth.clientId}")
    String clientId;
    @Value("${auth.clientSecret}")
    String clientSecret;
    @Value("${auth.cookieDomain}")
    String cookieDomain;
    @Value("${auth.cookieMaxAge}")
    int cookieMaxAge;
    @Autowired
    AuthService authService;

    @Override
    @PostMapping("/userlogin")
    public LoginResult login(LoginRequest loginRequest) {
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getUsername())){
            ExceptionCast.cast(AuthCode.AUTH_USERNAME_NONE);
        }
        if(loginRequest == null || StringUtils.isEmpty(loginRequest.getPassword())){
            ExceptionCast.cast(AuthCode.AUTH_PASSWORD_NONE);
        }
        //账号
        String username = loginRequest.getUsername();
        //密码
        String password = loginRequest.getPassword();
        //申请令牌
        AuthToken authToken =  authService.login(username,password,clientId,clientSecret);
        //用户身份令牌
        String access_token = authToken.getAccess_token();
        this.saveCookie(access_token);
        return new LoginResult(CommonCode.SUCCESS,access_token);
    }

    //将令牌存储到cookie
    private void saveCookie(String token) {
        HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
        CookieUtil.addCookie(response,cookieDomain,"/","uid",token,cookieMaxAge,false);
    }

    /**
     * 退出登陆
     * @return
     */
    @Override
    @PostMapping("/userlogout")
    public ResponseResult logout() {
        String uid = getTokenFormCookie();
        //删除redis中的token
        boolean result = authService.delToken(uid);

        //清除cookie
        this.delCookie(uid);
        return new ResponseResult(CommonCode.SUCCESS);
    }

    /**
     * 获取cookie
     * @return
     */
    @Override
    @GetMapping("/userjwt")
    public ResponseResult userjwt() {
        //取出cookie中的用户身份令牌
        String uid = getTokenFormCookie();

        if(uid == null){
            return new JwtResult(CommonCode.FAIL,null);
        }

        //拿身份令牌从redis中查询jwt令牌
        AuthToken userToken = authService.getUserToken(uid);
        if(userToken!=null){
            //将jwt令牌返回给用户
            String jwt_token = userToken.getJwt_token();
            return new JwtResult(CommonCode.SUCCESS,jwt_token);
        }
        return null;
    }
    
        //从cookie中读取访问令牌
       private String getTokenFormCookie(){
           HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
           Map<String, String> cookieMap = CookieUtil.readCookie(request, "uid");
           String access_token = cookieMap.get("uid");
           return access_token;
       }

        //删除cookie
        private void delCookie(String token) {
            HttpServletResponse response = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
            CookieUtil.addCookie(response,cookieDomain,"/","uid",token,0,false);
        }

}
