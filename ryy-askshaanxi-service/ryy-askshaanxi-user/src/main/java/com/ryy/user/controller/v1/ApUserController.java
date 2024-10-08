package com.ryy.user.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.user.mapper.ApUserDetailsMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApUserController {

    @Autowired
    ApUserDetailsMapper apUserDetailsMapper;
    @GetMapping("/api/v1/user/get/{id}")
    public ResponseResult getById(@PathVariable Integer id){
        return ResponseResult.okResult(apUserDetailsMapper.selectById(id));
    }
}
