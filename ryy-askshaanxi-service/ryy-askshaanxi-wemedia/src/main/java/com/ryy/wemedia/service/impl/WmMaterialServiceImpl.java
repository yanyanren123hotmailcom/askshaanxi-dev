package com.ryy.wemedia.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.wemedia.dtos.WmMaterialDto;
import com.ryy.model.wemedia.pojos.WmMaterial;
import com.ryy.wemedia.mapper.WmMaterialMapper;
import com.ryy.wemedia.service.WmMaterialService;
import org.springframework.web.multipart.MultipartFile;

public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial>  implements WmMaterialService {

    @Override
    public ResponseResult uploadPicture(MultipartFile file){

        return null;
    }

    @Override
    public ResponseResult findList( WmMaterialDto dto){
        return null;
    }
}
