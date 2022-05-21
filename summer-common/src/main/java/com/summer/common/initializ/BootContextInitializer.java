package com.summer.common.initializ;

import com.alibaba.fastjson.serializer.DoubleSerializer;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.summer.common.helper.NetworkHelper;
import com.summer.common.helper.SpringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Properties;

public class BootContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static final Logger LOG = LoggerFactory.getLogger(BootContextInitializer.class);
    private static final String ACTIVE_CONF = "active-conf";

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        // Set ApplicationContext
        new SpringHelper(){}.setApplicationContext(context);
        String[] envList = context.getEnvironment().getActiveProfiles();
        if (null == envList || envList.length < 1) {
            // loading config from others
            loadingOthersConf(context.getEnvironment().getProperty("boot." + ACTIVE_CONF), context);
        }
    }

    // active-conf       file:   http:   https:
    private static final String P_FILE = "file:", P_HTTP = "http:", P_HTTPS = "https:";
    private void loadingOthersConf(String confF, ConfigurableApplicationContext context) {
        MutablePropertySources mps = context.getEnvironment().getPropertySources();
        if (mps.contains("applicationConfig: [classpath:/application.properties]")) {
            Properties properties = new Properties();
            if (isValid(confF)) {
                InputStream is = null;
                try {
                    if (confF.startsWith(P_FILE)) {
                        String path = confF.substring(P_FILE.length());
                        try {
                            is = new FileInputStream(path);
                        } catch (Exception e) {
                            is = new ClassPathResource(path).getInputStream();
                        }
                    } else if (confF.startsWith(P_HTTP) || confF.startsWith(P_HTTPS)) {
                        String urlF = toHex(confF.getBytes(StandardCharsets.UTF_8));
                        try {
                            is = new URL(confF).openStream();
                            try {
                                File cacheD = new File("/tmp/conf/");
                                if (!cacheD.exists()) { cacheD.mkdirs(); }
                                File targetF = new File((cacheD.getPath() + "/" + urlF));
                                if (!targetF.exists()) { targetF.createNewFile(); }
                                FileOutputStream urlFOS = new FileOutputStream(targetF);
                                byte[] buffer = new byte[is.available()]; is.read(buffer);
                                urlFOS.write(buffer);  urlFOS.close();
                            } catch (Exception e) {
                                LOG.error("cache conf={} error ", confF, e);
                            }
                        } catch (Exception e) {
                            LOG.warn("Loading active-conf={} error ", confF, e);
                        }
                        try {
                            is = new FileInputStream(("/tmp/conf/" + urlF));
                        } catch (Exception e) {
                            LOG.error("Loading active-conf from local cache error ", e);
                        }
                    } else {
                        LOG.warn("Unknown active-conf={}", confF);
                    }
                    if (null != is) {
                        properties.load(is);
                    } else {
                        LOG.error("Notfound active-conf from confF={} ", confF); System.exit(110);
                    }
                } catch (IOException e) {
                    LOG.error("Loading active-conf={} error ", confF, e); System.exit(110);
                } finally {
                    String confD = properties.toString().replace("{", "").replace("}", "\n").replace(", ", "\n");
                    LOG.info("load boot.active-conf from {}, conf details \n\n{}", confF, confD);
                    if (null != is) { try { is.close(); } catch (Exception e) { LOG.warn("CLOSE active-conf={} stream error ", confF, e);}}
                }
                properties.put("eureka.instance.metadata-map.conf", confF);
            } else {
                properties.put("eureka.instance.metadata-map.conf", SpringHelper.applicationEnv());
            }
            mps.addFirst(new PropertiesPropertySource(ACTIVE_CONF, properties));
        }
    }
    private static boolean isValid(String src) {
        return !(null == src || src.trim().length() < 1) && (src.startsWith(P_FILE) || src.startsWith(P_HTTP) || src.startsWith(P_HTTPS));
    }

    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    @SuppressWarnings("DuplicatedCode")
    private static String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < chars.length; i = i + 2) {
            byte b = bytes[i / 2];
            chars[i] = HEX_DIGITS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_DIGITS[b & 0xf];
        }
        return new String(chars);
    }

    static {
        // setting user country language
        System.setProperty("user.country", Locale.CHINESE.getCountry());
        System.setProperty("user.language", Locale.CHINESE.getLanguage());

        // jgroups cluster bind address ipv4
        System.setProperty("jgroups.bind_addr", NetworkHelper.machineIP());
        System.setProperty("java.net.preferIPv4Stack", String.valueOf(true));
        System.setProperty("java.net.preferIPv4Addresses", String.valueOf(true));

        // 统一设置默认文件上传限制100MB, 单个文件 100M, 多文件总上传的数据 100M
        System.setProperty("spring.servlet.multipart.max-file-size", "100MB");
        System.setProperty("spring.servlet.multipart.max-request-size", "100MB");

//        SerializeConfig.getGlobalInstance().put(Long.TYPE , ToStringSerializer.instance);
//        SerializeConfig.getGlobalInstance().put(long.class , ToStringSerializer.instance);
//        SerializeConfig.getGlobalInstance().put(Long.class , ToStringSerializer.instance);
        SerializeConfig.getGlobalInstance().put(Double.TYPE, new DoubleSerializer("#.######"));
        SerializeConfig.getGlobalInstance().put(double.class, new DoubleSerializer("#.######"));
        SerializeConfig.getGlobalInstance().put(Double.class, new DoubleSerializer("#.######"));
    }
}
