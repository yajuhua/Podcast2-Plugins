package io.github.yajuhua.plugin.bilibili.pojo.ass;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ScriptInfo {
    private String title;
    private String scriptType;
    private String playResX;
    private String playResY;
    private String aspectRatio;
    private String collisions;
    private String wrapStyle;
    private String scaledBorderAndShadow;
    private String yCbCrMatrix;
    private String comment;

    /**
     * 转换成字符串
     * @return
     */
    public String getScriptInfoToString(){
        StringBuilder sb = new StringBuilder("[Script Info]").append("\n");
        sb.append("; ").append(comment).append("\n");
        sb.append("Title: ").append(title).append("\n");
        sb.append("ScriptType: ").append(scriptType).append("\n");
        sb.append("PlayResX: ").append(playResX).append("\n");
        sb.append("PlayResY: ").append(playResY).append("\n");
        sb.append("Aspect Ratio: ").append(aspectRatio).append("\n");
        sb.append("Collisions: ").append(collisions).append("\n");
        sb.append("WrapStyle: ").append(wrapStyle).append("\n");
        sb.append("ScaledBorderAndShadow: ").append(scaledBorderAndShadow).append("\n");
        sb.append("YCbCr Matrix: ").append(yCbCrMatrix).append("\n");
        sb.append("\n\n");
        return sb.toString();
    }
}
