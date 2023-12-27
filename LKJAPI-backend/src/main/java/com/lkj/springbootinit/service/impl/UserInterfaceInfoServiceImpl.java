package com.lkj.springbootinit.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lkj.springbootinit.constant.UserConstant;
import com.lkj.springbootinit.mapper.UserInterfaceInfoMapper;
import com.lkj.springbootinit.model.dto.userinterface.UpdateUserInterfaceInfoDTO;
import com.lkj.springbootinit.model.vo.InterfaceInfoVo;
import com.lkj.springbootinit.model.vo.UserInterfaceInfoVO;
import com.lkj.springbootinit.service.InterfaceInfoService;
import com.lkj.springbootinit.service.UserInterfaceInfoService;
import com.lkj.springbootinit.service.UserService;
import com.lkj.apicommon.common.ErrorCode;
import com.lkj.apicommon.exception.BusinessException;
import com.lkj.apicommon.entity.InterfaceInfo;
import com.lkj.apicommon.entity.User;
import com.lkj.apicommon.entity.UserInterfaceInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author PC
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2023-12-09 10:57:09
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService {

    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private UserService userService;

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean b) {
        //判断接口信息对想是否为空，为空则抛出参数错误的异常
        if (userInterfaceInfo == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if (userInterfaceInfo.getLeftNum() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"剩余次数不能小于0");
        }
    }

    /**
     * 根据调用者id与接口id统计用户调用次数
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    //添加事务
    @Transactional
    @Override
    public boolean invokeCount(long userId, long interfaceInfoId) {
        //判断（其实这里还应该判断校验存不存在，这里就不用校验了，因为它不存在，也更新不到那条记录）
        if (interfaceInfoId <= 0 || userId <= 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户或者接口不存在");
        }
        //使用UpdateWrapper对象构建更新条件
        QueryWrapper<UserInterfaceInfo> queryWrapper= new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        queryWrapper.eq("userId",userId);

        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(queryWrapper);
        //获取乐观锁版本号，剩余调用次数
        Integer version = userInterfaceInfo.getVersion();
        Integer leftNum = userInterfaceInfo.getLeftNum();
        if (leftNum<=0){
            log.error("接口剩余调用次数不足");
            return false;
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId",userId);
        updateWrapper.eq("interfaceInfoId",interfaceInfoId);
        updateWrapper.eq("version",version);
        updateWrapper.gt("leftNum",0);
        //setSql方法用于设置要更新的SQL语句。这里通过SQL表达式实现了两个字段的更新操作：
        //leftNum = leftNum - 1 和totalNum = totalNum + 1。意思是将leftNum字段减1，totalNum字段加1，版本号+1.
        updateWrapper.setSql("totalNum = totalNum +1,leftNum = leftNum-1,version = version+1");
        return this.update(updateWrapper);
    }

    @Override
    public boolean recoverInvokeCount(long userId, long interfaceInfoId) {
        if (userId<0 || interfaceInfoId<0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户或接口不存在");
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId",userId);
        updateWrapper.eq("interfaceInfoId",interfaceInfoId);
        updateWrapper.gt("leftNum",0);
        updateWrapper.setSql("totalNum = totalNum -1,leftNum = leftNum+1,version = version+1");
        return this.update(updateWrapper);
    }

    /**
     * 获取接口的剩余调用次数
     * @param userId
     * @param interfaceInfoId
     * @return
     */
    @Override
    public int getLeftInvokeCount(long userId, long interfaceInfoId) {
        //1.根据用户id和接口id获取用户接口关系详情对象
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId",userId);
        queryWrapper.eq("interfaceInfoId",interfaceInfoId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(queryWrapper);
        //2.从用户接口关系详情对象中获取剩余调用次数
        return userInterfaceInfo.getLeftNum();
    }

    /**
     * 更新用户接口表
     * @param updateUserInterfaceInfoDTO
     * @return
     */
    @Override
    public boolean updateUserInterfaceInfo(UpdateUserInterfaceInfoDTO updateUserInterfaceInfoDTO) {
        Long userId = updateUserInterfaceInfoDTO.getUserId();
        Long interfaceId = updateUserInterfaceInfoDTO.getInterfaceId();
        Long lockNum = updateUserInterfaceInfoDTO.getLockNum();

        if(interfaceId == null || userId == null || lockNum == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        UserInterfaceInfo one = this.getOne(
                new QueryWrapper<UserInterfaceInfo>()
                        .eq("userId", userId)
                        .eq("interfaceInfoId", interfaceId)
        );

        if (one != null) {
            // 说明是增加数量
            return this.update(
                    new UpdateWrapper<UserInterfaceInfo>()
                            .eq("userId", userId)
                            .eq("interfaceInfoId", interfaceId)
                            .setSql("leftNum = leftNum + " + lockNum)
            );
        } else {
            // 说明是第一次购买
            UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
            userInterfaceInfo.setUserId(userId);
            userInterfaceInfo.setInterfaceInfoId(interfaceId);
            userInterfaceInfo.setLeftNum(Math.toIntExact(lockNum));
            return this.save(userInterfaceInfo);
        }
    }

    /**
     * 通过用户id获取拥有的接口信息
     * @param userId
     * @param request
     * @return
     */
    @Override
    public List<UserInterfaceInfoVO> getInterfaceInfoByUserId(Long userId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        // 判断用户是否有权限
        if(!loginUser.getId().equals(userId) && !loginUser.getUserRole().equals(UserConstant.ADMIN_ROLE)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }

        // 获取用户可调用接口列表
        QueryWrapper<UserInterfaceInfo> userInterfaceInfoQueryWrapper= new QueryWrapper<>();
        userInterfaceInfoQueryWrapper.eq("userId",loginUser.getId());
        List<UserInterfaceInfo> userInterfaceInfoList = this.list(userInterfaceInfoQueryWrapper);

        Map<Long, List<UserInterfaceInfo>> interfaceIdUserInterfaceInfoMap = userInterfaceInfoList.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        Set<Long> interfaceIds = interfaceIdUserInterfaceInfoMap.keySet();
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        if(CollectionUtil.isEmpty(interfaceIds)){
            return new ArrayList<>();
        }
        interfaceInfoQueryWrapper.in("id",interfaceIds);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(interfaceInfoQueryWrapper);
        List<UserInterfaceInfoVO> userInterfaceInfoVOList = interfaceInfoList.stream().map(interfaceInfo -> {
            UserInterfaceInfoVO userInterfaceInfoVO = new UserInterfaceInfoVO();
            // 复制接口信息
            BeanUtils.copyProperties(interfaceInfo, userInterfaceInfoVO);
            userInterfaceInfoVO.setInterfaceStatus(Integer.valueOf(interfaceInfo.getStatus()));

            // 复制用户调用接口信息
            List<UserInterfaceInfo> userInterfaceInfos = interfaceIdUserInterfaceInfoMap.get(interfaceInfo.getId());
            UserInterfaceInfo userInterfaceInfo = userInterfaceInfos.get(0);
            BeanUtils.copyProperties(userInterfaceInfo, userInterfaceInfoVO);
            return userInterfaceInfoVO;
        }).collect(Collectors.toList());
        return userInterfaceInfoVOList;
    }

    /**
     * 获取调用接口的前几排行
     * @param limit
     * @return
     */
    @Override
    public List<InterfaceInfoVo> interfaceInvokeTopAnalysis(int limit) {
        List<UserInterfaceInfo> userInterfaceInfoList = userInterfaceInfoMapper.listTopInvokeInterfaceInfo(limit);
        Map<Long, List<UserInterfaceInfo>> interfaceInfoIdObjMap = userInterfaceInfoList.stream()
                .collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", interfaceInfoIdObjMap.keySet());
        List<InterfaceInfo> list = interfaceInfoService.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        }

        List<InterfaceInfoVo> interfaceInfoVOList = list.stream().map(interfaceInfo -> {
            InterfaceInfoVo interfaceInfoVO = new InterfaceInfoVo();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVO);
            int totalNum = interfaceInfoIdObjMap.get(interfaceInfo.getId()).get(0).getTotalNum();
            interfaceInfoVO.setTotalNum(totalNum);
            return interfaceInfoVO;
        }).collect(Collectors.toList());
        return interfaceInfoVOList;
    }
}




