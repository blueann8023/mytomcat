package org.apache.catalina.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.Cookie;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import com.lm.mytomcat.chap3.connector.http.HttpRequest;

@Slf4j
public class RequestUtil {

    public static Cookie[] parseCookieHeader(String header) {
        if ((header == null) || (header.length() < 1)) {
            return new Cookie[0];
        }
        ArrayList cookies = new ArrayList();
        while (header.length() > 0) {
            int semicolonIndex = header.indexOf(';');
            if (semicolonIndex <= 0) {
                semicolonIndex = header.length();
            }
            if (semicolonIndex == 0) {
                break;
            }
            String tocken = header.substring(0, semicolonIndex);
            if (semicolonIndex < header.length()) {
                header = header.substring(semicolonIndex + 1);
            } else
                header = "";
            try {        
                int equalsIndex = tocken.indexOf('=');
                if (equalsIndex > 0) {
                    String name = tocken.substring(0, equalsIndex).trim();
                    String value = tocken.substring(equalsIndex + 1);
                    cookies.add(new Cookie(name, value));
                }
            } catch (Throwable e) {
                ;
            }
        }

        return (Cookie[]) cookies.toArray(new Cookie[cookies.size()]);
    }

    public static void parseParameters(ParameterMap map, String data, String encoding) throws UnsupportedEncodingException {
        log.info("parseParameters String Start");
        if (StringUtils.isNotEmpty(data)) {
            log.info("data {} ",data);
            //byte[] bytes = data.getBytes();
            int len = data.length();
            byte[] bytes = new byte[len];
            data.getBytes(0, len, bytes, 0);
            parseParameters(map, bytes, encoding);
        }
        log.info("parseParameters String End");
    }

    public static void parseParameters(Map map, byte[] data, String encoding) throws UnsupportedEncodingException  {
        log.info("parseParameters bytes Start");
        if (data != null && data.length > 0) {
            int pos = 0;
            int ix = 0;
            int ox = 0;
            String key = null, value = null;
            while (ix < data.length) {
                byte c = data[ix++];
                switch ((char) c) {
                case '&':
                    value = new String(data, 0, ox, encoding);
                    if (key != null) {
                        log.info("parseParameters key");
                        putMapEntry(map, key, value);
                        key = null;
                    }
                    ox = 0;
                    break;
                case '=':
                    key = new String(data, 0, ox, encoding);
                    ox = 0;
                    break;
                case '+':
                    data[ox++] = (byte) ' ';
                    break;
                case '%':
                    data[ox++] = (byte) ((convertHexDigit(data[ix++]) << 4) + convertHexDigit(data[ix++]));
                    break;
                default:
                    data[ox++] = c;
                }
            }
            // The last value does not end in '&'. So save it now.
            if (key != null) {
                value = new String(data, 0, ox, encoding);
                putMapEntry(map, key, value);
            }
        }
        log.info("parseParameters bytes End");
    }

    private static byte convertHexDigit(byte b) {
        if ((b >= '0') && (b <= '9'))
            return (byte) (b - '0');
        if ((b >= 'a') && (b <= 'f'))
            return (byte) (b - 'a' + 10);
        if ((b >= 'A') && (b <= 'F'))
            return (byte) (b - 'A' + 10);
        return 0;
    }

    // Put name and value pair in map. When name already exist, add value to
    // array of values.
    private static void putMapEntry(Map map, String name, String value) {
        String[] newVs = null;
        String[] oldVs = (String[]) map.get(name);
        if (oldVs == null) {
            newVs = new String[1];
            newVs[0] = value;
        } else {
            newVs = new String[oldVs.length + 1];
            System.arraycopy(oldVs, 0, newVs, 0, oldVs.length);
            newVs[oldVs.length] = value;
        }
        log.info("putMapEntry name {} = value {}",name,value);
        map.put(name, newVs);
    }
}
