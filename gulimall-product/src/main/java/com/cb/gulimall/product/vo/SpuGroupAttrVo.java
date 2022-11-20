package com.cb.gulimall.product.vo;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class SpuGroupAttrVo {

    private String groupName;

    private List<SpuBaseAttrVo> attrs;
}
