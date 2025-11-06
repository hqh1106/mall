package com.hqh.mall.service.impl;

import com.hqh.mall.dao.PortalMemberInfoDao;
import com.hqh.mall.domain.PortalMemberInfo;
import com.hqh.mall.service.UmsMemberCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UmsMemberCenterServiceImpl implements UmsMemberCenterService {

    @Autowired
    private PortalMemberInfoDao portalMemberInfoDao;

    /**
     * 查询会员信息
     * @param memberId
     * @return
     */
    @Override
    public PortalMemberInfo getMemberInfo(Long memberId) {
        return portalMemberInfoDao.getMemberInfo(memberId);
    }
}
