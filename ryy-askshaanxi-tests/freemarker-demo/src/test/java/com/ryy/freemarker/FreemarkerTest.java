package com.ryy.freemarker;


import com.ryy.freemarker.pojo.Student;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//@RunWith(SpringRunner.class)：使用 SpringRunner 作为测试运行器。
//@SpringBootTest(classes = FreeApp.class)：在 Spring Boot 环境中运行测试，并启动 FreeApp 类。
@SpringBootTest(classes = FreeMarkerApp.class)
@RunWith(SpringRunner.class)
public class FreemarkerTest {


    @Autowired
    private Configuration configuration;


    @Test
    public void test() throws IOException, TemplateException {
        Template template=configuration.getTemplate("01-basic.ftl");
        Map<String,Object> dataModel=getData();
        template.process(dataModel,new FileWriter("D://test.html"));
    }


    @Test
    public void test1() throws IOException, TemplateException {
        Template template=configuration.getTemplate("02-list.ftl");
        Map<String,Object> dataModel=getData1();
        template.process(dataModel,new FileWriter("D://test1.html"));
    }
    private Map<String,Object> getData1(){
        Map<String,Object> model=new HashMap<>();

        model.put("name","ryy");
        Student stu1=new Student();
        stu1.setName("bjm");
        stu1.setAge(23);
        stu1.setMoney(0.0F);

        Student stu2=new Student();
        stu2.setName("ryy");
        stu2.setAge(24);
        stu2.setMoney(10.0F);
        List<Student> list=new ArrayList<>();
        list.add(stu1);
        list.add(stu2);
        model.put("stus",list);

        Map<String,Object> map=new HashMap<>();
        map.put("stu1",stu1);
        map.put("stu2",stu2);

        model.put("stuMap",map);
        return model;
    }
    private Map<String,Object> getData(){
        Map<String,Object> model=new HashMap<>();

        model.put("name","ryy");
        Student stu=new Student();
        stu.setName("bjm");
        stu.setAge(23);

        model.put("stu",stu);
        return model;
    }
}
