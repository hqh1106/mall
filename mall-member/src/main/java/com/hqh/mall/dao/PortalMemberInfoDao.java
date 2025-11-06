package com.hqh.mall.dao;

import com.hqh.mall.domain.PortalMemberInfo;

/**
 * 会员信息获取
 */
public interface PortalMemberInfoDao {

    PortalMemberInfo getMemberInfo(Long memberId);
}
