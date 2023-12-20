package com.czq.apiorder.provider;


import com.czq.apiorder.service.TOrderService;
import com.lkj.apicommon.entity.Order;
import com.lkj.apicommon.service.InnerOrderService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
import java.util.List;


@DubboService
public class InnerOrderServiceImpl implements InnerOrderService {
    @Resource
    TOrderService orderService;
    @Override
    public List<Order> listTopBuyInterfaceInfo(int limit) {
        return orderService.listTopBuyInterfaceInfo(limit);
    }
}
