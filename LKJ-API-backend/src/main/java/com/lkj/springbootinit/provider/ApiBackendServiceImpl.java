package com.lkj.springbootinit.provider;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.lkj.springbootinit.mapper.InterfaceInfoMapper;
import com.lkj.springbootinit.mapper.UserInterfaceInfoMapper;
import com.lkj.springbootinit.mapper.UserMapper;
import com.lkj.springbootinit.model.dto.userinterface.UpdateUserInterfaceInfoDTO;
import com.lkj.springbootinit.service.InterfaceChargingService;
import com.lkj.springbootinit.service.UserInterfaceInfoService;
import com.lkj.apicommon.common.ErrorCode;
import com.lkj.apicommon.exception.BusinessException;
import com.lkj.apicommon.entity.InterfaceCharging;
import com.lkj.apicommon.entity.InterfaceInfo;
import com.lkj.apicommon.entity.User;
import com.lkj.apicommon.service.ApiBackendService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * 作为服务提供方，提供远程调用接口
 */
@DubboService
public class ApiBackendServiceImpl implements ApiBackendService {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceChargingService interfaceChargingService;


    @Override
    public User getInvokeUser(String accessKey) {

        if (StringUtils.isBlank(accessKey)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("accessKey",accessKey);
        return userMapper.selectOne(queryWrapper);
    }

    @Override
    public InterfaceInfo getInterFaceInfo(String url, String method) {
        if (StringUtils.isBlank(url) || StringUtils.isBlank(method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url",url);
        queryWrapper.eq("method",method);
        return interfaceInfoMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.invokeCount(userId,interfaceInfoId);
    }

    @Override
    public int getLeftInvokeCount(long userId, long interfaceInfoId) {
        return userInterfaceInfoService.getLeftInvokeCount(userId,interfaceInfoId);
    }

    @Override
    public InterfaceInfo getInterfaceById(long interfaceId) {
        return interfaceInfoMapper.selectById(interfaceId);
    }

    @Override
    public int getInterfaceStockById(long interfaceId) {
        QueryWrapper<InterfaceCharging> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceId",interfaceId);
        InterfaceCharging interfaceCharging = interfaceChargingService.getOne(queryWrapper);
        if (interfaceCharging == null){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"接口不存在");
        }
        return Integer.parseInt(interfaceCharging.getAvailablePieces());
    }

    @Override
    public boolean updateInterfaceStock(long interfaceId,Integer num) {
        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("availablePieces = availablePieces - "+num)
                .eq("interfaceId",interfaceId).gt("availablePieces",num);

        return interfaceChargingService.update(updateWrapper);
    }

    @Override
    public boolean recoverInterfaceStock(long interfaceId, Integer num) {
        UpdateWrapper<InterfaceCharging> updateWrapper = new UpdateWrapper<>();
        updateWrapper.setSql("availablePieces = availablePieces + "+num)
                .eq("interfaceId",interfaceId);
        return interfaceChargingService.update(updateWrapper);
    }

    @Override
    public boolean updateUserInterfaceInvokeCount(long userId, long interfaceId, int num) {
        UpdateUserInterfaceInfoDTO userInterfaceInfoDTO = new UpdateUserInterfaceInfoDTO();
        userInterfaceInfoDTO.setUserId(userId);
        userInterfaceInfoDTO.setInterfaceId(interfaceId);
        userInterfaceInfoDTO.setLockNum((long)num);
        return userInterfaceInfoService.updateUserInterfaceInfo(userInterfaceInfoDTO);
    }


}
