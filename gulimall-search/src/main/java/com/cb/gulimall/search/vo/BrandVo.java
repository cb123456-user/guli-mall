package com.cb.gulimall.search.vo;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class BrandVo {

    private Long brandId;
    private String name;
}
