package com.hqh.mall.service;


import com.hqh.mall.domain.MemberProductCollection;

import java.util.List;

/**
 * 会员收藏Service
 */
public interface MemberCollectionService {

    int addProduct(MemberProductCollection productCollection);

    int deleteProduct(Long memberId, Long productId);

    List<MemberProductCollection> listProduct(Long memberId);
}
