package com.hqh.mall.component.zklock;

public interface ZkLock {
    boolean lock(String lockpath);

    boolean unlock(String lockpath);

}
