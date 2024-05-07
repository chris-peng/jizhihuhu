package top.lcmatrix.jizhihuhu.service.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import cn.hutool.cache.impl.TimedCache;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import top.lcmatrix.jizhihuhu.common.FixedSizeList;
import top.lcmatrix.jizhihuhu.model.AIResponse;
import top.lcmatrix.jizhihuhu.model.RoleContent;
import top.lcmatrix.jizhihuhu.model.WechatMessage;

@Service
public class MessageService {

    private static final TimedCache<String, FixedSizeList<RoleContent>> userMsgs = new TimedCache<>(60000 * 10);
    private static final TimedCache<String, FixedSizeList<RoleContent>> roomMsgs = new TimedCache<>(60000 * 10);

    @Value("${robot_name}")
    private String robotName;

    @Value("${robot_system_prompt}")
    private String robotSystemPrompt;

    @Value("${ai_service:xunfeixinghuo}")
    private String aiServiceName;

    private AIServiceInterface aiService;

    public AIResponse aiReplyMessage (WechatMessage wechatMessage){
        List<RoleContent> roleContents = new ArrayList<>();
        RoleContent systemContent = new RoleContent();
        systemContent.setContent(robotSystemPrompt);
        systemContent.setRole(RoleContent.ROLE_SYSTEM);
        roleContents.add(systemContent);

        FixedSizeList<RoleContent> histories = null;
        if(wechatMessage.getRoomId() != null) {
            histories = roomMsgs.get(wechatMessage.getRoomId());
        } else {
            histories = userMsgs.get(wechatMessage.getTalkerId());
        }
        if(histories != null) {
            roleContents.addAll(histories);
        } else {
            histories = new FixedSizeList<>(6);
        }
        RoleContent userContent = new RoleContent();
        String userMsg = wechatMessage.getText();
        if(userMsg.startsWith("@" + robotName)) {
            userMsg = userMsg.substring(robotName.length() + 1).trim();
        }
        userMsg = wechatMessage.getTalkerName() + "@你：" + userMsg;
        userContent.setContent(userMsg);
        userContent.setRole(RoleContent.ROLE_USER);
        roleContents.add(userContent);
        histories.push(userContent);

        AIResponse aiResponse = getAiService().request(roleContents);
        if(aiResponse.isSuccess() && StrUtil.isNotBlank(aiResponse.getMessage())) {
            RoleContent assistantContent = new RoleContent();
            assistantContent.setContent(aiResponse.getMessage());
            assistantContent.setRole(RoleContent.ROLE_ASSISTANT);
            histories.push(assistantContent);
            if(wechatMessage.getRoomId() != null) {
                roomMsgs.put(wechatMessage.getRoomId(), histories);
            } else {
                userMsgs.put(wechatMessage.getTalkerId(), histories);
            }
        }
        return aiResponse;
    }

    private AIServiceInterface getAiService(){
        if(aiService != null) {
            return aiService;
        }
        aiService = SpringUtil.getBean(aiServiceName + "Service");
        return aiService;
    }
}
