package io.github.yajuhua.podcast2.alist.dto.task.upload;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

/**
 * 获取任务信息
 */
@NoArgsConstructor
@Data
@ToString
public class InfoDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    @ToString
    public static class DataDTO {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;
        /**
         * 1是正在上传 2是上传完成
         */
        @SerializedName("state")
        private Integer state;
        @SerializedName("status")
        private String status;
        @SerializedName("progress")
        private Double progress;
        @SerializedName("error")
        private String error;
    }
}
