# 机智糊糊，连接你的微信和 AI

生成代码和聊天助手可以算是 ChatGPT 这样的AI最擅长的事，正如最好用的代码助手都是IDE的插件，智能聊天助手也应该呆在聊天软件里面。
### 花半个小时，机智糊糊可以把你的微信连接上 AI，变成一个上知天文下知地理的智能聊天助手，不管是单聊有问题随时问他，还是群聊辅助讨论，都要比另外打开网页或者APP要方便太多
![image](https://github.com/chris-peng/jizhihuhu/blob/master/doc/jzhhpreview%20-big-original.gif?raw=true)

## 如何安装
### 1. 运行环境
* 机智糊糊目前只支持以 docker 方式运行，所以只要你的电脑/服务器支持 docker 和 docker-compose 就没问题

### 2. 安装前准备
* 需要一个微信号，作为机器人，连接 AI 进行自动回复。虽然目前我在使用中还没有被封的情况，但还是建议使用不常用的微信号，比如可以[注册一个微信小号](https://www.36kr.com/p/2117021873817988)
* 需要ChatGPT 或者 讯飞星火的 API 凭证，用来调用 AI 的 api，生成回复。目前只支持 ChatGPT(或其他兼容OpenAI 接口协议的大模型，比如 nvidia llama3) 和 讯飞星火，目前两者都有给新用户赠送一定的免费额度。可以访问其官网注册。
    * 注册 ChatGPT 及获取token，网上资料很多，可以参考这篇文章 https://blog.csdn.net/qq_51447436/article/details/134624252 ， ChatGPT 官网：https://openai.com （需要科学上网）
    * 注册 Nvidia, https://build.nvidia.com/explore/discover?signin=true , 然后点击代码示例上方的 Get API Key 即可
    * 注册讯飞星火， https://xinghuo.xfyun.cn/sparkapi ，注册后需要认证才能获得免费额度，然后创建app 以得到 appId，apiKey 和 apiSecret
 
### 3. 安装
