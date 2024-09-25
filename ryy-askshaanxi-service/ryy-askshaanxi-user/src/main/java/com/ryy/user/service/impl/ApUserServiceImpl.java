package com.ryy.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.ryy.common.exception.CustomException;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import com.ryy.model.user.dtos.LoginDto;
import com.ryy.model.user.pojos.ApUserLogin;
import com.ryy.user.mapper.ApUserLoginMapper;
import com.ryy.user.service.ApUserService;
import com.ryy.utils.common.AppJwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@Slf4j
public class ApUserServiceImpl implements ApUserService {
    @Autowired
    private ApUserLoginMapper apUserLoginMapper;
    @Override
    public ResponseResult login(LoginDto dto) {

        if(!StringUtils.isEmpty(dto.getPhone()) &&!StringUtils.isEmpty(dto.getPassword())){
            //正常登录
            LambdaQueryWrapper<ApUserLogin> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ApUserLogin::getPhone,dto.getPhone());
            ApUserLogin apUserLogin = apUserLoginMapper.selectOne(wrapper);
            //判断是否查询到此用户
            if(apUserLogin==null){
                throw new CustomException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
            }
            String inputPs=dto.getPassword()+apUserLogin.getSalt();
            String inputPsMd5=DigestUtils.md5DigestAsHex(inputPs.getBytes());

            if(!inputPsMd5.equals(apUserLogin.getPassword())){
                throw new CustomException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
            }
            Map<String,Object> result=new HashMap<>();
            apUserLogin.setPassword("");
            apUserLogin.setSalt("");
            result.put("user",apUserLogin);
            result.put("token",AppJwtUtil.getToken(apUserLogin.getId().longValue()));
            return ResponseResult.okResult(result);

        }else{
            //游客登录
            Map<String,Object> result=new HashMap<>();
            result.put("token",AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(result);
        }
    }
}
