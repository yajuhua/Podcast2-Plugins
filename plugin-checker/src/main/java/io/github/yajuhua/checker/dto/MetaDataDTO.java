package io.github.yajuhua.checker.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MetaDataDTO {

    @SerializedName("repoVersion")
    private String repoVersion;
    @SerializedName("updateTime")
    private String updateTime;
    @SerializedName("pluginList")
    private List<PluginListDTO> pluginList;

    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class PluginListDTO {
        @SerializedName("name")
        private String name;
        @SerializedName("updateTime")
        private String updateTime;
        @SerializedName("version")
        private String version;
        @SerializedName("uuid")
        private String uuid;
        @SerializedName("url")
        private String url;
        @SerializedName("md5")
        private String md5;
    }
}
