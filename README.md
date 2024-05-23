# 机智糊糊，连接你的微信和 AI

生成代码和聊天助手可以算是 ChatGPT 这样的AI最擅长的事，正如最好用的代码助手都是IDE的插件，智能聊天助手也应该呆在聊天软件里面。
### 花半个小时，机智糊糊可以把你的微信连接上 AI，变成一个上知天文下知地理的智能聊天助手，不管是单聊有问题随时问他，还是群聊辅助讨论，都要比另外打开网页或者APP要方便太多
![image](https://github.com/chris-peng/jizhihuhu/blob/master/doc/jzhhpreview%20-big-original.gif?raw=true)

## 如何安装
### 1. 运行环境
* 机智糊糊目前支持以 docker 方式运行，只要你的电脑/服务器支持 docker 和 docker-compose 就没问题

### 2. 安装前准备
* 需要一个微信号，作为机器人，连接 AI 进行自动回复。虽然目前在使用中还没有被封的情况，但还是建议使用不常用的微信号，比如可以[注册一个微信小号](https://www.36kr.com/p/2117021873817988)(需要实名认证并绑定一张银行卡，否则可能无法登录)
* 需要ChatGPT 或者 讯飞星火的 API 凭证，用来调用 AI 的 api，生成回复。目前只支持 ChatGPT(或其他兼容OpenAI 接口协议的大模型，比如 nvidia llama3) 和 讯飞星火，目前两者都有给新用户赠送一定的免费额度。可以访问其官网注册。
    * 注册 ChatGPT 及获取token，网上资料很多，可以参考这篇文章 https://blog.csdn.net/qq_51447436/article/details/134624252 ， ChatGPT 官网：https://openai.com （需要科学上网）
    * 注册 Nvidia, https://build.nvidia.com/explore/discover?signin=true , 然后点击代码示例上方的 Get API Key 即可
    * 注册讯飞星火， https://xinghuo.xfyun.cn/sparkapi ，注册后需要认证才能获得免费额度，然后创建app 以得到 appId，apiKey 和 apiSecret
 
### 3. 安装
* 安装 docker 和 docker-compose
     * windows， 安装 docker-desktop 即可， 参考文档 https://docs.docker.com/desktop/install/windows-install/
     * macOS，也是安装 docker-desktop 即可， 参考文档 https://docs.docker.com/desktop/install/mac-install/
     * linux，需要分别安装 docker 和 docker-compose
 
* 下载[机智糊糊最新版本](https://github.com/chris-peng/jizhihuhu/releases/latest)，解压之后，根据需要修改配置文件 `robot.config`:
  
      robot_name=机智糊糊       # 机器人的微信名
      robot_system_prompt=你是一个开源机器人（https://github.com/chris-peng/jizhihuhu），名字叫机智糊糊，是一个聪明可爱的AI助手，当有人@你的时候，请回答他。        # 给机器人的系统提示，可以告诉机器人自己叫什么，职责是什么等
      introduce=大家好，我是机智糊糊，是一个智能机器人助手，虽然我有时会犯迷糊，但大多数时候我都机智的一匹！[得意]有什么问题可以随时@我哦。[害羞][害羞]       # 被邀进群自我介绍
      admin=管理员微信名                                                  # 管理员的微信名，管理员可以通过给机器人发送“开”或“关”控制机器人是否开启自动回复
      test_user=微信团队                                              # 用来维持登录(否则长时间无对话可能会被强制下线)，机器人登录后，将每隔一天自动向该联系人发送一个 "hello, 机智糊糊"
      
      ai_service=xunfeixinghuo                                          # 选择ai模型，目前只支持讯飞星火xunfeixinghuo和chatgpt
      
      # 以下只需要填写选择的ai模型对应的配置项
      # xunfeixinghuo
      xunfeixinghuo_domain=generalv3.5                                  # 讯飞星火模型版本，默认为v3.5模型，除非你要用其他版本的模型，否则不需要修改
      xunfeixinghuo_hostUrl=https://spark-api.xf-yun.com/v3.5/chat      # 讯飞星火api地址，默认为v3.5模型的地址，除非你要用其他版本的模型，否则不需要修改
      xunfeixinghuo_appid=<your app id>                                 # 讯飞星火提供的appid，需要在讯飞开放平台注册并创建应用后获取
      xunfeixinghuo_apiSecret=<your api secret>                         # 讯飞星火提供的apiSecret，需要在讯飞开放平台注册并创建应用后获取
      xunfeixinghuo_apiKey=<your api key>                               # 讯飞星火提供的apiKey，需要在讯飞开放平台注册并创建应用后获取
      xunfeixinghuo_temperature=0.5                                     # 机器人回复的温度，范围是0～1，数值越大表示回复越具有不确定性
      xunfeixinghuo_max_tokens=2048                                     # 机器人每次回复的最大token数
      xunfeixinghuo_qps=2                                               # 每秒最多允许多少次回复，多出的回复将排队延迟发送，注意不要超过讯飞星火允许的 qps
      
      # chatgpt
      chatgpt_api_base=https://api.openai.com/v1                        # chatgpt 接口地址，默认为openai官方地址，也可以用其他兼容openai接口的地址，比如nvidia llama3的https://integrate.api.nvidia.com/v1
      chatgpt_token=<your api key>                                      # chatgpt 接口（或兼容接口）的 api key，需要在 openai 官网申请， ai_service 为chatgpt时，必填
      chatgpt_model=gpt-3.5-turbo                                       # chatgpt（或其他兼容接口）的 模型
      chatgpt_temperature=0.5                                           # 机器人回复的温度，范围是0～1，数值越大表示回复越具有不确定性
      chatgpt_max_tokens=2048                                           # 机器人每次回复的最大token数
  
* 使用终端或命令行进入到 docker 目录，运行以下命令启动所有服务

      docker-compose up -d

然后执行命令查看 wechaty 的启动日志，待其展示出二维码后，使用微信扫码登录：

      docker-compose logs -f wechaty

然后可以尽情享用。

其他有用的命令：

      docker-compose down   # 停止服务。当修改配置文件后，需要停止然后再启动服务使其生效，重启后若之前已成功登录微信，则不需要再次登录
      docker-compose logs -f aiservice   # 查看 ai 服务的日志
      
  
