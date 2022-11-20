package com.cb.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 三级分类vo
 */
@Data
@Accessors(chain = true)
public class Catalog3Vo {

    /**
     * 三级分类id
     */
    private String id;

    private String name;

    /**
     * 二级分类id
     */
    private String catalog2Id;
}
