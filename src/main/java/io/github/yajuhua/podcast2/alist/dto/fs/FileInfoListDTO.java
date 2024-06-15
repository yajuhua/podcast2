package io.github.yajuhua.podcast2.alist.dto.fs;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 列出文件目录
 */
@NoArgsConstructor
@Data
public class FileInfoListDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("content")
        private List<ContentDTO> content;
        @SerializedName("total")
        private Integer total;
        @SerializedName("readme")
        private String readme;
        @SerializedName("header")
        private String header;
        @SerializedName("write")
        private Boolean write;
        @SerializedName("provider")
        private String provider;

        @NoArgsConstructor
        @Data
        public static class ContentDTO {
            @SerializedName("name")
            private String name;
            @SerializedName("size")
            private Integer size;
            @SerializedName("is_dir")
            private Boolean isDir;
            @SerializedName("modified")
            private String modified;
            @SerializedName("created")
            private String created;
            @SerializedName("sign")
            private String sign;
            @SerializedName("thumb")
            private String thumb;
            @SerializedName("type")
            private Integer type;
            @SerializedName("hashinfo")
            private String hashinfo;
            @SerializedName("hash_info")
            private Object hashInfo;
        }
    }
}
