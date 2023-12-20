package com.lkj.springbootinit.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lkj.apicommon.entity.InterfaceInfo;

/**
* @author PC
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2023-12-04 22:14:53
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {


    void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add);
}
