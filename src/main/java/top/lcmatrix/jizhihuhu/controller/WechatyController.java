package top.lcmatrix.jizhihuhu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;
import jakarta.servlet.http.HttpServletRequest;
import top.lcmatrix.jizhihuhu.model.AIResponse;
import top.lcmatrix.jizhihuhu.model.WechatMessage;
import top.lcmatrix.jizhihuhu.service.ai.MessageService;

@RestController
@RequestMapping("wechaty")
public class WechatyController {

    @Autowired
    MessageService messageService;

    @PostMapping("/ai")
    public AIResponse ai(HttpServletRequest request, @RequestBody String body){
        WechatMessage wechatMessage = JSON.parseObject(body, WechatMessage.class);
        if(wechatMessage.getType() == null || (wechatMessage.getType() != 7 && wechatMessage.getType() != 8)) {
            AIResponse aiResponse = new AIResponse();
            aiResponse.setSuccess(true);
            aiResponse.setMessage("我现在还不支持这种消息类型哦");
            return aiResponse;
        }

        return messageService.aiReplyMessage(wechatMessage);
    }

}
