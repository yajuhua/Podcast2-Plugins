package io.github.yajuhua.plugin.bilibili.pojo.ass;

import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dialogue{
    private Integer layer;
    private String start;
    private String end;
    private String style;
    private String name;
    private Integer marginL;
    private Integer marginR;
    private Integer marginV;
    private String effect;
    private String text;

    public String formattedString(){
        return String.format("%d,%s,%s,%s,%s," +
                        "%d,%d,%d,%s,%s",
                layer, start, end, style, name, marginL, marginR, marginV, effect, text);
    }
}
