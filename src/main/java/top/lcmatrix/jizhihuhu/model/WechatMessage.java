package top.lcmatrix.jizhihuhu.model;

import lombok.Data;

@Data
public class WechatMessage {
    private Integer type;
    private String text;
    private String talkerName;
    private String talkerId;
    private String toName;
    private String toId;
    private String roomId;
    private boolean isSelf;
    private boolean mentionSelf;
}
