package io.github.yajuhua.podcast2.alist.dto.task.upload;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 重试任务
 */
@NoArgsConstructor
@Data
public class RetryDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private Object data;
}
