package com.lkj.springbootinit.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lkj.springbootinit.annotation.AuthCheck;
import com.lkj.springbootinit.mapper.UserInterfaceInfoMapper;
import com.lkj.springbootinit.model.vo.InterfaceInfoVo;
import com.lkj.springbootinit.service.InterfaceInfoService;
import com.lkj.apicommon.common.BaseResponse;
import com.lkj.apicommon.common.ErrorCode;
import com.lkj.apicommon.common.ResultUtils;
import com.lkj.apicommon.exception.BusinessException;
import com.lkj.apicommon.entity.InterfaceInfo;
import com.lkj.apicommon.entity.UserInterfaceInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
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
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVo>> listTopInvokeInterfaceInfo(){
        //查询调用次数最多的接口信息列表
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(3);
        //将接口信息按照接口ID分组，便于关联查询
        Map<Long,List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        //创建查询接口信息的条件包装器
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        //设置查询条件，使用接口信息ID再接口信息映射中的键集合进行条件匹配
        queryWrapper.in("id",interfaceInfoIdObjMap.keySet());
        //调用接口信息服务的list方法，传入条件包装器，获取符合条件的接口信息列表
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        //判断查询条件是否为空
        if (CollectionUtils.isEmpty(list)){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }
        //构建接口信息VO列表，使用流式处理将接口信息映射为接口信息VO对象，并加入列表中
        List<InterfaceInfoVo> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            //创建一个新的接口信息VO对象
            InterfaceInfoVo interfaceInfoVO = new InterfaceInfoVo();
            //将接口信息复制到接口信息VO对象中
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            //丛接口信息ID对应的映射中获取调用次数
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            //将调用次数设置到接口信息VO对象中
            interfaceInfoVO.setTotalNum(totalNum);
            //返回构建好的接口信息VO对象
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        //返回处理结果
        return ResultUtils.success(interfaceInfoVOList);

    }
}
