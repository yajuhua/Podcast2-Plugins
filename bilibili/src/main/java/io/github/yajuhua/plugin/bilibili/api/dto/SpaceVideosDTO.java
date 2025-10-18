package io.github.yajuhua.plugin.bilibili.api.dto;


import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * https://app.bilibili.com/x/v2/space/archive/cursor?vmid=3493116233386366
 */
@NoArgsConstructor
@Data
public class SpaceVideosDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("ttl")
    private Integer ttl;
    @SerializedName("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("count")
        private Integer count;
        @SerializedName("item")
        private List<ItemDTO> item;

        @NoArgsConstructor
        @Data
        public static class ItemDTO {
            @SerializedName("title")
            private String title;
            @SerializedName("cover")
            private String cover;
            @SerializedName("duration")
            private Integer duration;
            @SerializedName("danmaku")
            private Integer danmaku;
            @SerializedName("ctime")
            private Integer ctime;
            @SerializedName("author")
            private String author;
            @SerializedName("bvid")
            private String bvid;
        }
    }
}
