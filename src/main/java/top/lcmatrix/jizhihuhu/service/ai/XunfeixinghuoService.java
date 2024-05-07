package top.lcmatrix.jizhihuhu.service.ai;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.Data;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import top.lcmatrix.jizhihuhu.model.AIResponse;
import top.lcmatrix.jizhihuhu.model.RoleContent;

@Service("xunfeixinghuoService")
public class XunfeixinghuoService implements AIServiceInterface{
    private long waitResponseMilSecs = 60 * 1000;

    @Value("${xunfeixinghuo_hostUrl:}")
    private String hostUrl;

    @Value("${xunfeixinghuo_appid:}")
    private String appid;

    @Value("${xunfeixinghuo_apiSecret:}")
    private String apiSecret;

    @Value("${xunfeixinghuo_apiKey:}")
    private String apiKey;

    @Value("${xunfeixinghuo_domain:}")
    private String domain;

    @Value("${xunfeixinghuo_temperature:0.7}")
    private float temperature;

    @Value("${xunfeixinghuo_max_tokens:2048}")
    private int maxTokens;

    @Value("${xunfeixinghuo_qps:2}")
    private int qps;

    private static volatile long second = 0;
    private static AtomicInteger currentQInSecond = new AtomicInteger(0);

    private final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();

    @Override
    public AIResponse request(List<RoleContent> roleContents) {
        AIResponse aiResponse = new AIResponse();
        if(roleContents == null || roleContents.isEmpty()) {
            aiResponse.setSuccess(false);
            aiResponse.setError("没有请求内容");
            return aiResponse;
        }
        MessageHandler messageHandler = new MessageHandler(aiResponse);
        String authUrl = XunfeixinghuoService.getAuthUrl(hostUrl, apiKey, apiSecret).replace("http://", "ws://").replace("https://", "wss://");
        Request authRequest = new Request.Builder().url(authUrl).build();
        String payload = roleContentsToPayload(roleContents);
        checkQps();
        WebSocket webSocket = okHttpClient.newWebSocket(authRequest, messageHandler);
        try {
            webSocket.send(payload);
        } catch (Exception e) {
            aiResponse.setSuccess(false);
            aiResponse.setError("发送请求失败：" + e.getMessage());
            return aiResponse;
        }
        synchronized (aiResponse) {
            try {
                aiResponse.wait(waitResponseMilSecs);
            } catch (InterruptedException e) {
                aiResponse.setSuccess(false);
                aiResponse.setError("等待结果超时");
                return aiResponse;
            }
        }
        try {
            webSocket.close(1000, "");
        } catch (Exception ignored) {

        }
        return aiResponse;
    }

    private void checkQps(){
        while (true) {
            synchronized (this) {
                long currentSecond = System.currentTimeMillis() / 1000;
                if(currentSecond != second) {
                    second = currentSecond;
                    currentQInSecond.set(0);
                    break;
                }
            }
            if(currentQInSecond.get() >= qps) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } else {
                currentQInSecond.incrementAndGet();
                break;
            }
        }
    }

    private String roleContentsToPayload(List<RoleContent> roleContents){
        JSONObject requestJson=new JSONObject();

        JSONObject header=new JSONObject();  // header参数
        header.put("app_id",appid);

        JSONObject parameter=new JSONObject(); // parameter参数
        JSONObject chat=new JSONObject();
        chat.put("domain",domain);
        chat.put("temperature", temperature);
        chat.put("max_tokens", maxTokens);
        parameter.put("chat", chat);

        JSONObject payload=new JSONObject(); // payload参数
        JSONObject message=new JSONObject();
        JSONArray text=new JSONArray();

        // 历史问题获取
        for(RoleContent roleContent : roleContents){
            text.add(JSON.toJSON(roleContent));
        }

        message.put("text",text);
        payload.put("message",message);


        requestJson.put("header",header);
        requestJson.put("parameter",parameter);
        requestJson.put("payload",payload);
        return requestJson.toString();
    }

    private static class MessageHandler extends WebSocketListener {

        private final AIResponse aiResponse;

        MessageHandler(AIResponse aiResponse){
            this.aiResponse = aiResponse;
        }

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            try {
                JsonParse myJsonParse = JSON.parseObject(text, JsonParse.class);
                if (myJsonParse.header.code != 0) {
                    aiResponse.setSuccess(false);
                    aiResponse.setError("发生错误，错误码为：" + myJsonParse.header.code);
                    synchronized (aiResponse) {
                        aiResponse.notifyAll();
                    }
                    return;
                }
                aiResponse.setSuccess(true);
                List<Text> textList = myJsonParse.payload.choices.text;
                for (Text temp : textList) {
                    aiResponse.setMessage(aiResponse.getMessage() + temp.content);
                }
                if (myJsonParse.header.status == 2) {
                    synchronized (aiResponse) {
                        aiResponse.notifyAll();
                    }
                }
            } catch (Exception e) {
                aiResponse.setSuccess(false);
                aiResponse.setError("发生错误：" + e.getMessage());
                synchronized (aiResponse) {
                    aiResponse.notifyAll();
                }
            }
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            try {
                super.onFailure(webSocket, t, response);
                aiResponse.setSuccess(false);
                try {
                    if (null != response) {
                        int code = response.code();
                        aiResponse.setError("发生错误，错误码为：" + code + ", body: " + (response.body() != null ? response.body().string() : ""));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                aiResponse.setSuccess(false);
                synchronized (aiResponse) {
                    aiResponse.notifyAll();
                }
            }
            synchronized (aiResponse) {
                aiResponse.notifyAll();
            }
        }
    }


    // 鉴权方法
    public static String getAuthUrl(String hostUrl, String apiKey, String apiSecret) {
        try {
            URL url = new URL(hostUrl);
            // 时间
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            // 拼接
            String preStr = "host: " + url.getHost() + "\n" +
                    "date: " + date + "\n" +
                    "GET " + url.getPath() + " HTTP/1.1";
            // System.err.println(preStr);
            // SHA256加密
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(apiSecret.getBytes(StandardCharsets.UTF_8), "hmacsha256");
            mac.init(spec);

            byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
            // Base64加密
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            // System.err.println(sha);
            // 拼接
            String authorization = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", apiKey, "hmac-sha256", "host date request-line", sha);
            // 拼接地址
            HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().//
                    addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorization.getBytes(StandardCharsets.UTF_8))).//
                    addQueryParameter("date", date).//
                    addQueryParameter("host", url.getHost()).//
                    build();

            // System.err.println(httpUrl.toString());
            return httpUrl.toString();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    //返回的json结果拆解
    static class JsonParse {
        Header header;
        Payload payload;
    }

    @Data
    static class Header {
        int code;
        int status;
        String sid;
    }

    @Data
    static class Payload {
        Choices choices;
    }

    @Data
    static class Choices {
        List<Text> text;
    }

    @Data
    static class Text {
        String role;
        String content;
    }
}