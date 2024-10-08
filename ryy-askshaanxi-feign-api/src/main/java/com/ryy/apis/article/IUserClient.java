package com.ryy.apis.article;

import com.ryy.model.common.dtos.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "askshaanxi-user")
public interface IUserClient {
    @GetMapping("/api/v1/user/get/{id}")
    public ResponseResult getById(@PathVariable Integer id);
}
