package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * https://api.bilibili.com/x/v3/fav/resource/list?media_id=1208491396&pn=1&ps=20
 */
@NoArgsConstructor
@Data
public class FavContentDTO {

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
        @SerializedName("info")
        private InfoDTO info;
        @SerializedName("medias")
        private List<MediasDTO> medias;

        @NoArgsConstructor
        @Data
        public static class InfoDTO {
            @SerializedName("id")
            private Integer id;
            @SerializedName("fid")
            private Integer fid;
            @SerializedName("mid")
            private Integer mid;
            @SerializedName("attr")
            private Integer attr;
            @SerializedName("title")
            private String title;
            @SerializedName("cover")
            private String cover;
            @SerializedName("upper")
            private UpperDTO upper;
            @SerializedName("ctime")
            private Integer ctime;
            @SerializedName("mtime")
            private Integer mtime;

            @NoArgsConstructor
            @Data
            public static class UpperDTO {
                @SerializedName("mid")
                private Integer mid;
                @SerializedName("name")
                private String name;
                @SerializedName("face")
                private String face;
                @SerializedName("followed")
                private Boolean followed;
                @SerializedName("vip_type")
                private Integer vipType;
                @SerializedName("vip_statue")
                private Integer vipStatue;
            }
        }

        @NoArgsConstructor
        @Data
        public static class MediasDTO {
            @SerializedName("id")
            private Integer id;
            @SerializedName("type")
            private Integer type;
            @SerializedName("title")
            private String title;
            @SerializedName("cover")
            private String cover;
            @SerializedName("page")
            private Integer page;
            @SerializedName("duration")
            private Integer duration;
            @SerializedName("upper")
            private UpperDTO upper;
            @SerializedName("ctime")
            private Integer ctime;
            @SerializedName("pubtime")
            private Integer pubtime;
            @SerializedName("fav_time")
            private Integer favTime;
            @SerializedName("bvid")
            private String bvid;

            @NoArgsConstructor
            @Data
            public static class UpperDTO {
                @SerializedName("mid")
                private Integer mid;
                @SerializedName("name")
                private String name;
                @SerializedName("face")
                private String face;
            }
        }
    }
}
