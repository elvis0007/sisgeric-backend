package ec.com.eeasa.wssisrec.wssisrec.shared.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ObjectEncryptor {
  private static final Logger logger = LoggerFactory.getLogger(ObjectEncryptor.class);
  @Value("${jwt.secret}")
  private String SECRET_KEY;

  public String decrypt(String encrypted) {
    try {
      SecretKeySpec key = new SecretKeySpec(SECRET_KEY.substring(0, 16).getBytes("UTF-8"), "AES");
      IvParameterSpec iv = new IvParameterSpec(SECRET_KEY.substring(0, 16).getBytes("UTF-8"));

      Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING"); // Asegúrate de que es PKCS5PADDING
      cipher.init(Cipher.DECRYPT_MODE, key, iv);

      byte[] decodeEncryptedData = Base64.decodeBase64(encrypted);
      byte[] original = cipher.doFinal(decodeEncryptedData);

      return new String(original);
    } catch (Exception e) {
      logger.error("Error al desencriptar el objeto: " + e.getMessage());
      return null;
    }
  }
}