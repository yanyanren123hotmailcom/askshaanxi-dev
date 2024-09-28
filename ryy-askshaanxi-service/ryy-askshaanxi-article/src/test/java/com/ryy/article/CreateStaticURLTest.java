package com.ryy.article;


import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ryy.article.mapper.ApArticleContentMapper;
import com.ryy.article.mapper.ApArticleMapper;
import com.ryy.file.service.FileStorageService;
import com.ryy.model.article.pojos.ApArticle;
import com.ryy.model.article.pojos.ApArticleContent;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest(classes = ArticleApplication.class)
@RunWith(SpringRunner.class)
public class CreateStaticURLTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

    @Autowired
    private Configuration configuration;

    @Test
    public void createStaticUrlTest() throws IOException, TemplateException {

        //获取所有文章
        List<ApArticle> articleList=apArticleMapper.selectList(new QueryWrapper<ApArticle>().lambda());

        for (ApArticle article : articleList) {
            //获取文章内容
            ApArticleContent apArticleContent=apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery()
                    .eq(ApArticleContent::getArticleId,article.getId()));
            if(apArticleContent!=null && StringUtils.isNotBlank(apArticleContent.getContent())){
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
                apArticleMapper.updateById(article);
            }
        }
    }
}
