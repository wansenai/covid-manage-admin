package com.summer.common.helper;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.summer.common.core.RpcException;
import com.summer.common.support.CommonCode;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.DigestInputStream;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;

/**
 * 加密功能
 **/
public final class EncryptHelper {
    private static final String SALT = "2_~!@=?=#$++%^&*:(121)";
    private static final JwtAlgorithm JWT = new JwtAlgorithm();
    private static final AesAlgorithm AES = new AesAlgorithm(SALT);
    private static final DesAlgorithm DES = new DesAlgorithm(SALT);
    private static final char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static MessageDigest SHA1;
    private static MessageDigest MD5;

    static {
        try {
            SHA1 = MessageDigest.getInstance("sha1");
            MD5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            throw new RuntimeException("no md5 algorithm error...", e);
        }
    }

    /**
     * SHA1 混淆加密
     */
    public static String cryptogram(String val) {
        if (StringHelper.isBlank(val)) {
            return val;
        }
        synchronized (SHA1) {
            return toHex(SHA1.digest(BytesHelper.utf8Bytes(val + SALT)));
        }
    }

    /**
     * SHA1 加密
     **/
    public static String sha1(String val) {
        if (StringHelper.isBlank(val)) {
            return val;
        }
        synchronized (SHA1) {
            return toHex(SHA1.digest(BytesHelper.utf8Bytes(val)));
        }
    }

    /**
     * MD5 加密
     **/
    public static String md5(String val) {
        if (StringHelper.isBlank(val)) {
            return val;
        }
        return md5(BytesHelper.utf8Bytes(val));
    }

    /**
     * MD5 加密
     **/
    public static String md5(byte[] bytes) {
        if (null == bytes || bytes.length < 1) {
            return StringHelper.EMPTY;
        }
        synchronized (MD5) {
            return toHex(MD5.digest(bytes));
        }
    }

    /**
     * MD5 加密
     **/
    public static String md5(File file) {
        FileInputStream fis = null;
        DigestInputStream dis = null;
        try {
            fis = new FileInputStream(file);
            synchronized (MD5) {
                dis = new DigestInputStream(fis, MD5);
                byte[] buffer = new byte[256 * 1024];
                while (dis.read(buffer) > 0) ;
                return toHex(dis.getMessageDigest().digest());
            }
        } catch (Exception e) {
            throw new RuntimeException("File " + file.getName() + " encrypt MD5 error... ", e);
        } finally {
            BytesHelper.close(dis);
            BytesHelper.close(fis);
        }
    }

    /**
     * AES加密字符串
     **/
    public static String encryptAES(String strIn) {
        try {
            return toHex(AES.encrypt(BytesHelper.utf8Bytes(strIn)));
        } catch (Exception e) {
            throw new RuntimeException("AES algorithm encrypt error.....", e);
        }
    }

    /**
     * AES解密字符串
     **/
    public static String decryptAES(String strIn) {
        try {
            return new String(AES.decrypt(hex2Bytes(strIn)));
        } catch (Exception e) {
            throw new RuntimeException("AES algorithm decrypt error.....", e);
        }
    }

    /**
     * 3DES加密字符串
     **/
    public static String encrypt3DES(String strIn) {
        try {
            return toHex(DES.encrypt(strIn.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("3DES algorithm encrypt error.....", e);
        }
    }

    /**
     * 3DES解密字符串
     **/
    public static String decrypt3DES(String strIn) {
        try {
            return new String(DES.decrypt(hex2Bytes(strIn)));
        } catch (Exception e) {
            throw new RuntimeException("3DES algorithm decrypt error.....", e);
        }
    }

    /**
     * 签名字段， tid, uid, ext, expireTime
     **/
    public static String signatureJWT(final String tid, final String uid, final String ext, long expire) {
        return JWT.signature(tid, uid, ext, expire);
    }

    public static List<String> verifyJWT(String signature, boolean checkExpire) {
        return JWT.verify(signature, checkExpire);
    }

    /**
     * 验证授权按签名字段顺序返回
     **/
    public static List<String> verifyJWT(String signature) {
        return JWT.verify(signature, true);
    }

    /**
     * Base64 Encode
     **/
    public static String encode64(String val) {
        if (StringHelper.isBlank(val)) {
            return val;
        }
        return BytesHelper.string(Base64.getEncoder().encode(BytesHelper.utf8Bytes(val)));
    }

    public static String encode64(byte[] bytes) {
        if (null == bytes) {
            return StringHelper.EMPTY;
        }
        return BytesHelper.string(Base64.getEncoder().encode(bytes));
    }

    /**
     * Base64 decode byte[]
     **/
    public static byte[] decode64Bytes(String val) {
        val = StringHelper.defaultString(val).replaceAll("\\s+", "");
        if (StringHelper.isBlank(val)) {
            return new byte[0];
        }
        try {
            return Base64.getDecoder().decode(BytesHelper.utf8Bytes(val));
        } catch (Exception e) {
            throw new RuntimeException("decode base64 error...", e);
        }
    }

    /**
     * Base64 decode string
     **/
    public static String decode64String(String val) {
        return BytesHelper.string(decode64Bytes(val));
    }

    /**
     * Base64 decode InputStream
     **/
    public static InputStream decode64Stream(String val) {
        return new ByteArrayInputStream(decode64Bytes(val));
    }

    /**
     * 字符串转为 16 进制字符串
     **/
    public static String toHex(String src) {
        if (StringHelper.isBlank(src)) {
            return src;
        }
        return toHex(BytesHelper.utf8Bytes(src));
    }

    /**
     * 字节数组转 16 进制字符串
     **/
    public static String toHex(byte[] bytes) {
        char[] rs = new char[bytes.length * 2];
        for (int i = 0; i < rs.length; i = i + 2) {
            byte b = bytes[i / 2];
            rs[i] = HEX_DIGITS[(b >>> 0x4) & 0xf];
            rs[i + 1] = HEX_DIGITS[b & 0xf];
        }
        return new String(rs);
    }

    /**
     * 16 进制字符串反解
     **/
    public static String hex2String(String src) {
        if (StringHelper.isBlank(src)) {
            return src;
        }
        return BytesHelper.string(hex2Bytes(src));
    }

    /**
     * 16 进制字符串字节数组
     **/
    public static byte[] hex2Bytes(String src) {
        byte[] res = new byte[src.length() / 2];
        char[] chs = src.toCharArray();
        for (int i = 0, c = 0; i < chs.length; i += 2, c++) {
            res[c] = (byte) (Integer.parseInt(new String(chs, i, 2), 16));
        }
        return res;
    }

    private static byte[] bytes(byte[] bytes, Cipher cipher, int times) throws IllegalBlockSizeException, BadPaddingException {
        byte[] b = bytes;
        for (int i = 0; i < times; i++) {
            b = cipher.doFinal(b);
        }
        return b;
    }

    private static Key createKey(String secret, int bits, String spec) {
        byte[] bs = new byte[bits];
        byte[] bytes = BytesHelper.utf8Bytes(secret);
        for (int i = 0; i < bytes.length && i < bs.length; i++) {
            bs[i] = bytes[i];
        }
        return new SecretKeySpec(bs, spec);
    }

    private static Pair<Cipher, Cipher> initialize(String secret, String cipher, int bits, String spec) throws Exception {
        Key key = createKey(secret, bits, spec);
        Cipher encrypt = Cipher.getInstance(cipher);
        encrypt.init(Cipher.ENCRYPT_MODE, key);
        Cipher decrypt = Cipher.getInstance(cipher);
        decrypt.init(Cipher.DECRYPT_MODE, key);
        return new Pair<>(encrypt, decrypt);
    }

    private static final class AesAlgorithm {
        private static final int AES_TIMES = 1, BITS = 16;
        private static final String CIPHER = "AES/ECB/PKCS5Padding", SPEC = "AES";
        private final Cipher encryptCipher, decryptCipher;

        //指定密钥构造方法
        private AesAlgorithm(String secret) {
            try {
                Pair<Cipher, Cipher> pair = initialize(secret, CIPHER, BITS, SPEC);
                this.encryptCipher = pair.getKey();
                this.decryptCipher = pair.getValue();
            } catch (Exception e) {
                throw new RuntimeException("new AES algorithm error.....", e);
            }
        }

        //加密字节数组
        private byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
            return bytes(bytes, encryptCipher, AES_TIMES);
        }

        //解密字节数组
        private byte[] decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
            return bytes(bytes, decryptCipher, AES_TIMES);
        }
    }

    private static final class DesAlgorithm {
        private static final String CIPHER = "DES";
        private static final int DES_TIMES = 3, BITS = 8;
        private final Cipher encryptCipher, decryptCipher;

        //指定密钥构造方法
        private DesAlgorithm(String secret) {
            try {
                Pair<Cipher, Cipher> pair = initialize(secret, CIPHER, BITS, CIPHER);
                this.encryptCipher = pair.getKey();
                this.decryptCipher = pair.getValue();
            } catch (Exception e) {
                throw new RuntimeException("new DES algorithm error.....", e);
            }
        }

        //加密字节数组
        private byte[] encrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
            return bytes(bytes, encryptCipher, DES_TIMES);
        }

        //解密字节数组
        private byte[] decrypt(byte[] bytes) throws BadPaddingException, IllegalBlockSizeException {
            return bytes(bytes, decryptCipher, DES_TIMES);
        }
    }

    private static final class JwtAlgorithm {
        private static final Logger LOG = LoggerFactory.getLogger(JwtAlgorithm.class);
        private static final String MIXTURE = "|@|", DATA_NONE = "N/A";

        private JwtAlgorithm() {
        }

        /**
         * 签名字段， tid, uid, ext, expireTime
         **/
        private String signature(final String tenantNo, final String uid, final String ext, long expireTime) {
            String sign = Joiner.on(MIXTURE).join(sterilizeData(tenantNo, uid, ext, expireTime));
            try {
                return new StringBuilder(toHex(AES.encrypt(DES.encrypt(BytesHelper.utf8Bytes(sign))))).reverse().toString();
            } catch (Exception e) {
                throw new RuntimeException("JWT algorithm error.....", e);
            }
        }

        /**
         * 验证授权按签名字段顺序返回
         **/
        private List<String> verify(String signature, boolean checkExpire) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("verify jwt signature: {}", signature);
            }
            try {
                if (StringHelper.isBlank(signature) || signature.length() < 32) {
                    throw new RpcException(CommonCode.NoAuthority);
                }
                byte[] bytes = hex2Bytes(new StringBuilder(signature).reverse().toString());
                String sign = BytesHelper.string(DES.decrypt(AES.decrypt(bytes)));
                List<String> data = Splitter.on(MIXTURE).splitToList(sign);
                if (CollectsHelper.isNullOrEmpty(data)) {
                    throw new RpcException(CommonCode.NoAuthority);
                }
                if (checkExpire) {
                    long expireTime = Long.parseLong(CollectsHelper.end(data).get());
                    if (expireTime <= DateHelper.time()) {
                        throw new RpcException(CommonCode.NoAuthority);
                    }
                }
                List<String> rsList = Lists.newArrayList();
                for (String rs : data) {
                    if (DATA_NONE.equals(rs)) {
                        rsList.add(StringHelper.EMPTY);
                    } else {
                        rsList.add(rs);
                    }
                }
                return rsList;
            } catch (Exception e) {
                if (e instanceof RpcException) {
                    throw (RpcException) e;
                } else {
                    LOG.error("verify jwt signature error: ", e);
                    throw new RpcException(CommonCode.NoAuthority);
                }
            }
        }

        private List<String> sterilizeData(final String tenantNo, final String uid, final String ext, long expireTime) {
            long time = DateHelper.time();
            if (expireTime < time) {
                throw new RuntimeException("jwt signature expire time must greater than now");
            }
            return Lists.newArrayList(StringHelper.defaultIfBlank(tenantNo, DATA_NONE),
                                      StringHelper.defaultIfBlank(uid, DATA_NONE),
                                      StringHelper.defaultIfBlank(ext, DATA_NONE),
                                      String.valueOf(expireTime));
        }
    }
}
