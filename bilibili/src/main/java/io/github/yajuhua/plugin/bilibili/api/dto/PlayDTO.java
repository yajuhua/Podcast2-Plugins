package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class PlayDTO{

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
        @SerializedName("message")
        private String message;
        @SerializedName("dash")
        private DashDTO dash;

        @NoArgsConstructor
        @Data
        public static class DashDTO {
            @SerializedName("duration")
            private Integer duration;
            @SerializedName("video")
            private List<VideoDTO> video;
            @SerializedName("audio")
            private List<AudioDTO> audio;

            @NoArgsConstructor
            @Data
            public static class VideoDTO {
                @SerializedName("id")
                private Integer id;
                @SerializedName("baseUrl")
                private String baseUrl;
                @SerializedName("mimeType")
                private String mimeType;
                @SerializedName("codecs")
                private String codecs;
                @SerializedName("width")
                private Integer width;
                @SerializedName("height")
                private Integer height;
                @SerializedName("codecid")
                private Integer codecid;
            }

            @NoArgsConstructor
            @Data
            public static class AudioDTO {
                @SerializedName("id")
                private Integer id;
                @SerializedName("baseUrl")
                private String baseUrl;
                @SerializedName("mimeType")
                private String mimeType;
                @SerializedName("codecs")
                private String codecs;
                @SerializedName("width")
                private Integer width;
                @SerializedName("height")
                private Integer height;
                @SerializedName("codecid")
                private Integer codecid;
            }
        }
    }
}