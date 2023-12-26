package com.czq.apiorder.model.dto;


import com.lkj.apicommon.common.PageRequest;
import lombok.Data;
import java.io.Serializable;


@Data
public class OrderQueryRequest extends PageRequest implements Serializable {
    /**
     * 当前账单状态对应status 0-未支付，1-已支付，2-已过期/失效
     */
    private String type;
}
