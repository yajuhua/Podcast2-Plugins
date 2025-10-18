package io.github.yajuhua.plugin.bilibili.api.dto;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * https://api.bilibili.com/x/web-interface/card?mid=3493116233386366
 */
@NoArgsConstructor
@Data
public class UpCardDTO {

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
        @SerializedName("card")
        private CardDTO card;

        @NoArgsConstructor
        @Data
        public static class CardDTO {
            @SerializedName("name")
            private String name;
            @SerializedName("face")
            private String face;
            @SerializedName("description")
            private String description;
        }
    }
}
