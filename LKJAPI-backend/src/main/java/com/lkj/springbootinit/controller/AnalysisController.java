package com.lkj.springbootinit.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lkj.apicommon.common.BaseResponse;
import com.lkj.apicommon.common.ErrorCode;
import com.lkj.apicommon.common.ResultUtils;
import com.lkj.apicommon.entity.InterfaceInfo;
import com.lkj.apicommon.entity.Order;
import com.lkj.apicommon.entity.UserInterfaceInfo;
import com.lkj.apicommon.exception.BusinessException;
import com.lkj.apicommon.service.InnerOrderService;
import com.lkj.apicommon.vo.OrderVO;
import com.lkj.springbootinit.annotation.AuthCheck;
import com.lkj.springbootinit.mapper.UserInterfaceInfoMapper;
import com.lkj.springbootinit.model.excel.InterfaceInfoInvokeExcel;
import com.lkj.springbootinit.model.excel.InterfaceInfoOrderExcel;
import com.lkj.springbootinit.model.vo.InterfaceInfoVo;
import com.lkj.springbootinit.model.vo.UserInterfaceInfoAnalysisVo;
import com.lkj.springbootinit.service.InterfaceInfoService;
import com.lkj.springbootinit.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author：LKJ
 * @Package：com.lkj.springbootinit.controller
 * @Project：LKJAPI-backend
 * @name：AnalysisController
 * @Date：2023/12/14 21:09
 * @Filename：AnalysisController
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @DubboReference
    private InnerOrderService innerOrderService;

    /**
     * 获取调用次数前几的接口
     * @return
     */
    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVo>> listTopInvokeInterfaceInfo(){
        //查询调用次数最多的接口信息列表
        List<UserInterfaceInfoAnalysisVo> interfaceInfoVoList = userInterfaceInfoMapper.listTopInterfaceInfo(3);
        if (CollectionUtils.isEmpty(interfaceInfoVoList)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //将接口信息按照接口ID分组，便于关联查询
        List<Long> interfaceInfoIds = interfaceInfoVoList.stream()
                .map(UserInterfaceInfo::getInterfaceInfoId).collect(Collectors.toList());
        //创建查询接口信息的条件包装器
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件，使用接口信息ID再接口信息映射中的键集合进行条件匹配
        queryWrapper.in("id",interfaceInfoIds);
        //调用接口信息服务的list方法，传入条件包装器，获取符合条件的接口信息列表
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        //新建一个接口列表（接口总调用次数，计费规则，计费id，剩余可调用次数）
        List<InterfaceInfoVo> infoVoList = new ArrayList<>(interfaceInfoList.size());
        for (int i = 0; i < interfaceInfoList.size(); i++) {
            InterfaceInfo interfaceInfo = interfaceInfoList.get(i);
            UserInterfaceInfoAnalysisVo userInterfaceInfoVo = interfaceInfoVoList.get(i);

            InterfaceInfoVo interfaceInfoVo = new InterfaceInfoVo();
            BeanUtils.copyProperties(interfaceInfo,interfaceInfoVo);
            interfaceInfoVo.setTotalNum(userInterfaceInfoVo.getSumNum());

            infoVoList.add(interfaceInfoVo);
        }
        //返回处理结果
        return ResultUtils.success(infoVoList);
    }

    /**
     * 下载统计出来的接口调用次数，生成表格
     * @param response
     * @throws IOException
     */
    @GetMapping("/top/interface/invoke/excel")
    @AuthCheck(mustRole = "admin")
    public void topInvokeInterfaceInfoExcel(HttpServletResponse response) throws IOException {

        List<InterfaceInfoVo> interfaceInfoVOList = userInterfaceInfoService.interfaceInvokeTopAnalysis(100);
        List<InterfaceInfoInvokeExcel> collect = interfaceInfoVOList.stream().map(interfaceInfoVO -> {
            InterfaceInfoInvokeExcel interfaceInfoExcel = new InterfaceInfoInvokeExcel();
            BeanUtils.copyProperties(interfaceInfoVO, interfaceInfoExcel);
            return interfaceInfoExcel;
        }).sorted((a,b)-> b.getTotalNum() - a.getTotalNum()).collect(Collectors.toList());

        String fileName = "interface_invoke.xlsx";
        genExcel(response,fileName, InterfaceInfoInvokeExcel.class,collect);
    }

    /**
     * 获取购买最多的接口
     * @return
     */
    @GetMapping("/top/interface/buy")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<OrderVO>> listTopBuyInterfaceInfo() {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        return ResultUtils.success(orderVOList);
    }

    /**
     * 获得购买最多的方法
     * @return
     */
    private List<OrderVO> interfaceBuyTopAnalysis() {
        List<Order> orderList = innerOrderService.listTopBuyInterfaceInfo(5);
        List<OrderVO> orderVOList = orderList.stream().map(order -> {
            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
            OrderVO orderVO = new OrderVO();
            orderVO.setInterfaceId(interfaceId);
            orderVO.setTotal(order.getCount().longValue());
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            return orderVO;
        }).collect(Collectors.toList());
        return orderVOList;
    }

    /**
     *
     * @param response
     * @throws IOException
     */
    @GetMapping("/top/interface/buy/excel")
    @AuthCheck(mustRole = "admin")
    public void topBuyInterfaceInfoExcel(HttpServletResponse response) throws IOException {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        List<InterfaceInfoOrderExcel> collect = orderVOList.stream().map(orderVO -> {
            InterfaceInfoOrderExcel interfaceInfoOrderExcel = new InterfaceInfoOrderExcel();
            BeanUtils.copyProperties(orderVO, interfaceInfoOrderExcel);
            return interfaceInfoOrderExcel;
        }).sorted((a, b) -> (int) (b.getTotal() - a.getTotal())).collect(Collectors.toList());
        String fileName = "interface_buy.xlsx";
        //生成excel文档
        genExcel(response,fileName,InterfaceInfoOrderExcel.class,collect);
    }

    /**
     * 生成Excel文档
     * @param response
     * @param fileName
     * @param entity
     * @param collect
     * @throws IOException
     */
    private void genExcel(HttpServletResponse response,String fileName,Class entity, List collect) throws IOException {

        String sheetName = "analysis";
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        // 创建ExcelWriter对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), entity).build();
        // 创建工作表
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
        // 写入数据到工作表
        excelWriter.write(collect, writeSheet);
        // 关闭ExcelWriter对象
        excelWriter.finish();
    }
}
