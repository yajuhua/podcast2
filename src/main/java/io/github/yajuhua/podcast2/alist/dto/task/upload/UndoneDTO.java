package io.github.yajuhua.podcast2.alist.dto.task.upload;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 获取未完成任务
 */
@NoArgsConstructor
@Data
public class UndoneDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<DataDTO> data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("id")
        private String id;
        @SerializedName("name")
        private String name;
        @SerializedName("state")
        private String state;
        @SerializedName("status")
        private String status;
        @SerializedName("progress")
        private Integer progress;
        @SerializedName("error")
        private String error;
    }
}
