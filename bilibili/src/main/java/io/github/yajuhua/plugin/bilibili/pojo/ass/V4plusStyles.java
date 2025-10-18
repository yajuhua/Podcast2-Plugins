package io.github.yajuhua.plugin.bilibili.pojo.ass;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class V4plusStyles {
    private String format;
    private List<String> styles;

    public String getV4plusStylesToString(){
        StringBuilder sb = new StringBuilder("[V4+ Styles]").append("\n");
        sb.append("Format: ").append(format).append("\n");
        for (String style : styles) {
            sb.append("Style: ").append(style).append("\n");
        }
        sb.append("\n\n");
        return sb.toString();
    }
}


