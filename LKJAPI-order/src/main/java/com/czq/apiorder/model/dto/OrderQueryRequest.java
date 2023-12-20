package com.czq.apiorder.model.dto;


import com.lkj.apicommon.common.PageRequest;
import lombok.Data;

import java.io.Serializable;


@Data
public class OrderQueryRequest extends PageRequest implements Serializable {
    private String type;
}
