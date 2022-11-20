package com.cb.gulimall.search.controller;

import com.cb.gulimall.search.service.MallSerchService;
import com.cb.gulimall.search.vo.SearchParam;
import com.cb.gulimall.search.vo.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class SearchController {

    @Autowired
    private MallSerchService mallSerchService;

    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model, HttpServletRequest request){
        searchParam.setQueryString(request.getQueryString());
        SearchResult searchResult = mallSerchService.search(searchParam);
        model.addAttribute("result", searchResult);
        return "index";
    }
}
