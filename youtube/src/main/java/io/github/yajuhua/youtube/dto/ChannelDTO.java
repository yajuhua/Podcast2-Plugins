package io.github.yajuhua.youtube.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ChannelDTO {

    @SerializedName("id")
    private String id;
    @SerializedName("channel")
    private String channel;
    @SerializedName("channel_id")
    private String channelId;
    @SerializedName("title")
    private String title;
    @SerializedName("description")
    private String description;
    @SerializedName("thumbnails")
    private List<ThumbnailsDTO> thumbnails;
    @SerializedName("uploader_id")
    private String uploaderId;
    @SerializedName("uploader_url")
    private String uploaderUrl;
    @SerializedName("uploader")
    private String uploader;
    @SerializedName("channel_url")
    private String channelUrl;
    @SerializedName("webpage_url")
    private String webpageUrl;

    @NoArgsConstructor
    @Data
    public static class ThumbnailsDTO {
        @SerializedName("url")
        private String url;
        @SerializedName("id")
        private String id;
        @SerializedName("preference")
        private Integer preference;
    }
}
