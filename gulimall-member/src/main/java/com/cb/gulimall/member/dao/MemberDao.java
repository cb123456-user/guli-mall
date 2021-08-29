package com.cb.gulimall.member.dao;

import com.cb.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author chenbin
 * @email chenbin@gmail.com
 * @date 2021-08-29 21:18:01
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
