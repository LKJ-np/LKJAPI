package com.czq.apiorder.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.czq.apiorder.model.dto.OrderAddRequest;
import com.czq.apiorder.model.dto.OrderQueryRequest;
import com.czq.apiorder.service.TOrderService;
import com.lkj.apicommon.common.BaseResponse;
import com.lkj.apicommon.common.ResultUtils;
import com.lkj.apicommon.vo.OrderVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 订单接口
 */
@RestController
@RequestMapping("/")
public class OrderController {

    @Resource
    private TOrderService orderService;

    /**
     * 添加订单，购买接口
     * @param orderAddRequest
     * @param request
     * @return
     */
    @PostMapping("/addOrder")
    public BaseResponse<OrderVO> interfaceTOrder(@RequestBody OrderAddRequest orderAddRequest, HttpServletRequest request){
        OrderVO order = orderService.addOrder(orderAddRequest,request);
        return ResultUtils.success(order);
    }

    /**
     * 获取订单列表
     * @param orderQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/list")
    public BaseResponse<Page<OrderVO>> listPageOrder(OrderQueryRequest orderQueryRequest, HttpServletRequest request){
        Page<OrderVO> orderPage = orderService.listPageOrder(orderQueryRequest, request);
        return ResultUtils.success(orderPage);
    }


}
