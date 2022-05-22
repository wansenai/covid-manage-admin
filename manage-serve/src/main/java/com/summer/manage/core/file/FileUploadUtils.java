package com.summer.manage.core.file;

import com.summer.common.helper.JvmOSHelper;
import com.summer.common.helper.SnowIdHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * 文件上传工具类
 *
 * @author sacher
 */
public class FileUploadUtils {

    protected static final Logger LOG = LoggerFactory.getLogger(FileUploadUtils.class);

    /**
     * 默认上传的地址
     */
    private static final String DEFAULT_BASE_DIR = JvmOSHelper.projectDir() + "/uploadPath";

    public static String upload(String baseDir, MultipartFile multipartFile, String fileName) {
        String path = baseDir + SnowIdHelper.unique() + "-" + fileName;
        File file = new File(DEFAULT_BASE_DIR + path);
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
            FileHelper.inputStreamToFile(multipartFile.getInputStream(), file);
        } catch (Exception e) {
            LOG.error("create file is error {}", e.getMessage());
        }
        return "/file" + path;
    }
}
