package com.hqh.mall.constant;

/**
 * Member constant interface
 * 接口中定义的变量默认为 public static final
 */
public interface MemberConstant {
    /**
     * 会员服务第三方客户端(这个客户端在认证服务器配置好的oauth_client_details)
     */
    String CLIENT_ID = "member-service";
    /**
     * 会员服务第三方客户端密码(这个客户端在认证服务器配置好的oauth_client_details)
     */
    String CLIENT_SECRET = "mall";

    /**
     * 认证服务器登陆地址
     */
    String OAUTH_LOGIN_URL = "http://mall-authcenter/oauth/token";

    String USER_NAME = "username";

    String PASS = "password";

    String GRANT_TYPE = "grant_type";

    String SCOPE = "scope";

    String SCOPE_AUTH = "read";
}
