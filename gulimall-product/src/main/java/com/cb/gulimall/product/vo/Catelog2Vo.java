package com.cb.gulimall.product.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 二级分类vo
 */
@Data
@Accessors(chain = true)
public class Catelog2Vo {

    /**
     * 二级分类id
     */
    private String id;

    private String name;

    /**
     * 一级分类id
     */
    private String catalog1Id;

    private List<Catalog3Vo> catalog3List;
}
