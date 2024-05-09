package Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class FFmpeg {
    private static final Logger LOGGER = LoggerFactory.getLogger("FFmpeg");

    /**
     * 读取视频时长
     * @param videoUrl
     * @return
     */
    public static int getDuration(String videoUrl) {
        // 构建FFmpeg命令
        String ffmpegCommand = "ffmpeg -i " + videoUrl;

        try {
            // 执行FFmpeg命令
            Process process = Runtime.getRuntime().exec(ffmpegCommand);

            // 读取命令输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // 匹配时长信息
                if (line.contains("Duration:")) {
                    // 提取时长信息
                    int index1 = line.indexOf(":");
                    int index2 = line.indexOf(",");
                    String durationString = line.substring(index1+1,index2).trim();
                    // 将时长字符串转换时分秒分开
                    int h = Integer.parseInt(durationString.split(":")[0])*3600;
                    int m = Integer.parseInt(durationString.split(":")[1])*60;
                    double s_ = Double.valueOf(durationString.split(":")[2]);
                    int s = (int)s_;
                    int seconds = h+m+s;
                    return  seconds;
                }
            }
        } catch (IOException e) {
            LOGGER.error("获取时长异常:{}",e.getMessage());
            return 10;
        }
        LOGGER.error("无法获取时长");
        return 10;
    }
}
