package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Controller
public class IndexController {

    @Autowired
    private IndexService indexService;

    @GetMapping
    public String toIndex(Model model){

        //获取一级分类
        List<CategoryEntity> categories =  indexService.queryLv1Categories();
        model.addAttribute("categories", categories);//第一个categories是从前端工程templates中的headers里面找到的变量名

        //TODO：获取广告信息

        return "index";

    }

    @GetMapping("/index/cates/{pid}")
    public ResponseVo<List<CategoryEntity>> queryLv2CategoriesWithSubsByPid(
            @PathVariable("pid")Long pid
    ) {
        List<CategoryEntity> categoryEntities = indexService.queryLv2CategoriesWithSubsByPid(pid);
        return ResponseVo.ok(categoryEntities);
    }

}
