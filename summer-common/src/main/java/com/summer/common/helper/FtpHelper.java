package com.summer.common.helper;

import com.google.common.collect.Lists;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public final class FtpHelper {
    //覆盖
    public static final int OVERWRITE = 0;
    //断点续传
    public static final int RESUME = 1;
    //追加
    public static final int APPEND = 2;
    private final static int TIME_OUT = 10000;
    private final static String FTP_TYPE = "sftp";
    private static Logger LOG = LoggerFactory.getLogger(FtpHelper.class);


    private FtpHelper() {
    }

    // 从ftp读取文件内容
    public static String readAsString(String host, int port, String username, String password, String filePath) {
        FtpStream stream = null;
        InputStream is = null;
        try {
            stream = readAsStream(host, port, username, password);
            is = stream.read(filePath);
            return BytesHelper.string(is);
        } finally {
            BytesHelper.close(is);
            if (null != stream) {
                stream.close();
            }
        }
    }

    // 从ftp读取每行数据
    public static List<String> readLines(String host, int port, String username, String password, String filePath) {
        List<String> lines = Lists.newArrayList();
        FtpStream stream = null;
        InputStream is = null;
        BufferedReader reader = null;
        String line;
        try {
            stream = readAsStream(host, port, username, password);
            is = stream.read(filePath);
            reader = new BufferedReader(new InputStreamReader(is));
            while (!StringHelper.isBlank(line = reader.readLine())) {
                lines.add(line);
            }
            return lines;
        } catch (Exception e) {
            throw new RuntimeException("ftp read lines error ", e);
        } finally {
            BytesHelper.close(is);
            if (null != stream) {
                stream.close();
            }
            BytesHelper.close(reader);
        }
    }

    /**
     * 字符串上传到FTP
     */
    public static void uploadFileAsString(String content, String host, int port, String username, String password, String filePath, int mode) {
        InputStream inputStream = new ByteArrayInputStream(BytesHelper.utf8Bytes(content));
        uploadFileAsStream(inputStream, host, port, username, password, filePath, mode);
    }

    /**
     * 上传文件到FTP
     */
    public static void uploadFile(File file, String host, int port, String username, String password, String filePath, int mode) throws Exception {
        InputStream inputStream = new FileInputStream(file);
        uploadFileAsStream(inputStream, host, port, username, password, filePath, mode);
    }

    /**
     * 通过流的形式上传文件到FTP
     */
    public static void uploadFileAsStream(InputStream is, String host, int port, String username, String password, String filePath, int mode) {
        FtpStream stream = null;
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel(FTP_TYPE);
            stream = new FtpStream(session, channel);
            if (OVERWRITE == mode) {
                stream.write(is, filePath);
            } else if (RESUME == mode) {
                stream.resume(is, filePath);
            } else if (APPEND == mode) {
                stream.append(is, filePath);
            } else {
                LOG.error("ftp unsupported upload mode...");
            }
        } catch (Exception e) {
            throw new RuntimeException("ftp file upload error ", e);
        } finally {
            if (null != stream) {
                stream.close();
            }
        }
    }

    // 从ftp读取流数据
    private static FtpStream readAsStream(String host, int port, String username, String password) {
        try {
            Session session = connect(host, port, username, password);
            ChannelSftp channel = (ChannelSftp) session.openChannel(FTP_TYPE);
            return new FtpStream(session, channel);
        } catch (Exception e) {
            throw new RuntimeException("read string from ftp error ", e);
        }
    }

    private static Session connect(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        //给出连接需要的用户名，ip地址以及端口号
        Session session = jsch.getSession(username, host, port);
        //第一次登陆时候，是否需要提示信息，value可以填写 yes，no或者是ask
        session.setConfig("StrictHostKeyChecking", "no");
        //设置是否超时
        session.setTimeout(TIME_OUT);
        //设置密码
        session.setPassword(password);
        //创建连接
        session.connect();
        return session;
    }

    private static void mkdir(String filePath, ChannelSftp channel) throws Exception {
        String dirPath = filePath.substring(0, filePath.lastIndexOf("/"));
        String[] folders = dirPath.split("/");
        for (String folder : folders) {
            if (folder.length() > 0) {
                try {
                    channel.cd(folder);
                } catch (SftpException e) {
                    channel.mkdir(folder);
                    channel.cd(folder);
                }
            }
        }
    }

    private static class FtpStream {
        final Session session;
        final ChannelSftp channel;

        private FtpStream(Session session, ChannelSftp channel) throws JSchException {
            this.session = session;
            this.channel = channel;
            channel.connect();
        }

        private void close() {
            if (channel != null) {
                channel.exit();
            }
            if (session != null) {
                session.disconnect();
            }
        }

        private InputStream read(String filePath) {
            try {
                return channel.get(filePath);
            } catch (Exception e) {
                throw new RuntimeException("read from ftp error ", e);
            }
        }

        private void write(InputStream is, String filePath) {
            try {
                mkdir(filePath, channel);
                channel.put(is, filePath, 0);
            } catch (Exception e) {
                throw new RuntimeException("write to ftp error ", e);
            }
        }

        private void resume(InputStream is, String filePath) {
            try {
                channel.put(is, filePath, 1);
            } catch (Exception e) {
                throw new RuntimeException("resume to ftp error ", e);
            }
        }

        private void append(InputStream is, String filePath) {
            try {
                channel.put(is, filePath, 2);
            } catch (Exception e) {
                throw new RuntimeException("append to ftp error ", e);
            }
        }
    }
}
