package site.starsone.xtool.view.krc2lrc;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.zip.Inflater;

/**
 * @author StarsOne
 * @Date Create in  2023/05/12 01:42
 */
class Krc2LrcUtil {

    private static int key[] = {64, 71, 97, 119, 94, 50, 116, 71, 81, 54, 49, 45, 206, 210, 110, 105};

    /**
     * 是否krc格式
     *
     * @param datas
     * @return
     */
    private static boolean iskrc(byte[] datas) {
        if (datas.length < 4) {
            System.out.println("长度不够");
            return false;
        }
        if (datas[0] == 'k' && datas[1] == 'r' && datas[2] == 'c' && datas[3] == '1') {
            return true;
        }
        return false;
    }


    public static void krc2lrc(File krcPath, File lrcPath) throws Exception {
        byte[] datas = readAllBytes(krcPath);
        if (!iskrc(datas)) {
            throw new Exception("不是krc格式");
        }
        byte[] _datas = new byte[datas.length - 4];
        System.arraycopy(datas, 4, _datas, 0, datas.length - 4);
        for (int i = 0; i < _datas.length; i++) {
            _datas[i] = (byte) (_datas[i] ^ key[i % 16]);
        }
        Inflater decompresser = new Inflater();
        decompresser.setInput(_datas);
        ByteArrayOutputStream sb = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        while (!decompresser.finished()) {
            int leng = decompresser.inflate(buf);
            sb.write(buf, 0, leng);
        }
        // System.out.println("解压长度:" + sb.toByteArray().length);
        String lines[] = new String(sb.toByteArray()).split("\n");
        // System.out.println("行数:" + lines.length);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(lrcPath))) {
            for (String line : lines) {
                int i1 = line.indexOf("]");
                String timestr = line.substring(1, i1);
                String times[] = timestr.split(",");
                if (times.length == 2) {
                    int ms = Integer.parseInt(times[0]);
                    String time = String.format("[%02d:%02d.%02d]", (ms % (1000 * 60 * 60)) / (1000 * 60),
                            (ms % (1000 * 60)) / 1000, (ms % (1000 * 60)) % 100);
                    writer.write(time);
                    writer.write(line.substring(i1 + 1).replaceAll("<.*?>", ""));
                } else {
                    writer.write(line);
                }
            }
        }

    }

    /**
     * @param file
     * @return
     */
    private static byte[] readAllBytes(File file) {
        try (FileInputStream is = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            is.read(data);
            return data;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
