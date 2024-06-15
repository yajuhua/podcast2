package io.github.yajuhua.podcast2.alist.dto.fs;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class PutDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private DataDTO data;

    @NoArgsConstructor
    @Data
    public static class DataDTO {
        @SerializedName("task")
        private TaskDTO task;

        @NoArgsConstructor
        @Data
        public static class TaskDTO {
            @SerializedName("id")
            private String id;
            @SerializedName("name")
            private String name;
            @SerializedName("state")
            private Integer state;
            @SerializedName("status")
            private String status;
            @SerializedName("progress")
            private Integer progress;
            @SerializedName("error")
            private String error;
        }
    }
}
