package com.ryy.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ryy.model.user.pojos.ApUserLogin;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApUserLoginMapper extends BaseMapper<ApUserLogin> {
    ApUserLogin selectByPhone(String phone);
}
