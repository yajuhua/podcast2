package io.github.yajuhua.podcast2.alist.dto.task.upload;

import com.google.gson.annotations.SerializedName;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 清除已成功任务
 */
@NoArgsConstructor
@Data
public class ClearSucceededDTO {

    @SerializedName("code")
    private Integer code;
    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private Object data;
}
