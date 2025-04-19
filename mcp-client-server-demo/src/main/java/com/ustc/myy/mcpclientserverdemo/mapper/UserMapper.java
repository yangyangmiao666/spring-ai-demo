package com.ustc.myy.mcpclientserverdemo.mapper;

import com.mybatisflex.core.BaseMapper;
import com.ustc.myy.mcpclientserverdemo.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表 Mapper 接口
 *
 * @author YangyangMiao
 * @email yangyangmiao666@outlook.com
 * @date 2025/4/19 22:18
 * @version 1.0
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
