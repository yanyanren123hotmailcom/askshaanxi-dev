package com.ryy.search.service;

import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.search.dtos.UserSearchDto;

public interface ApUserSearchService {

    ResponseResult search(UserSearchDto dto);

}
