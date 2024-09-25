package com.ryy.user.controller.v1;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.user.dtos.LoginDto;
import com.ryy.user.service.ApUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/login")
public class ApUserLoginController {
    //基于注解的注入
    //@Autowired
    private ApUserService apUserService;

    //构造函数注入
    @Autowired
    public ApUserLoginController(ApUserService apUserService){

        this.apUserService=apUserService;
    }

    @PostMapping("/login_auth")
    public ResponseResult login(@RequestBody LoginDto dto){
        return apUserService.login(dto);
    }


}
