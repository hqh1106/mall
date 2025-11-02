package com.hqh.leafcore;

import com.hqh.leafcore.common.Result;

public interface IDGen {

    public Result get(String key);

    boolean init();
}
