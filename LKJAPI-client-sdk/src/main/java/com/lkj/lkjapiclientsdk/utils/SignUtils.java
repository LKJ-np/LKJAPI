package com.lkj.lkjapiclientsdk.utils;

import cn.hutool.crypto.digest.DigestAlgorithm;
import cn.hutool.crypto.digest.Digester;

/**
 * @Description: 签名工具
 * @Author：LKJ
 * @Package：com.lkj.lkjapiinterface.utils
 * @Project：LKJAPI-interface
 * @name：SignUtil
 * @Date：2023/12/5 16:06
 * @Filename：SignUtil
 */
public class SignUtils {


    public static String genSign( String body,String secretKey) {

        Digester md5 = new Digester(DigestAlgorithm.SHA256);

        String digestHex =body + "." + secretKey;

        return md5.digestHex(digestHex);
    }
}
