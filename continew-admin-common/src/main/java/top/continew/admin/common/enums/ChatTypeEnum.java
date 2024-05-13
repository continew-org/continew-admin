package top.continew.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by WeiRan on  2022.09.20 17:43
 */
@Getter
@AllArgsConstructor
public enum ChatTypeEnum {
    CHAT_TYPE_UNKNOWN(0, "未知"),

    CHAT_TYPE_SINGLE(1, "单聊"),

    CHAT_TYPE_GROUP(2, "群聊"),

    CHAT_TYPE_NOTICE(3, "系统通知");

    private final int code;
    private final String desc;

    public static ChatTypeEnum getInstance(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code).findFirst().orElse(null);
    }
}