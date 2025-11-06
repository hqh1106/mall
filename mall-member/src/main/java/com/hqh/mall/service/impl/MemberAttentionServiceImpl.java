package com.hqh.mall.service.impl;

import com.hqh.mall.domain.MemberBrandAttention;
import com.hqh.mall.repository.MemberBrandAttentionRepository;
import com.hqh.mall.service.MemberAttentionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会员关注Service实现类
 */
@Service
public class MemberAttentionServiceImpl implements MemberAttentionService {

    @Autowired
    private MemberBrandAttentionRepository memberBrandAttentionRepository;

    @Override
    public int add(MemberBrandAttention memberBrandAttention) {
        MemberBrandAttention findAttention = memberBrandAttentionRepository.findByMemberIdAndBrandId(memberBrandAttention.getMemberId(), memberBrandAttention.getBrandId());
        if (findAttention == null) {
            memberBrandAttentionRepository.save(findAttention);
            return 1;
        }
        return 0;
    }

    @Override
    public int delete(Long memberId, Long brandId) {
        return memberBrandAttentionRepository.deleteByMemberIdAndBrandId(memberId, brandId);
    }

    @Override
    public List<MemberBrandAttention> list(Long memberId) {
        return memberBrandAttentionRepository.findByMemberId(memberId);
    }
}
