package me.cocoblue.passkeysample.security;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.spec.RSAPublicKeySpec;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class PemFileLoader {

  public static PrivateKey loadPrivateKey(String resourcePath) throws Exception {
    Resource resource = new ClassPathResource(resourcePath.replace("classpath:", ""));
    try (InputStream inputStream = resource.getInputStream();
        Reader reader = new InputStreamReader(inputStream);
        PEMParser pemParser = new PEMParser(reader)) {

      Object object = pemParser.readObject();

      if (object instanceof PrivateKeyInfo) {
        return new JcaPEMKeyConverter().getPrivateKey((PrivateKeyInfo) object);
      } else if (object instanceof org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo) {
        throw new UnsupportedOperationException("암호화된 개인 키는 지원하지 않습니다");
      } else {
        throw new IllegalArgumentException("지원되지 않는 PEM 형식: " + object.getClass());
      }
    }
  }

  public static PublicKey extractPublicKeyFromPrivateKey(String privateKeyPath) throws Exception {
    PrivateKey privateKey = loadPrivateKey(privateKeyPath);
    if (privateKey instanceof RSAPrivateCrtKey rsaKey) {
      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(
          rsaKey.getModulus(),
          rsaKey.getPublicExponent()
      );
      return KeyFactory.getInstance("RSA").generatePublic(publicKeySpec);
    }
    throw new IllegalArgumentException("RSA CRT 키가 아닙니다.");
  }

}
