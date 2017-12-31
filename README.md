# Tomcat简易版 V1.0
## 项目介绍 
    基于《how tomcat works》(中文版:《深入剖析Tomcat》)每个章节的内容（ZAO LUN ZI）
    本版本实现了一个简单的Servlet的容器
- 可以解析并相应静态html、css、图片等资源。
- 可以解析实现Servlet接口的自定义Servlet。
- 该版本只实现了Servlet的调用，Request/Response的大部分方法会在以后的版本实现。
- Cookie/Session or Servlet init()/destory()等功能也会在以后的版本实现。

实现过程： [跟我一起动手实现Tomcat（二）:实现简单的Servlet容器](https://juejin.im/post/5a487097f265da4319569d4f)
## 快速开始
    1. 将所需要使用的静态HTML文件放入resources/webroot/路径下
    2. 编写简单的Servlet(实现Servlet接口)，编译成class文件后拷贝到resources/webroot/servlet/路径下
    3. 运行HttpServer.main()方法启动服务
    4. 打开浏览器输入127.0.0.1:8080/{你的资源文件}即可访问静态HTML资源
    5. 打开浏览器输入127.0.0.1:8080/servlet/{你的serlvet名称}即可访问自定义Servlet
## 运行环境
    - java 8
    - maven 3.0+
    - chrome


