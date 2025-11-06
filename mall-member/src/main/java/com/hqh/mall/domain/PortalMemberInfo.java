package com.hqh.mall.domain;


import com.hqh.mall.model.UmsMember;
import com.hqh.mall.model.UmsMemberLevel;
import lombok.Data;


@Data
public class PortalMemberInfo extends UmsMember {
    private UmsMemberLevel umsMemberLevel;
}
