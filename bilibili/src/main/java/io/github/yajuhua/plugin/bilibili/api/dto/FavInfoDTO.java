package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * https://api.bilibili.com/x/v3/fav/folder/info?media_id=1208491396
 */
@NoArgsConstructor
@Data
public class FavInfoDTO {

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
        @SerializedName("id")
        private Integer id;
        @SerializedName("fid")
        private Integer fid;
        @SerializedName("mid")
        private Integer mid;
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
}
