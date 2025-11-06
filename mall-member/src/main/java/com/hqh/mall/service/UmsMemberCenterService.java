package com.hqh.mall.service;


import com.hqh.mall.domain.PortalMemberInfo;

public interface UmsMemberCenterService {

    /**
     * 查询会员信息
     * @param memberId
     * @return
     */
    PortalMemberInfo getMemberInfo(Long memberId);
}
