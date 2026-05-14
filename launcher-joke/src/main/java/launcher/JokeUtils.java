package launcher;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 佛祖保佑         永无BUG
 *
 * @author Created by joker on 2023/2/10
 */
public class JokeUtils {


    public static String addUrlParam(String k, Object v) {
        String value = null;
        try {
            value = URLEncoder.encode(v.toString(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        if (k != null && value != null && !k.isEmpty()) {
            return k + "=" + value;
        }
        return "";
    }
}