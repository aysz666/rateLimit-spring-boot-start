package com.yuisole.storagebase;

import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Yuisole
 */
public interface Storage {
    /**
     * 获取访问次数
     * @param key 键
     * @return 次数
     */
    Integer getRequestCount(String key);

    /**
     * 清空访问记录
     * @param key 键
     */
    void removeRequestCount(String key);


    /**
     * 设置冷静时间
     * @param key 键
     * @param cooldown 冷静的时间
     */
    void setCooldown(String key, long cooldown);

    /**
     * 判断是否在冷静的时间段
     * @param key 键
     * @return 冷静时间
     */
    long getCooldown(String key);

    /**
     * 过滤早于时间窗口的请求
     * @param key 键
     * @param timestamp 窗口大小
     */
    void filterRequestsBefore(String key, long timestamp);

    /**
     * 增加计数
     * @param key 键
     */
    void incrementRequestCount(String key);

    /**
     * 保存图像验证码
     * @param key 键
     * @param text 验证码
     */
    void saveImageCode(String key,String text,long timestamp);

    /**
     * 校验图片验证码
     * @param key 键
     * @param uuid 图片uuid
     * @param text 验证码
     * @return 返回是否校验成功
     */
    boolean validateCaptcha(String key,String uuid,String text);

    /**
     * 设置图片验证码
     * @param key 键
     * @return map对象，包括key和imagCode（base64编码）
     * @throws IOException io异常
     */
    default Map<String,String> setImageCode(String key,long timestamp) throws IOException {
        // 创建kaptcha对象
        DefaultKaptcha kaptcha = new DefaultKaptcha();

        // 设置kaptcha属性
        Properties properties = new Properties();
        properties.setProperty("kaptcha.border", "no");
        properties.setProperty("kaptcha.textproducer.font.color", "black");
        properties.setProperty("kaptcha.image.width", "150");
        properties.setProperty("kaptcha.image.height", "50");
        kaptcha.setConfig(new Config(properties));

        // 生成验证码
        String text = kaptcha.createText();
        BufferedImage image = kaptcha.createImage(text);

        // 将验证码图片转换成base64编码的文本格式
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        String base64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
        Map<String,String> result = new HashMap<>();
        String code = "data:image/jpeg;base64,"+base64Image;

        result.put("imageCode",code);

        Random random = new Random();
        // 生成1000到9999之间的随机数
        int randomNumber = random.nextInt(9000) + 1000;
        result.put("uuid", String.valueOf(randomNumber));

        saveImageCode(key+":"+randomNumber,text,timestamp);

        return result;
    }
}
