package com.ustc.myy.springaitoolsdemo.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.mybatisflex.core.BaseMapper;
import com.ustc.myy.springaitoolsdemo.entity.User;

/**
 * 用户表 Mapper 接口
 *
 * @author YangyangMiao
 * @email yangyangmiao666@icloud.com
 * @date 2025/4/19 22:18
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
