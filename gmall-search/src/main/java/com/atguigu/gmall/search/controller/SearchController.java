package com.atguigu.gmall.search.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.search.pojo.SearchParamVo;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @GetMapping
//    @ResponseBody
    public String search(SearchParamVo searchParamVo, Model model){
        SearchResponseVo responseVo =  searchService.search(searchParamVo);
        //key=response，不是随便写的，是前端页面main组件中th:Object="${response}"中写的，解析的Object是response
        model.addAttribute("response", responseVo);
        return "search";
    }

}
