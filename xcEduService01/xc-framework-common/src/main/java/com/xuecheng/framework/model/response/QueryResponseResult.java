package com.xuecheng.framework.model.response;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class QueryResponseResult<T> extends ResponseResult {

    QueryResult<T> queryResult; //查询的数据列表和集合

    public QueryResponseResult(ResultCode resultCode,QueryResult queryResult){
        super(resultCode);
       this.queryResult = queryResult;
    }

}
