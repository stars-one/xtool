package site.starsone.xtool.view.lpk;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import cn.hutool.core.util.ArrayUtil;
import kotlin.Pair;
import site.starsone.xtool.view.lpk.mime.MimeMatcher;
import site.starsone.xtool.view.lpk.mime.MimeMoc;
import site.starsone.xtool.view.lpk.mime.MimeMoc3;
import site.starsone.xtool.view.lpk.mime.MimeTypes;


@SuppressWarnings("unchecked")
public class LpkUtils {

    static final Pattern match_rule = Pattern.compile("^[0-9a-f]{32}.bin3?$");

    public static boolean isEncryptedFile(String s) {
        return match_rule.matcher(s).find();
    }

    public static long genKey(String s) {
        long ret = 0;
        for (int i = 0; i < s.length(); i++) {
            ret = (ret * 31 + s.charAt(i)) & 0xffffffffL;
        }
        if ((ret & 0x80000000L) != 0) {
            ret = ret | 0xffffffff00000000L;
        }
        return ret;
    }

    public static byte[] decrypt(long key, byte[] data) {
        ArrayList<Byte> list = new ArrayList<>();
        for (int i = 0; i < data.length; i += 1024) {
            int end = Math.min(i + 1024, data.length);
            byte[] slice = ArrayUtil.sub(data,i,end);
            long tmpkey = key;
            for (byte b : slice) {
                tmpkey = (65535L & 2531011 + 214013L * tmpkey >> 16) & 0xffffffffL;
                list.add((byte) ((tmpkey & 0xff) ^ b));
            }
        }
        int size = list.size();
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) {
            bytes[i] = list.get(i);
        }
        return bytes;
    }

    public static List<Pair<String, Object>> travelsDict(Map<String, ?> map) {
        List<Pair<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, ?> entry : map.entrySet()) {
            Object o = entry.getValue();
            //根据数值分类
            if (o instanceof Map) {
                for (Pair i : travelsDict((Map<String, ?>) o)) {
                    items.add(new Pair(String.format("%s_%s", entry.getKey(), i.getFirst()), i.getSecond()));
                }
            } else if (o instanceof List) {
                for (Pair i : travelsList((List<?>) o)) {
                    items.add(new Pair(String.format("%s_%s", entry.getKey(), i.getFirst()), i.getSecond()));
                }
            } else {
                items.add(new Pair(entry.getKey(), o));
            }
        }
        return items;
    }

    public static List<Pair<String, Object>> travelsList(List<?> list) {
        List<Pair<String, Object>> items = new ArrayList<Pair<String, Object>>();
        for (int i = 0; i < list.size(); i++) {
            Object o = list.get(i);
            if (o instanceof Map) {
                for (Pair res : travelsDict((Map<String, ?>) o)) {
                    items.add(new Pair(String.format("%d_%s", i, res.getFirst()), res.getSecond()));
                }
            } else if (o instanceof List) {
                for (Pair res : travelsList((List<?>) o)) {
                    items.add(new Pair(String.format("%d_%s", i, res.getFirst()), res.getSecond()));
                }
            } else {
                items.add(new Pair(String.valueOf(i), o));
            }
        }
        return items;
    }

    // MIME
    static final List<MimeMatcher> customMimeTypes = new ArrayList<>();
    static final Tika TIKA = new Tika();

    static {
        customMimeTypes.add(new MimeMoc());
        customMimeTypes.add(new MimeMoc3());
    }

    public static String guessType(byte[] data) {
        // 使用自定义的类型匹配器
        for (MimeMatcher matcher : customMimeTypes) {
            if (matcher.match(data)) {
                return "." + matcher.getExt();
            }
        }
        // 使用 tika 检查内容类型
        String mime = TIKA.detect(data);
        if (StringUtils.isNoneEmpty(mime) && !"text/plain".equals(mime)) {
            // https://stackoverflow.com/questions/48053127/get-the-extension-from-a-mimetype
            String ext = MimeTypes.lookupExt(mime);
            if (StringUtils.isNoneEmpty(ext)) {
                return "." + ext;
            }
        }

        try {
            //尝试转换为json格式
            String str = new String(data);
            JsonElement parse = new JsonParser().parse(str);
            return ".json";
        } catch (Exception ignored) {
            //如果转换失败，则返回空白字符串
            return "";
        }
    }

}
