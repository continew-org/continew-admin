package top.continew.admin.common.util;

import top.continew.admin.common.enums.ChatTypeEnum;
import top.continew.admin.common.enums.MsgTypeEnum;
import top.continew.admin.common.model.dto.Msg;
import top.continew.admin.common.util.helper.LoginHelper;

public class NoticeMsgUtils {
    public static Msg conversion(String massage, String id){
        Msg msg = new Msg();
        msg.setMsgId(id);
        msg.setFromId( LoginHelper.getUserId().toString());
        msg.setFromName(LoginHelper.getUsername());
        msg.setContent(massage);
        msg.setMsgType(MsgTypeEnum.MSG_TYPE_TEXT.getCode());
        msg.setChatType(ChatTypeEnum.CHAT_TYPE_NOTICE.getCode());
        msg.setSendTime(System.currentTimeMillis());
        return msg;
    }
}
