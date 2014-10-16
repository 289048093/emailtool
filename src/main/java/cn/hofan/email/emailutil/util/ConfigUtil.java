package cn.hofan.email.emailutil.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author lizhao  2014/9/17.
 */

public class ConfigUtil {
    private final Logger log = LoggerFactory.getLogger(cn.hofan.email.util.ConfigUtil.class);

    private Properties prop = null;


    private static final Map<String, ConfigUtil> cfgs = new HashMap<>();

    private ConfigUtil(String fileName) {
        prop = new Properties();
        InputStream is = cn.hofan.email.util.ConfigUtil.class.getClassLoader().getResourceAsStream(fileName);
        try {
            prop.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ConfigUtil instanseOf(String cfgFileName) {
        ConfigUtil res = null;
        if ((res = cfgs.get(cfgFileName)) == null) {
            synchronized (cfgs) {
                if ((res = cfgs.get(cfgFileName)) == null) {
                    res = new ConfigUtil(cfgFileName);
                    cfgs.put(cfgFileName, res);
                }
            }
        }
        return res;
    }


    public String getString(Enum key) {
        return prop.getProperty(key.toString());
    }

    public long getLong(Enum key) {
        try {
            return Long.parseLong(getString(key));
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return 0L;
    }

    public int getInt(Enum key) {
        String val = getString(key);
        if (StringUtils.isBlank(val)) {
            return 0;
        }
        try {
            return Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    public Properties getProp() {
        return prop;
    }

    public String getString(String str) {
        return prop.getProperty(str);
    }


    public int getInt(String key) {
        String val = getString(key);
        if (StringUtils.isBlank(val)) {
            return 0;
        }
        try {
            return Integer.parseInt(getString(key));
        } catch (NumberFormatException e) {
            log.error(e.getMessage(), e);
        }
        return 0;
    }

    public Map<String, String> getStringByRegex(String regex) {
        Map<String, String> res = new HashMap<>();
        for (Object key : prop.keySet()) {
            if (key.toString().matches(regex)) {
                res.put(key.toString(), getString(key.toString()));
            }
        }
        return res;
    }

    public Map<String, Integer> getIntByRegex(String regex) {
        Map<String, Integer> res = new HashMap<>();
        for (Object key : prop.keySet()) {
            if (key.toString().matches(regex)) {
                res.put(key.toString(), getInt(key.toString()));
            }
        }
        return res;
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getString(key));
    }

}
