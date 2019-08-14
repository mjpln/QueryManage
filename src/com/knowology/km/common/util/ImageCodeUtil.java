package com.knowology.km.common.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;

public class ImageCodeUtil {
//	
//	static{
//        System.setProperty("java.awt.headless", "true");
//    }
	public static Map<String, BufferedImage> createImage() {
		
		
		// 颜色列表，用于验证码、噪线、噪点
		Color[] color = { Color.BLACK, Color.RED, Color.BLUE, Color.GREEN,
				Color.ORANGE, Color.PINK, Color.CYAN, Color.DARK_GRAY,
				Color.GRAY, Color.LIGHT_GRAY, Color.MAGENTA };
		// 字体列表，用于验证码
		String[] font = { "Times New Roman", "MS Mincho", "Book Antiqua",
				"Gungsuh", "PMingLiU", "Impact", "Georgia", "Verdana", "Arial",
				"Tahoma", "Courier New", "Arial Black", "Quantzite" };
		// 验证码的字符集，去掉了一些容易混淆的字符
		char[] character = { '2', '3', '4', '5', '6', '8', '9', 'A', 'B', 'C',
				'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'R',
				'S', 'T', 'W', 'X', 'Y', 'a', 'b', 'c', 'd', 'e', 'f', 'g',
				'h', 'j', 'k', 'm', 'n', 'p', 'r', 's', 't', 'w', 'x', 'y' };
		Random rnd = new Random();
		StringBuffer sb = new StringBuffer();
		BufferedImage image = new BufferedImage(100, 40,
				BufferedImage.TYPE_INT_RGB);
		Graphics graphic = image.getGraphics();
		graphic.setColor(Color.WHITE);
		graphic.fillRect(0, 0, 100, 40);
		// 画随机字符
		for (int i = 0; i < 4; i++) {
			graphic.setColor(color[rnd.nextInt(color.length)]);
			// 设置字体大小
			String fnt = font[rnd.nextInt(font.length)];
			Font ft = new Font(fnt, Font.PLAIN, 22);
			graphic.setFont(ft);
			String chkCode = String.valueOf(character[rnd
					.nextInt(character.length)]);
			graphic.drawString(chkCode + "", i * 20 + 10, 27);
			sb.append(chkCode);
		}

		Map<String, BufferedImage> map = new HashMap<String, BufferedImage>();
		map.put(sb.toString(), image);
		return map;
	}

	public static InputStream getInputStream(BufferedImage image)
			throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(bos);
		encoder.encode(image);
		byte[] imageBts = bos.toByteArray();
		InputStream in = new ByteArrayInputStream(imageBts);
		return in;
	}
}
