package com.cb.gulimall.search.service;

import com.cb.gulimall.search.vo.SearchParam;
import com.cb.gulimall.search.vo.SearchResult;

public interface MallSerchService {
    SearchResult search(SearchParam searchParam);
}
