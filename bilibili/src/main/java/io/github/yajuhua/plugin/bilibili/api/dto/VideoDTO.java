package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class VideoDTO {

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
        @SerializedName("bvid")
        private String bvid;
        @SerializedName("aid")
        private Long aid;
        @SerializedName("videos")
        private Integer videos;
        @SerializedName("tid")
        private Integer tid;
        @SerializedName("pic")
        private String pic;
        @SerializedName("title")
        private String title;
        @SerializedName("pubdate")
        private Integer pubdate;
        @SerializedName("ctime")
        private Integer ctime;
        @SerializedName("desc")
        private String desc;
        @SerializedName("duration")
        private Integer duration;
        @SerializedName("owner")
        private OwnerDTO owner;
        @SerializedName("pages")
        private List<PagesDTO> pages;

        @NoArgsConstructor
        @Data
        public static class OwnerDTO {
            @SerializedName("mid")
            private Integer mid;
            @SerializedName("name")
            private String name;
            @SerializedName("face")
            private String face;
        }

        @NoArgsConstructor
        @Data
        public static class PagesDTO {
            @SerializedName("cid")
            private Long cid;
            @SerializedName("page")
            private Integer page;
            @SerializedName("from")
            private String from;
            @SerializedName("part")
            private String part;
            @SerializedName("duration")
            private Integer duration;
            @SerializedName("vid")
            private String vid;
            @SerializedName("weblink")
            private String weblink;
            @SerializedName("dimension")
            private DimensionDTO dimension;
            @SerializedName("first_frame")
            private String firstFrame;

            @NoArgsConstructor
            @Data
            public static class DimensionDTO {
                @SerializedName("width")
                private Integer width;
                @SerializedName("height")
                private Integer height;
                @SerializedName("rotate")
                private Integer rotate;
            }
        }
    }
}
