package com.cb.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cb.common.utils.HttpUtils;
import com.cb.common.vo.MemberLoginVo;
import com.cb.common.vo.MemberRegistVo;
import com.cb.common.vo.SocialUser;
import com.cb.gulimall.member.entity.MemberLevelEntity;
import com.cb.gulimall.member.exceptiion.PhoneExistException;
import com.cb.gulimall.member.exceptiion.UserNameExistException;
import com.cb.gulimall.member.service.MemberLevelService;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cb.common.utils.PageUtils;
import com.cb.common.utils.Query;

import com.cb.gulimall.member.dao.MemberDao;
import com.cb.gulimall.member.entity.MemberEntity;
import com.cb.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    private MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 用户注册
     *
     * @param vo
     */
    @Override
    public void resist(MemberRegistVo vo) {

        MemberEntity memberEntity = new MemberEntity();

        // 用户名、电话号码需唯一
        int userNameCount = this.count(new QueryWrapper<MemberEntity>().lambda()
                .eq(MemberEntity::getUsername, vo.getUserName()));
        if (userNameCount > 0) {
            throw new UserNameExistException();
        }
        int phoneCount = this.count(new QueryWrapper<MemberEntity>().lambda()
                .eq(MemberEntity::getMobile, vo.getPhone()));
        if (phoneCount > 0) {
            throw new PhoneExistException();
        }
        memberEntity.setUsername(vo.getUserName()).setMobile(vo.getPhone());

        // 查询默认会员等级
        MemberLevelEntity levelEntity = memberLevelService.getOne(new QueryWrapper<MemberLevelEntity>().lambda()
                .eq(MemberLevelEntity::getDefaultStatus, 1));
        if (levelEntity != null) {
            memberEntity.setLevelId(levelEntity.getId());
        }

        // 密码加密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encode = passwordEncoder.encode(vo.getPassword());
        memberEntity.setPassword(encode);

        memberEntity.setCreateTime(new Date()).setStatus(0).setNickname(vo.getUserName());

        this.save(memberEntity);

    }

    @Override
    public MemberEntity login(MemberLoginVo vo) {
        String loginacct = vo.getLoginacct();
        String password = vo.getPassword();
        // 查询账号，手机号
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().lambda()
                .eq(MemberEntity::getUsername, loginacct)
                .or().eq(MemberEntity::getMobile, loginacct)
        );
        // 查询不到，账号错误
        if (memberEntity == null) {
            memberEntity.setPassword(null);
            return null;
        }
        // 查询到了，验证秘密
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        boolean matches = passwordEncoder.matches(password, memberEntity.getPassword());
        if (matches) {
            return memberEntity;
        }

        return null;
    }

    /**
     * 如果存在，返回用户信息
     * 否则，为社交账号创建一个对应的用户，并返回用户信息
     */
    @Override
    public MemberEntity login(SocialUser socialUser) {
        // 根据uid查询用户信息
        MemberEntity memberEntity = this.getOne(new QueryWrapper<MemberEntity>().lambda().eq(MemberEntity::getSocialUid, socialUser.getUid()));

        // 用户账号存在，更新信息
        if (memberEntity != null) {
            MemberEntity entity = new MemberEntity();
            entity.setId(memberEntity.getId()).setExpiresIn(socialUser.getExpiresIn()).setAccessToken(socialUser.getAccessToken());
            this.updateById(entity);
            // 重置token信息
            memberEntity.setExpiresIn(socialUser.getExpiresIn()).setAccessToken(socialUser.getAccessToken()).setPassword(null);
            return memberEntity;
        }
        // 注册一个对应的社交账号
        memberEntity = new MemberEntity();
        try {
            // 获取用户信息 https://api.weibo.com/2/users/show.json
            Map<String, String> querys = new HashMap<>();
            querys.put("access_token", socialUser.getAccessToken());
            querys.put("uid", socialUser.getUid());
            Map<String, String> headers = new HashMap<>();
            HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", headers, querys);
            // 成功设置用户信息
            if (response.getStatusLine().getStatusCode() == 200) {
                String json = EntityUtils.toString(response.getEntity());
                JSONObject jsonObject = JSON.parseObject(json);
                // 可以获取很多开发的用户信息
                memberEntity.setNickname(jsonObject.getString("name"))
                        .setUsername(jsonObject.getString("name"))
                        .setGender("m".equals(jsonObject.getString("gender")) ? 1 : 0)
                        .setCity(jsonObject.getString("location"))
                        .setEmail(jsonObject.getString("email"));
            }
        } catch (Exception e) {
            log.error("weibo远程查询用户信息失败，error {}", e);
        }
        memberEntity.setStatus(0)
                .setCreateTime(new Date())
                .setLevelId(1L)
                .setSocialUid(socialUser.getUid())
                .setAccessToken(socialUser.getAccessToken())
                .setExpiresIn(socialUser.getExpiresIn());
        this.save(memberEntity);
        memberEntity.setPassword(null);
        return memberEntity;
    }

}