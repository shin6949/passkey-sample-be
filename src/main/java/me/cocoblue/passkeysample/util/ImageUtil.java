package me.cocoblue.passkeysample.util;

import com.luciad.imageio.webp.WebPWriteParam;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import org.springframework.web.multipart.MultipartFile;

public class ImageUtil {

  // MultipartFile 처리 메서드
  public static byte[] processImage(MultipartFile file,
      int targetWidth,
      int targetHeight) throws IOException {
    BufferedImage original = ImageIO.read(file.getInputStream());
    BufferedImage resized = resizeImage(original, targetWidth, targetHeight);
    return convertToLosslessWebP(resized);
  }

  // 리사이징 메서드 (기존과 동일)
  private static BufferedImage resizeImage(BufferedImage original,
      int width, int height) {
    BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2d = resized.createGraphics();
    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
        RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2d.drawImage(original, 0, 0, width, height, null);
    g2d.dispose();
    return resized;
  }

  // WebP 변환 메서드
  private static byte[] convertToLosslessWebP(BufferedImage image) throws IOException {
    ImageWriter writer = ImageIO.getImageWritersByMIMEType("image/webp").next();
    WebPWriteParam writeParam = new WebPWriteParam(writer.getLocale());

    writeParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
    writeParam.setCompressionType(
        writeParam.getCompressionTypes()[WebPWriteParam.LOSSLESS_COMPRESSION]
    );

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    try (ImageOutputStream output = ImageIO.createImageOutputStream(baos)) {
      writer.setOutput(output);
      writer.write(null, new IIOImage(image, null, null), writeParam);
    } finally {
      writer.dispose();
    }
    return baos.toByteArray();
  }
}
