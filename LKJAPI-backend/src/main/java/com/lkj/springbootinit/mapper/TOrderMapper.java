package com.lkj.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkj.apicommon.entity.Order;



import java.util.List;

public interface TOrderMapper extends BaseMapper<Order> {
    /**
     * 获取前 limit 购买数量的接口
     * @param limit
     * @return
     */
    List<Order> listTopBuyInterfaceInfo(int limit);
}




