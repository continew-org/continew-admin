package top.continew.admin.common.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class Msg implements Serializable {
    //消息id
    public String msgId;
    //发送者id
    public String fromId;
    //发送人名称
    public String fromName;
    //接受者id
    public String toId;
    //接收人名称
    public String toName;
    //消息格式.区分消息体格式.txt,json,html.markdown
    public int format;
    //消息类型:文本,图片,视频
    public int msgType;
    //聊天类型:1:单聊,2:群聊,3:系统通知
    public int chatType;
    //发送消息时间戳
    public long sendTime;
    //区分业务线
    public int businessId;
    //内容
    public String content;

}
