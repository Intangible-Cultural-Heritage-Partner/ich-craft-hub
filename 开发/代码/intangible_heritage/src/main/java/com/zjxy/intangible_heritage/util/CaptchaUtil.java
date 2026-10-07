package com.zjxy.intangible_heritage.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class CaptchaUtil {

    private static final int WIDTH = 110;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    // 去掉易混淆字符 0/O/1/I/L
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private static final Random RANDOM = new Random();

    /**
     * 生成验证码图片，并将验证码文本写入 session。
     *
     * @param outputStream 响应输出流
     * @return 验证码文本
     */
    public static String generate(OutputStream outputStream) throws IOException {
        String code = randomCode();
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 干扰线
        for (int i = 0; i < 6; i++) {
            g.setColor(randomColor(120, 200));
            g.drawLine(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT),
                    RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT));
        }

        // 验证码字符
        g.setFont(new Font("Arial", Font.BOLD, 26));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(randomColor(20, 130));
            g.drawString(String.valueOf(code.charAt(i)), 18 + i * 22, 30 + RANDOM.nextInt(6) - 3);
        }

        // 干扰点
        for (int i = 0; i < 40; i++) {
            g.setColor(randomColor(100, 220));
            g.fillOval(RANDOM.nextInt(WIDTH), RANDOM.nextInt(HEIGHT), 2, 2);
        }

        g.dispose();
        ImageIO.write(image, "png", outputStream);
        return code;
    }

    private static String randomCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    private static Color randomColor(int min, int max) {
        int r = min + RANDOM.nextInt(max - min);
        int g = min + RANDOM.nextInt(max - min);
        int b = min + RANDOM.nextInt(max - min);
        return new Color(r, g, b);
    }
}