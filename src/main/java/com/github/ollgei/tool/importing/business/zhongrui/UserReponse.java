package com.github.ollgei.tool.importing.business.zhongrui;

import java.util.List;

import lombok.Data;

/**
 * desc.
 * @author zjw
 * @since 1.0
 */
@Data
final class UserReponse {
    private Integer status;
    private List<UserData> data;
    private String message;
    private String allMessages;
    private Integer rowsCount;
    private Boolean success;

    @Data
    public static class UserData {
        private String id;
        private String loginName;
        private String name;
    }
}
