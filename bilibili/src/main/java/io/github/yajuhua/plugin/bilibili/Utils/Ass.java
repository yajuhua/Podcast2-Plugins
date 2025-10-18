package io.github.yajuhua.plugin.bilibili.Utils;

import io.github.yajuhua.plugin.bilibili.pojo.ass.ScriptInfo;
import io.github.yajuhua.plugin.bilibili.pojo.DanmakuInfo;
import io.github.yajuhua.plugin.bilibili.pojo.ass.Dialogue;
import io.github.yajuhua.plugin.bilibili.pojo.ass.Events;
import io.github.yajuhua.plugin.bilibili.pojo.ass.V4plusStyles;


import java.util.ArrayList;
import java.util.List;

/**
 * ASS字幕格式
 */
public class Ass {

    /**
     * 构建ass字幕格式内容
     * @param scriptInfo
     * @param v4plusStyles
     * @param events
     * @return
     */
    public static String build(ScriptInfo scriptInfo, V4plusStyles v4plusStyles, Events events){
        StringBuilder sb = new StringBuilder();
        sb.append(scriptInfo.getScriptInfoToString()).append("\n");
        sb.append(v4plusStyles.getV4plusStylesToString()).append("\n");
        sb.append(events.getEventsToString()).append("\n");
        return sb.toString();
    }

    /**
     * 将秒数转换成 0:01:15.38 这种格式
     * @param seconds
     * @return
     */
    public static String formatSeconds(double seconds) {
        // 计算小时、分钟和秒数部分
        int hours = (int) (seconds / 3600);
        int minutes = (int) ((seconds % 3600) / 60);
        double remainingSeconds = seconds % 60;

        // 格式化输出，确保分钟和秒数部分以两位数字显示
        return String.format("%d:%02d:%05.2f", hours, minutes, remainingSeconds);
    }


    /**
     * DEC（RGB888）to HEX（RGB888）
     * @param decimalRGB
     * @return
     */
    public static String decimalToHex(int decimalRGB) {
        // 将十进制整数分离为红色、绿色和蓝色通道的值
        int red = (decimalRGB >> 16) & 0xFF;
        int green = (decimalRGB >> 8) & 0xFF;
        int blue = decimalRGB & 0xFF;

        // 使用Java的字符串格式化功能将RGB值转换为十六进制表示
        return String.format("%02x%02x%02x", red, green, blue);
    }


    /**
     * 构建ASS字幕格式内容
     * @param title 标题
     * @param width 屏幕宽
     * @param height 屏幕高
     * @param duration 弹幕在屏幕持续时间
     * @param rowHeightPx 行高
     * @param fontSize 字号
     * @param danmakuInfoList 弹幕信息集合
     * @param danmakuWidthMultiplier 弹幕宽度乘子（1-2）
     * @param displayArea 显示区域(0-1)
     * @param shadow 阴影(0-4)
     * @return
     */
    public static String build(String title, Integer width, Integer height, Integer duration,Integer rowHeightPx,Integer fontSize,
                               List<DanmakuInfo> danmakuInfoList,float danmakuWidthMultiplier,float displayArea,float shadow){
        ScriptInfo scriptInfo = ScriptInfo.builder()
                .title(title)
                .scriptType("v4.00+")
                .playResX(width.toString())
                .playResY(height.toString())
                .aspectRatio(width + ":" + height)
                .collisions("Normal")
                .wrapStyle("2")
                .scaledBorderAndShadow("yes")
                .yCbCrMatrix("TV.601")
                .comment("参考danmu2ass")
                .build();

        List<String> styles = new ArrayList<>();
        styles.add("Float,黑体,"+ fontSize +",&H4dFFFFFF,&H00FFFFFF,&H4d000000,&H00000000,1, 0, 0, 0, 100, 100, 0.00, 0.00, 1, "+ shadow +", 0, 7, 0, 0, 0, 1");
        styles.add("Bottom,黑体,"+ fontSize +",&H4dFFFFFF,&H00FFFFFF,&H4d000000,&H00000000,1, 0, 0, 0, 100, 100, 0.00, 0.00, 1, "+ shadow +", 0, 7, 0, 0, 0, 1");
        styles.add("Top,黑体,"+ fontSize +",&H4dFFFFFF,&H00FFFFFF,&H4d000000,&H00000000,1, 0, 0, 0, 100, 100, 0.00, 0.00, 1, "+ shadow +", 0, 7, 0, 0, 0, 1");

        V4plusStyles v4plusStyles = V4plusStyles.builder()
                .format("Name, Fontname, Fontsize, PrimaryColour, SecondaryColour, OutlineColour, BackColour, Bold, " +
                        "Italic, Underline, StrikeOut, ScaleX, ScaleY, Spacing, Angle, BorderStyle, Outline, Shadow, " +
                        "Alignment, MarginL, MarginR, MarginV, Encoding")
                .styles(styles)
                .build();

        List<Dialogue> dialogueList = new ArrayList<>();
        Integer y1 = 0;//纵坐标初始
        Integer y2;//纵坐标初始
        Integer x1 = width;
        Integer x2 = 0;
        for (int i = 0; i < danmakuInfoList.size(); i++) {

            DanmakuInfo info = danmakuInfoList.get(i);
            y2 = y1;
            int strLen = info.getContent().length();
            x2 = (int)(strLen * fontSize * danmakuWidthMultiplier);

            Dialogue dialogue = Dialogue.builder()
                    .layer(2)
                    .start(formatSeconds(info.getAvailabilityTime()))
                    .end(formatSeconds(info.getAvailabilityTime() + duration))
                    .style("Float")
                    .name("")
                    .marginL(0)
                    .marginR(0)
                    .marginV(0)
                    .effect("")
                    .text("{\\move("+ x1 +", "+ y1 +", -"+ x2 +", "+ y2 +")\\c&H" + decimalToHex(Integer.parseInt(info.getColor())) + "&}" + info.getContent())
                    .build();
            dialogueList.add(dialogue);
            y1 = y2 + rowHeightPx;
            if (y1 > height*displayArea){
                y1 = 0;
            }
        }


        Events events = Events.builder()
                .format("Layer, Start, End, Style, Name, MarginL, MarginR, MarginV, Effect, Text")
                .dialogueList(dialogueList)
                .build();

        return build(scriptInfo,v4plusStyles,events);

    }

}
