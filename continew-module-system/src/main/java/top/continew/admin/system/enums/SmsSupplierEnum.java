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

package top.continew.admin.system.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.dromara.sms4j.comm.constant.SupplierConstant;
import top.continew.starter.core.enums.BaseEnum;

/**
 * 菜单类型枚举
 *
 * @author Charles7c
 * @since 2023/2/15 20:12
 */
@Getter
@RequiredArgsConstructor
public enum SmsSupplierEnum implements BaseEnum<String> {

    ALIBABA(SupplierConstant.ALIBABA, "阿里云"), CLOOPEN(SupplierConstant.CLOOPEN, "容联云"),
    CTYUN(SupplierConstant.CTYUN, "天翼云"),
    //    EMAY(SupplierConstant.EMAY, "亿美软通"), HUAWEI(SupplierConstant.HUAWEI, "华为云短信"),
    //    JDCLOUD(SupplierConstant.JDCLOUD, "京东云短信"), NETEASE(SupplierConstant.NETEASE, "网易云信"),
    //    TENCENT(SupplierConstant.TENCENT, "腾讯云短信"), UNISMS(SupplierConstant.UNISMS, "合一短信"),
    //    YUNPIAN(SupplierConstant.YUNPIAN, "云片短信"), ZHUTONG(SupplierConstant.ZHUTONG, "助通短信"),
    //    LIANLU(SupplierConstant.LIANLU, "联麓短信"), DINGZHONG(SupplierConstant.DINGZHONG, "鼎众短信"),
    //    QINIU(SupplierConstant.QINIU, "七牛云短信"), CHUANGLAN(SupplierConstant.CHUANGLAN, "创蓝短信"),
    //    JIGUANG(SupplierConstant.JIGUANG, "极光短信"), BUDING_V2(SupplierConstant.BUDING_V2, "布丁云V2"),
    //    MAS(SupplierConstant.MAS, "中国移动 云MAS"), BAIDU(SupplierConstant.BAIDU, "百度云短信"),
    //    LUO_SI_MAO(SupplierConstant.LUO_SI_MAO, "螺丝帽短信"), MY_SUBMAIL(SupplierConstant.MY_SUBMAIL, "SUBMAIL短信"),
    //    DAN_MI(SupplierConstant.DAN_MI, "单米短信"), YIXINTONG(SupplierConstant.YIXINTONG, "亿信通"),
    ;

    private final String value;
    private final String description;
}
