package com.ryy.wemedia.service.impl;


import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryy.common.constants.WemediaConstants;
import com.ryy.model.common.dtos.PageResponseResult;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import com.ryy.model.wemedia.dtos.WmNewsDto;
import com.ryy.model.wemedia.dtos.WmNewsPageReqDto;
import com.ryy.model.wemedia.pojos.WmMaterial;
import com.ryy.model.wemedia.pojos.WmNews;
import com.ryy.model.wemedia.pojos.WmNewsMaterial;
import com.ryy.utils.thread.WmThreadLocalUtil;
import com.ryy.wemedia.mapper.WmMaterialMapper;
import com.ryy.wemedia.mapper.WmNewsMapper;
import com.ryy.wemedia.mapper.WmNewsMaterialMapper;
import com.ryy.wemedia.service.WmNewsAutoScanService;
import com.ryy.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {
    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    /**
     * 条件查询文章列表
     *
     * @param dto
     * @return
     */
    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        //1.检查参数
        //分页检查
        dto.checkParam();

        //2.分页条件查询
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lambdaQueryWrapper = new LambdaQueryWrapper();
        //状态精确查询
        if (dto.getStatus() != null) {
            lambdaQueryWrapper.eq(WmNews::getStatus, dto.getStatus());
        }

        //频道精确查询
        if (dto.getChannelId() != null) {
            lambdaQueryWrapper.eq(WmNews::getChannelId, dto.getChannelId());
        }

        //时间范围查询
        if (dto.getBeginPubDate() != null && dto.getEndPubDate() != null) {
            lambdaQueryWrapper.between(WmNews::getPublishTime, dto.getBeginPubDate(), dto.getEndPubDate());
        }

        //关键字的模糊查询
        if (StringUtils.isNotBlank(dto.getKeyword())) {
            lambdaQueryWrapper.like(WmNews::getTitle, dto.getKeyword());
        }

        //查询当前登录人的文章
        lambdaQueryWrapper.eq(WmNews::getUserId, WmThreadLocalUtil.getUser());

        //按照发布时间倒序查询
        lambdaQueryWrapper.orderByDesc(WmNews::getPublishTime);


        page = page(page, lambdaQueryWrapper);

        //3.结果返回
        ResponseResult responseResult = new PageResponseResult(dto.getPage(), dto.getSize(), (int) page.getTotal());
        responseResult.setData(page.getRecords());


        return responseResult;
    }

    /**
     * 发布修改文章或保存为草稿
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //校验
        if(dto==null || StringUtils.isEmpty(dto.getTitle())|| StringUtils.isEmpty(dto.getContent())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_REQUIRE);
        }

        //获取内容中的图片
        List<String> images=getContentImages(dto);
        WmNews wmNews=saveOrUpdate(dto,images);
        //草稿
        if(dto.getStatus()==WmNews.Status.NORMAL.getCode()){
            return ResponseResult.okResult("");
        }
        //保存文章与素材，文章与article端文章关联关系
        //保存文章内容与素材的关联关系
        saveRelationsMaterial(images,wmNews.getId(),WemediaConstants.WM_CONTENT_REFERENCE);
        //保存文章封面与素材的关联关系
        String coverImages=wmNews.getImages();
        if(!StringUtils.isEmpty(coverImages)){
            String[] imageArray = org.springframework.util.StringUtils.commaDelimitedListToStringArray(coverImages);
            saveRelationsMaterial(Arrays.asList(imageArray),wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }
        //文章审核
        wmNewsAutoScanService.autoScanWmNews(wmNews,images);
        return ResponseResult.okResult(wmNews);
    }

    private void saveRelationsMaterial(List<String> images, Integer newsId, Short type) {
        if(CollectionUtils.isEmpty(images)){
            log.info("没有图片");
            return;
        }
        LambdaQueryWrapper<WmMaterial> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(WmMaterial::getUrl,images);

        List<WmMaterial> wmMaterials=wmMaterialMapper.selectList(wrapper);
        //获取素材所有id
        List<Integer> materialIds = wmMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
        wmNewsMaterialMapper.saveRelations(materialIds,newsId,type);

    }

    private WmNews saveOrUpdate(WmNewsDto dto,List<String> contentImages){
        WmNews wmNews=new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        //补充参数
        wmNews.setUserId(WmThreadLocalUtil.getUser());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架

        //获取封面图片
        List<String > images=getCoverImages(dto,contentImages,wmNews);
        wmNews.setImages(StringUtils.join(images,","));
        if(dto.getId()!=null){
            //删除关联关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,dto.getId()));
            updateById(wmNews);
        }else{
            save(wmNews);
        }

        return wmNews;
    }

    private List<String> getContentImages(WmNewsDto dto) {
        List<String> images=new ArrayList<>();
        String content=dto.getContent();
        List<Map> contentImgs = JSONArray.parseArray(content, Map.class);
        if(CollectionUtils.isEmpty(contentImgs)){
            return images;
        }
        for(Map contentImg:contentImgs){
            String type=contentImg.get("type")+"";
            if("image".equals(type)){
                String url = contentImg.get("value")+"";
                images.add(url);
            }
        }
        return images;
    }
    private List<String> getCoverImages(WmNewsDto dto,List<String> contentImages,WmNews wmNews){
        //默认获取到的封面图片就为前端传的
        List<String> images=dto.getImages();
        //处理自动封面:WmNewsDto中的type与WmNews中的type不同
        if(dto.getType()==-1){
            int size=contentImages.size();
            if(size>=3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images=contentImages.stream().limit(3).collect(Collectors.toList());
            } else if (size>=1) {
                images=contentImages.stream().limit(1).collect(Collectors.toList());
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
            }else{
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }
        }
        return images;
    }


    /**
     * 保存或修改文章
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        //补全属性
        wmNews.setUserId(WmThreadLocalUtil.getUser());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);//默认上架

        if(wmNews.getId() == null){
            //保存
            save(wmNews);
        }else {
            //修改
            //删除文章图片与素材的关系
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }


}
