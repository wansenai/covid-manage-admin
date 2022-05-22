package com.summer.manage.core.file;

import com.summer.common.helper.BytesHelper;
import com.summer.common.helper.StringHelper;
import org.jgroups.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @Description：
 * @Author：sacher
 * @Create：2019/9/5 23:31
 **/
public class FileHelper {

    protected static final Logger LOG = LoggerFactory.getLogger(FileHelper.class);

    public static File multipartFileToFile(MultipartFile file) {
        File toFile = null;
        InputStream ins = null;
        try {
            if (file.getSize() <= 0) {
                file = null;
            } else {
                ins = file.getInputStream();
                toFile = new File(StringHelper.defaultString(file.getOriginalFilename()));
                inputStreamToFile(ins, toFile);
            }
        } catch (Exception e) {
            LOG.error("multipartFileToFile is error ,the message {}", e.getMessage());
        } finally {
            BytesHelper.close(ins);
        }
        return toFile;
    }

    public static File urlToFile(String name, InputStream ins) {
        File toFile = new File(name);
        try {
            inputStreamToFile(ins, toFile);
        } catch (Exception e) {
            LOG.error("urlToFile is error ,the message {}", e.getMessage());
        } finally {
            BytesHelper.close(ins);
        }
        return toFile;
    }


    public static File bytesToFile(byte[] fileB, String suffix) {
        String fileName = UUID.randomUUID().toString();
        FileOutputStream fos = null;
        File file = null;
        try {
            file = File.createTempFile(fileName, suffix);
            fos = new FileOutputStream(file);
            fos.write(fileB);
            fos.flush();
        } catch (Exception e) {
            LOG.error("bytesToFile is error ,the message {}", e.getMessage());
        } finally {
            BytesHelper.close(fos);
        }
        return file;
    }

    public static void inputStreamToFile(InputStream ins, File file) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            int bytesRead;
            byte[] buffer = new byte[ins.available()];
            while ((bytesRead = ins.read(buffer, 0, ins.available())) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            LOG.error("inputStreamToFile is error ,the message {}", e.getMessage());
        } finally {
            BytesHelper.close(os);
            BytesHelper.close(ins);
        }
    }

}
