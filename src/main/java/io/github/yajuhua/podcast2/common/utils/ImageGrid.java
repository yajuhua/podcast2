package io.github.yajuhua.podcast2.common.utils;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.List;

/**
 * 九宫格生成
 */
@Slf4j
public class ImageGrid {
    private static int GRID_SIZE = 3; // 九宫格的行列数
    private static final int IMAGE_SIZE = 100; // 每张图片的大小
    private static final int ARC_RADIUS = 20; // 圆角半径
    private static final int PADDING = 10; // 图片之间的间隔
    private static final int OUTER_PADDING = 20; // 九宫格四周的间隔
    private static Map<String,BufferedImage> imageDataMap = new HashMap<>();//http图片数据重复利用

    /**
     * BufferedImage
     * @param imageUrls http图片链接
     * @return 九宫格字节数据
     */
    public static byte[] createImageGrid(List<String> imageUrls) {
        if (imageUrls.isEmpty()){
            log.error("imagesUrl为空");
            return null;
        }
        double sqrt = Math.sqrt(imageUrls.size());
        if (sqrt != (int)sqrt){
            GRID_SIZE = (int)sqrt + 1;
        }
        // 计算九宫格的大小，考虑到间隔和外部边距
        int gridWidth = GRID_SIZE * IMAGE_SIZE + (GRID_SIZE - 1) * PADDING + 2 * OUTER_PADDING;
        int gridHeight = GRID_SIZE * IMAGE_SIZE + (GRID_SIZE - 1) * PADDING + 2 * OUTER_PADDING;

        // 创建一个空的图像
        BufferedImage gridImage = new BufferedImage(gridWidth, gridHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = gridImage.createGraphics();

        // 设置背景为透明
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setColor(new Color(0, 0, 0, 0)); // 透明背景
        g2d.fillRect(0, 0, gridImage.getWidth(), gridImage.getHeight());

        // 加载图片并绘制到九宫格
        int x = OUTER_PADDING, y = OUTER_PADDING; // 外部边距
        for (int i = 0; i < imageUrls.size(); i++) {
            try {
                BufferedImage img = null;
                if (imageDataMap.containsKey(imageUrls.get(i))){
                    img = imageDataMap.get(imageUrls.get(i));
                }else {
                    img = downloadImage(imageUrls.get(i));
                }
                img = resizeAndRoundImage(img, IMAGE_SIZE, IMAGE_SIZE); // 圆角裁剪并调整大小
                g2d.drawImage(img, x, y, null);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 计算下一个图片的位置，考虑间隔
            x += IMAGE_SIZE + PADDING; // 每个图像之间增加间隔
            if ((i + 1) % GRID_SIZE == 0) {
                x = OUTER_PADDING; // 回到左边
                y += IMAGE_SIZE + PADDING; // 行之间也添加间隔
            }
        }

        // 释放资源
        g2d.dispose();

        // 将合成的图像转换为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(gridImage, "PNG", baos);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return baos.toByteArray();
    }

    /**
     从 HTTP URL 下载图片
     */
    private static BufferedImage downloadImage(String imageUrl) throws IOException {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(imageUrl);
            conn = (HttpURLConnection)url.openConnection();
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            conn.setRequestProperty("Accept", "image/webp,image/apng,image/*,*/*;q=0.8");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            try(InputStream in = conn.getInputStream();) {
                return ImageIO.read(in);
            }
        } finally {
            if (conn != null){
                conn.disconnect();
            }
        }
    }

    /**
     *  调整图片大小并应用圆角效果
     * @param img
     * @param width
     * @param height
     * @return
     */
    private static BufferedImage resizeAndRoundImage(BufferedImage img, int width, int height) {
        // 创建一个新的透明图像
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = roundedImage.createGraphics();

        // 使用抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

        // 创建圆角形状
        g2d.setClip(new RoundRectangle2D.Double(0, 0, width, height, ARC_RADIUS, ARC_RADIUS));

        // 将原始图像绘制到圆角区域内
        g2d.drawImage(img, 0, 0, width, height, null);
        g2d.dispose();

        return roundedImage;
    }
}
