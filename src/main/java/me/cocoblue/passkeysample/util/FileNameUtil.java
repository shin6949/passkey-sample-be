package me.cocoblue.passkeysample.util;

public class FileNameUtil {

  /**
   * 파일 이름에서 확장자 추출
   * @param fileName 파일 이름
   * @return 파일 확장자
   */
  public static String getExtension(final String fileName) {
    if (fileName == null) return "";

    // 1. 파일 경로에서 순수 파일명 추출
    final int lastSeparator = Math.max(
        fileName.lastIndexOf('/'),
        fileName.lastIndexOf('\\')
    );
    final String pureFileName = (lastSeparator == -1)
        ? fileName
        : fileName.substring(lastSeparator + 1);

    // 2. 확장자 분리
    int dotIndex = pureFileName.lastIndexOf('.');
    return (dotIndex > 0)
        ? pureFileName.substring(dotIndex + 1)
        : "";
  }
}
