package io.github.yajuhua.plugin.bilibili.pojo;

import lombok.*;

/**
 * 每条弹幕信息封装
 * <d p="27.17900,1,25,16777215,1720749846,0,6f5df845,1624526474856110848,10">删减了</d>
 * //27.17900, 1, 25, 16777215, 1720749846, 0, 6f5df845, 1624526474856110848, 10
 * API文档 https://socialsisteryi.github.io/bilibili-API-collect/docs/danmaku/danmaku_xml.html#xml%E6%A0%BC%E5%BC%8F%E7%BB%93%E6%9E%84
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class DanmakuInfo {
    private Float availabilityTime;//出现时间
    private Integer danmakuType;//弹幕类型
    private Integer fontSize;//字号
    private String color; //十进制RGB888值
    private Long sendTime;//发送时间
    private Integer danmakuPoolType;//弹幕池类型
    private String senderMidHash;//发送者mid的HASH
    private String danmakuDmid;//弹幕dmid
    private Integer shieldingLevel;//弹幕屏蔽等级
    private String content;//弹幕内容
}
