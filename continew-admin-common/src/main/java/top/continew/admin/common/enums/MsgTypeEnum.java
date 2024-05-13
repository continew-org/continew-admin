package top.continew.admin.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

/**
 * Created by WeiRan on  2022.09.20 17:46
 */
@Getter
@AllArgsConstructor
public enum MsgTypeEnum {
    MSG_TYPE_UNKNOWN(0, "未知"),
    MSG_TYPE_TEXT(1, "通知"),
    MSG_TYPE_IMG(2, "活动");

    private final int code;
    private final String desc;

    public static MsgTypeEnum getInstance(int code) {
        return Arrays.stream(values()).filter(e -> e.code == code).findFirst().orElse(null);
    }
}
