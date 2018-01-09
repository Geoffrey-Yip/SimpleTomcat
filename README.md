# Tomcat简易版 V3.0
## 项目介绍 
    基于《how tomcat works》(中文版:《深入剖析Tomcat》)每个章节的内容（ZAO LUN ZI）
    本版本实现了一个简单的Servlet容器
- 可以解析并相应静态html、css、图片等资源。
- 可以解析继承HttpServlet类的自定义Servlet。
- 能够解析parameter/header/cookie等信息。
- Session or Servlet init()/destory()等功能也会在以后的版本实现。

实现过程： [跟我一起动手实现Tomcat（三）:解析Request请求参数、请求头、cookie](https://juejin.im/post/5a49ca76f265da4328413499)

##体验DEMO
    1.运行Bootstrap#main()启动服务
    2.浏览器输入127.0.0.1:8080/register.html
    3.输入注册账号密码，即可看到控制台输出执行RegisterServlet的消息。
    
## 快速开始
    1. 将所需要使用的静态HTML文件放入resources/webroot/路径下
    2. 编写简单的Servlet(继承HttpServlet类)，编译成class文件后拷贝到resources/webroot/servlet/路径下
    3. 运行Bootstrap#main()启动服务
    4. 打开浏览器输入127.0.0.1:8080/{你的资源文件}即可访问静态HTML资源
    5. 打开浏览器输入127.0.0.1:8080/servlet/{你的serlvet名称}即可访问自定义Servlet
    
## 运行流程

![process](https://github.com/dnhxm/SimpleTomcat/tree/master/src/main/resources/webroot/image/version3Process.png)

## 运行环境
    - java 8
    - maven 3.0+
    - chrome


