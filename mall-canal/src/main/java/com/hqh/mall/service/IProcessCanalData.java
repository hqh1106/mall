package com.hqh.mall.service;

import com.google.protobuf.InvalidProtocolBufferException;

public interface IProcessCanalData {
    void connect();

    void disConnect();

    void processData() throws InvalidProtocolBufferException;
}
