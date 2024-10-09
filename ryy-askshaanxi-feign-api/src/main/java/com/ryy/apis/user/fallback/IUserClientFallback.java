package com.ryy.apis.user.fallback;

import com.ryy.apis.user.IUserClient;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class IUserClientFallback implements IUserClient {
    @Override
    public ResponseResult getById(Integer id) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"获取数据失败2");
    }
}
