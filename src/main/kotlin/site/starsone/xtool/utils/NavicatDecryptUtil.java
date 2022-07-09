package site.starsone.xtool.utils;

import org.apache.commons.lang3.ArrayUtils;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

/**
 * @author starsone
 * @date 2022/07/07 10:11
 */
public class NavicatDecryptUtil {

    /**
     * @param string 需要解密的字符串
     * @param type   1: 11版本 2:12版本以上
     * @return 密码
     */
    public static String decryptString(String string, int type) {
        if (type == 1) {
            return new Navicat11Cipher().decryptString(string);
        } else {
            return new Navicat12Cipher().decryptString(string);
        }
    }

    private static class Navicat12Cipher {
        private static SecretKeySpec _AesKey;
        private static IvParameterSpec _AesIV;

        static {
            _AesKey = new SecretKeySpec("libcckeylibcckey".getBytes(), "AES");
            _AesIV = new IvParameterSpec("libcciv libcciv ".getBytes());
        }

        public String encryptString(String plaintext) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.ENCRYPT_MODE, _AesKey, _AesIV);
                byte[] ret = cipher.doFinal(plaintext.getBytes());
                return DatatypeConverter.printHexBinary(ret);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        public String decryptString(String ciphertext) {
            try {
                Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                cipher.init(Cipher.DECRYPT_MODE, _AesKey, _AesIV);
                byte[] ret = cipher.doFinal(DatatypeConverter.parseHexBinary(ciphertext));
                return new String(ret);
                //return new String(ret, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

    private static class Navicat11Cipher {
        public static final String DefaultUserKey = "3DC5CA39";
        private static byte[] _IV;

        private static SecretKeySpec _Key;
        private static Cipher _Encryptor;
        private static Cipher _Decryptor;

        private static void initKey(String UserKey) {
            try {
                MessageDigest sha1 = MessageDigest.getInstance("SHA1");
                byte[] userkey_data = UserKey.getBytes();
                sha1.update(userkey_data, 0, userkey_data.length);
                _Key = new SecretKeySpec(sha1.digest(), "Blowfish");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void initChiperEncrypt() {
            try {
                // Must use NoPadding
                _Encryptor = Cipher.getInstance("Blowfish/ECB/NoPadding");
                _Encryptor.init(Cipher.ENCRYPT_MODE, _Key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void initChiperDecrypt() {
            try {
                // Must use NoPadding
                _Decryptor = Cipher.getInstance("Blowfish/ECB/NoPadding");
                _Decryptor.init(Cipher.DECRYPT_MODE, _Key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static void initIV() {
            try {
                byte[] initVec = DatatypeConverter.parseHexBinary("FFFFFFFFFFFFFFFF");
                _IV = _Encryptor.doFinal(initVec);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void xorBytes(byte[] a, byte[] b) {
            for (int i = 0; i < a.length; i++) {
                int aVal = a[i] & 0xff; // convert byte to integer
                int bVal = b[i] & 0xff;
                a[i] = (byte) (aVal ^ bVal); // xor aVal and bVal and typecast to byte
            }
        }

        private void xorBytes(byte[] a, byte[] b, int l) {
            for (int i = 0; i < l; i++) {
                int aVal = a[i] & 0xff; // convert byte to integer
                int bVal = b[i] & 0xff;
                a[i] = (byte) (aVal ^ bVal); // xor aVal and bVal and typecast to byte
            }
        }

        static {
            initKey(DefaultUserKey);
            initChiperEncrypt();
            initChiperDecrypt();
            initIV();
        }

        private byte[] Encrypt(byte[] inData) {
            try {
                byte[] CV = ArrayUtils.clone(_IV);
                //byte[] CV = Arrays.copyOf(_IV, _IV.length);
                byte[] ret = new byte[inData.length];

                int blocks_len = inData.length / 8;
                int left_len = inData.length % 8;

                for (int i = 0; i < blocks_len; i++) {
                    byte[] temp = ArrayUtils.subarray(inData, i * 8, (i * 8) + 8);
                    //byte[] temp = Arrays.copyOfRange(inData, i * 8, (i * 8) + 8);

                    xorBytes(temp, CV);
                    temp = _Encryptor.doFinal(temp);
                    xorBytes(CV, temp);

                    System.arraycopy(temp, 0, ret, i * 8, 8);
                }

                if (left_len != 0) {
                    CV = _Encryptor.doFinal(CV);
                    byte[] temp = ArrayUtils.subarray(inData, blocks_len * 8, (blocks_len * 8) + left_len);
                    //byte[] temp = Arrays.copyOfRange(inData, blocks_len * 8, (blocks_len * 8) + left_len);
                    xorBytes(temp, CV, left_len);
                    System.arraycopy(temp, 0, ret, blocks_len * 8, temp.length);
                }

                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public String encryptString(String inputString) {
            try {
                byte[] inData = inputString.getBytes();
                //byte[] inData = inputString.getBytes(StandardCharsets.UTF_8);
                byte[] outData = Encrypt(inData);
                return DatatypeConverter.printHexBinary(outData);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }

        private byte[] Decrypt(byte[] inData) {
            try {

                byte[] CV = ArrayUtils.clone(_IV);
                byte[] ret = new byte[inData.length];

                int blocks_len = inData.length / 8;
                int left_len = inData.length % 8;

                for (int i = 0; i < blocks_len; i++) {
                    byte[] temp = ArrayUtils.subarray(inData, i * 8, (i * 8) + 8);
                    //byte[] temp = Arrays.copyOfRange(inData, i * 8, (i * 8) + 8);

                    temp = _Decryptor.doFinal(temp);
                    xorBytes(temp, CV);
                    System.arraycopy(temp, 0, ret, i * 8, 8);
                    for (int j = 0; j < CV.length; j++) {
                        CV[j] = (byte) (CV[j] ^ inData[i * 8 + j]);
                    }
                }

                if (left_len != 0) {
                    CV = _Encryptor.doFinal(CV);
                    byte[] temp = ArrayUtils.subarray(inData, blocks_len * 8, (blocks_len * 8) + left_len);
                    //byte[] temp = Arrays.copyOfRange(inData, blocks_len * 8, (blocks_len * 8) + left_len);

                    xorBytes(temp, CV, left_len);
                    for (int j = 0; j < temp.length; j++) {
                        ret[blocks_len * 8 + j] = temp[j];
                    }
                }

                return ret;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public String decryptString(String hexString) {
            try {
                byte[] inData = DatatypeConverter.parseHexBinary(hexString);
                byte[] outData = Decrypt(inData);
                return new String(outData);

                //return new String(outData, StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        }
    }

}
