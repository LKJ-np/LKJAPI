package com.lkj.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lkj.apicommon.entity.UserInterfaceInfo;
import com.lkj.springbootinit.model.vo.UserInterfaceInfoAnalysisVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author PC
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2023-12-09 10:57:09
* @Entity generator.domain.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {

    List<UserInterfaceInfoAnalysisVo> listTopInterfaceInfo(@Param("size") int size);

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);
}




