package com.cb.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.cb.common.valid.AddGroup;
import com.cb.common.valid.ShowList;
import com.cb.common.valid.UpdateGroup;
import com.cb.common.valid.UpdateStatus;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Range;
import org.hibernate.validator.constraints.URL;

import javax.validation.constraints.*;

/**
 * 品牌
 *
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 16:51:48
 */
@Data
@Accessors(chain = true)
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @Null(message = "新增时不能提交品牌id", groups = {AddGroup.class})
    @NotNull(message = "更新时必须提交品牌id", groups = {UpdateGroup.class, UpdateStatus.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名不能为空", groups = {AddGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotBlank(message = "logo不为空", groups = {AddGroup.class})
    @URL(message = "logo必须是一个url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(message = "显示状态不能为空", groups = {AddGroup.class, UpdateGroup.class, UpdateStatus.class})
//    @Range(min = 0, max = 1, message = "显示状态只能为0或1", groups = {AddGroup.class, UpdateGroup.class})
    @ShowList(value = {1, 0}, groups = {AddGroup.class, UpdateGroup.class, UpdateStatus.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(message = "检索首字母不能为空", groups = {AddGroup.class})
    @Pattern(regexp = "^[a-zA-Z]$", message = "检索首字母必须是一个字母", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(message = "排序不能为空", groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须是一个大于等于0的整数", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}
