package site.starsone.xtool.view.ncm2mp3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;


/**
 * @author charlottexiao
 */
public class Core {

    /**
     * NCM格式头部读取
     * 功能:MagicHeader读取
     *
     * @param inputStream ncm文件输入流
     */
    public static void appendMagicHeader(FileInputStream inputStream) throws Exception {
        byte[] bytes = new byte[10];
        inputStream.read(bytes, 0, 10);
    }

    /**
     * 获取CR4密钥
     * 功能:将用AES128加密的CR4密钥进行解密
     *
     * @param inputStream ncm文件输入流
     * @return CR4密钥
     */
    public static byte[] cr4Key(FileInputStream inputStream) throws Exception {
        byte[] bytes = new byte[4];
        inputStream.read(bytes, 0, 4);
        int len = Utils.getLength(bytes);
        bytes = new byte[len];
        inputStream.read(bytes, 0, len);
        //1.按字节对0x64异或
        for (int i = 0; i < len; i++) {
            bytes[i] ^= 0x64;
        }
        //2.AES解密(其中PKCS5Padding填充模式会去除末尾填充部分)
        bytes = AES.decrypt(bytes, AES.CORE_KEY, AES.TRANSFORMATION, AES.ALGORITHM);
        //3.去除前面`neteasecloudmusic`的17个字节
        byte[] key = new byte[bytes.length - 17];
        System.arraycopy(bytes, 17, key, 0, key.length);
        return key;
    }

    /**
     * 获取Mata头部信息
     * 功能:获取音乐的Mata头部信息,以JSON格式表示
     *
     * @param inputStream ncm文件输入流
     * @return JSON格式头部信息
     */
    public static String mataData(FileInputStream inputStream) throws Exception {
        byte[] bytes = new byte[4];
        inputStream.read(bytes, 0, 4);
        int len = Utils.getLength(bytes);
        bytes = new byte[len];
        inputStream.read(bytes, 0, len);
        //跳过:CRC(4字节),unused Gap(5字节)
        inputStream.skip(9);
        //1.按字节对0x63异或
        for (int i = 0; i < len; i++) {
            bytes[i] ^= 0x63;
        }
        //2.去除前面`163 key(Don't modify):`22个字节
        byte[] temp = new byte[bytes.length - 22];
        System.arraycopy(bytes, 22, temp, 0, temp.length);
        //3.Base64进行decode转码

        temp = cn.hutool.core.codec.Base64.decode(temp);

        //4.AES解密(其中PKCS5Padding填充模式会去除末尾填充部分)
        temp = AES.decrypt(temp, AES.MATA_KEY, AES.TRANSFORMATION, AES.ALGORITHM);
        //5.去除前面`music:`6个字节后获得JSON
        return new String(temp, 6, temp.length - 6, Charset.forName("utf-8"));
    }

    /**
     * 专辑图片
     * 功能:获取专辑图片数据
     *
     * @param inputStream ncm文件输入流
     * @return 专辑图片数据
     */
    public static byte[] getAlbumImage(FileInputStream inputStream) throws Exception {
        byte[] bytes = new byte[4];
        inputStream.read(bytes, 0, 4);
        int len = Utils.getLength(bytes);
        byte[] imageData = new byte[len];
        inputStream.read(imageData, 0, len);
        return imageData;
    }

    /**
     * 音乐数据
     * 功能:获取音乐数据
     *
     * @param inputStream  ncm文件输入流
     * @param outputStream 存音乐数据的文件输出流
     * @param cr4Key       CR4密钥
     */
    public static void musicData(FileInputStream inputStream, FileOutputStream outputStream, byte[] cr4Key) throws Exception {
        CR4 cr4 = new CR4();
        cr4.KSA(cr4Key);
        byte[] buffer = new byte[0x8000];
        for (int len; (len = inputStream.read(buffer)) > 0; ) {
            cr4.PRGA(buffer, len);
            outputStream.write(buffer, 0, len);
        }
        inputStream.close();
        outputStream.close();
    }
}
