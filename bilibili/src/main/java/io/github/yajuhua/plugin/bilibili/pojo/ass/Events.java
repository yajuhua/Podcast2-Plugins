package io.github.yajuhua.plugin.bilibili.pojo.ass;

import lombok.Builder;

import java.util.ArrayList;
import java.util.List;

@Builder
public class Events {
    private  String format;
    private List<Dialogue> dialogueList = new ArrayList<>();

    public String getEventsToString(){
        StringBuilder sb  = new StringBuilder("[Events]").append("\n");
        sb.append("Format: ").append(format).append("\n");
        for (Dialogue dialogue : dialogueList) {
            sb.append("Dialogue: ").append(dialogue.formattedString()).append("\n");
        }
        return sb.toString();
    }
}
