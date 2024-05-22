/*
 * Copyright (c) 2022-present Charles7c Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.continew.admin.common.util;

import top.continew.admin.common.enums.MessageTypeEnum;
import top.continew.admin.common.model.dto.WsMsg;
import top.continew.admin.common.util.helper.LoginHelper;

public class NoticeMsgUtils {
    public static WsMsg conversion(String massage, String id) {
        WsMsg msg = new WsMsg();
        msg.setMsgId(id);
        msg.setFromId(LoginHelper.getUserId().toString());
        msg.setFromName(LoginHelper.getUsername());
        msg.setContent(massage);
        msg.setMsgType(MessageTypeEnum.SYSTEM.getValue());
        msg.setSendTime(System.currentTimeMillis());
        return msg;
    }
}
