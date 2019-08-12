package com.xuecheng.framework.domain.learning.response;

import com.xuecheng.framework.model.response.CommonCode;
import com.xuecheng.framework.model.response.Response;
import com.xuecheng.framework.model.response.ResponseResult;
import com.xuecheng.framework.model.response.ResultCode;

public class  ResponseLearnStatus implements Response {


    //操作是否成功
    boolean success = SUCCESS;

    //操作代码
    int code = SUCCESS_CODE;

    //提示信息
    String message;

    String status;

    public ResponseLearnStatus(ResultCode resultCode,String stataus){
        this.success = resultCode.success();
        this.code = resultCode.code();
        this.message = resultCode.message();
        this.status = status;
    }

    public static ResponseResult SUCCESS(){
        return new ResponseResult(CommonCode.SUCCESS);
    }
    public static ResponseResult FAIL(){
        return new ResponseResult(CommonCode.FAIL);
    }
}
