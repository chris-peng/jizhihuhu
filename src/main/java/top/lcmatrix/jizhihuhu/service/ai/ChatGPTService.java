package top.lcmatrix.jizhihuhu.service.ai;

import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import top.lcmatrix.jizhihuhu.model.AIResponse;
import top.lcmatrix.jizhihuhu.model.RoleContent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("chatgptService")
public class ChatGPTService implements AIServiceInterface{

    @Value("${chatgpt_token:}")
    private String token;

    @Value("${chatgpt_model:}")
    private String model;

    @Value("${chatgpt_temperature:0.7}")
    private float temperature;

    @Value("${chatgpt_max_tokens:2048}")
    private int maxTokens;

    @Value("${chatgpt_api_base:https://api.openai.com/v1}")
    private String apiBase;


    @Override
    public AIResponse request(List<RoleContent> roleContents) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("messages", roleContents);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", maxTokens);
        requestBody.put("top_p", 1);
        requestBody.put("frequency_penalty", 0);
        requestBody.put("presence_penalty", 0);
        HttpResponse response = null;
        AIResponse aiResponse = new AIResponse();
        try {
            response = HttpUtil.createPost(apiBase + "/chat/completions")
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + token)
                    .body(JSONUtil.toJsonStr(requestBody))
                    .execute();
        } catch (Exception e) {
            aiResponse.setSuccess(false);
            aiResponse.setError(e.getMessage());
            return aiResponse;
        }
        String body = response.body();
        if(response.isOk()){
            JSONObject entries = JSONUtil.parseObj(body);
            JSONArray choices = entries.getJSONArray("choices");
            if(choices == null || choices.isEmpty()){
                aiResponse.setSuccess(false);
                return aiResponse;
            }
            String text = ((JSONObject) choices.get(0)).getJSONObject("message").getStr("content");
            if(text == null){
                aiResponse.setSuccess(false);
                return aiResponse;
            }
            aiResponse.setSuccess(true);
            aiResponse.setMessage(text.trim());
            return aiResponse;
        } else {
            aiResponse.setSuccess(false);
            aiResponse.setError("请求OpenAI失败，status: " + response.getStatus() + ", body: " + body);
            return aiResponse;
        }
    }
}
