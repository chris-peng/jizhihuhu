package top.lcmatrix.jizhihuhu.model;

import lombok.Data;

@Data
public class RoleContent {

    public static final String ROLE_USER = "user";
    public static final String ROLE_SYSTEM = "system";
    public static final String ROLE_ASSISTANT = "assistant";

    String role;
    String content;
}
