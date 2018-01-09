package me.geoffrey.tomcat.server.enums;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Geoffrey.Yip
 * @time 2018/1/1 12:58
 * @description HTTP请求头key枚举
 */
public enum HTTPHeaderEnum {

    /*cookie*/
    COOKIE("cookie"),
    /*content-length*/
    CONTENT_LENGTH("content-length"),
    /*content-type*/
    CONTENT_TYPE("content-type");

    private String desc;

    HTTPHeaderEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public static Optional<HTTPHeaderEnum> parse(String value){
        return Stream.of(values()).filter(header->header.getDesc().equals(value)).findAny();
    }
}
