package com.xuecheng.framework.domain.system.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class QueryDicRequest {


    @ApiModelProperty("字典名称")
    private String dName;

/*    @ApiModelProperty("字典类型")
    private String d_Type;*/
}
