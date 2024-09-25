package com.ryy.user.service;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.user.dtos.LoginDto;

public interface ApUserService {
    ResponseResult login(LoginDto dto);
}
