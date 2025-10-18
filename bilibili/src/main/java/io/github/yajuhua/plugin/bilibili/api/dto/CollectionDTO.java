package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * https://api.bilibili.com/x/polymer/web-space/seasons_archives_list
 */
@NoArgsConstructor
@Data
public class CollectionDTO {

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
        @SerializedName("archives")
        private List<ArchivesDTO> archives;
        @SerializedName("meta")
        private MetaDTO meta;

        @NoArgsConstructor
        @Data
        public static class MetaDTO {
            @SerializedName("cover")
            private String cover;
            @SerializedName("description")
            private String description;
            @SerializedName("mid")
            private Integer mid;
            @SerializedName("name")
            private String name;
            @SerializedName("ptime")
            private Integer ptime;
            @SerializedName("season_id")
            private Integer seasonId;
            @SerializedName("total")
            private Integer total;
        }

        @NoArgsConstructor
        @Data
        public static class ArchivesDTO {
            @SerializedName("aid")
            private Integer aid;
            @SerializedName("bvid")
            private String bvid;
            @SerializedName("ctime")
            private Integer ctime;
            @SerializedName("duration")
            private Integer duration;
            @SerializedName("pic")
            private String pic;
            @SerializedName("pubdate")
            private Integer pubdate;
            @SerializedName("state")
            private Integer state;
            @SerializedName("title")
            private String title;
        }
    }
}
