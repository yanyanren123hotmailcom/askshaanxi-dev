package com.ryy.article.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ryy.article.mapper.ApArticleConfigMapper;
import com.ryy.article.mapper.ApArticleContentMapper;
import com.ryy.article.mapper.ApArticleMapper;
import com.ryy.article.service.ApArticleService;
import com.ryy.common.constants.ArticleConstants;
import com.ryy.file.service.FileStorageService;
import com.ryy.model.article.dtos.ArticleDto;
import com.ryy.model.article.dtos.ArticleHomeDto;
import com.ryy.model.article.pojos.ApArticle;
import com.ryy.model.article.pojos.ApArticleConfig;
import com.ryy.model.article.pojos.ApArticleContent;
import com.ryy.model.common.dtos.ResponseResult;
import com.ryy.model.common.enums.AppHttpCodeEnum;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional
@Slf4j
public class ApArticleServiceImpl  extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    // 单页最大加载的数字
    private final static short MAX_PAGE_SIZE = 50;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;
    @Autowired
    private Configuration configuration;
    @Autowired
    private FileStorageService fileStorageService;
    /**
     * 根据参数加载文章列表
     * @param loadtype 1为加载更多  2为加载最新
     * @param dto
     * @return
     */
    @Override
    public ResponseResult load(Short loadtype, ArticleHomeDto dto) {
        //1.校验参数
        Integer size = dto.getSize();
        if(size == null || size == 0){
            size = 10;
        }
        size = Math.min(size,MAX_PAGE_SIZE);
        dto.setSize(size);

        //类型参数检验
        if(!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_MORE)&&!loadtype.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            loadtype = ArticleConstants.LOADTYPE_LOAD_MORE;
        }
        //文章频道校验
        if(StringUtils.isEmpty(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        //时间校验
        if(dto.getMaxBehotTime() == null) dto.setMaxBehotTime(new Date());
        if(dto.getMinBehotTime() == null) dto.setMinBehotTime(new Date());
        //2.查询数据
        List<ApArticle> apArticles = apArticleMapper.loadArticleList(dto, loadtype);

        //3.结果封装
        ResponseResult responseResult = ResponseResult.okResult(apArticles);
        return responseResult;
    }
    /**
     * 同步文章，自媒体端调用
     * @param dto
     * @return
     */
    @Override
    public ResponseResult save(ArticleDto dto) {

        ApArticle article=new ApArticle();
        BeanUtils.copyProperties(dto,article);

        if(dto.getId()==null){
            //新增三张表ap_article,ap_article_config,ap_article_content
            apArticleMapper.insert(article);

            ApArticleConfig config=new ApArticleConfig();
            config.setArticleId(article.getId());
            config.setIsComment(true);
            config.setIsDown(false);
            config.setIsForward(true);
            config.setIsDelete(false);
            apArticleConfigMapper.insert(config);

            ApArticleContent content=new ApArticleContent();
            content.setArticleId(article.getId());
            content.setContent(dto.getContent());
            apArticleContentMapper.insert(content);


            //配置statusUrl
            try{
                article=setArticleStaticUrl(article);
            }catch (Exception e){
                return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR,"内容生成freemarker失败");
            }
            apArticleMapper.updateById(article);

        }else{
            //修改文章
            apArticleMapper.updateById(article);
            //修改文章内容
            ApArticleContent content=apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId,dto.getId()));
            content.setContent(dto.getContent());
            apArticleContentMapper.updateById(content);

        }
        return ResponseResult.okResult(article.getId());
    }

    private ApArticle setArticleStaticUrl(ApArticle article) throws IOException, TemplateException {
        //获取文章内容
        ApArticleContent apArticleContent=apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                .eq(ApArticleContent::getArticleId,article.getId()));
        if(apArticleContent!=null && com.baomidou.mybatisplus.core.toolkit.StringUtils.isNotBlank(apArticleContent.getContent())){
            //文章内容通过freemarker生成html
            StringWriter out=new StringWriter();
            Template template=configuration.getTemplate("article.ftl");

            Map<String,Object> params=new HashMap<>();
            params.put("content",  JSONArray.parseArray(apArticleContent.getContent()));

            template.process(params,out);

            InputStream is=new ByteArrayInputStream(out.toString().getBytes());

            //html文件上传到minio中
            String path=fileStorageService.uploadHtmlFile("",apArticleContent.getArticleId()+".html",is);

            //修改ap_article表
            article.setStaticUrl(path);
        }
        return article;
    }

}