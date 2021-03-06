package utils;


import com.leyou.auth.pojo.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * @author bystander
 * @date 2018/10/1
 */
public class JwtUtilsTest {

    private static final String publicKeyPath = "D:\\rsa\\rsa.pub";
    private static final String privateKeyPath = "D:\\rsa\\rsa.pri";

    private PrivateKey privateKey;
    private PublicKey publicKey;


    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
    }

   @Before
    public void testGetRsa() throws Exception {
        privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        publicKey = RsaUtils.getPublicKey(publicKeyPath);
    }

    @Test
    public void generateToken() {
        //生成Token
        String s = JwtUtils.generateToken(new UserInfo(20L, "Jack"), privateKey, 5);
        System.out.println("s = " + s);
    }

    /**
     * 解析token
     */
    @Test
    public void parseToken() {
        //String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTUzODM2OTM3N30.Vi7UJrwMu0BOHMQoSSLefzGU1ir5LG-drcvAHPjMMMBzQz1oASjoDsiuw3h0bqVUUWXjdNcpybCWVuZ8UvOXOr-Jecqjz3NF_ZDfgessRGsijIIbju0qak6Zfm09jsjnHFTZ2munFJdM0I0RsiafQtkJSiLji7QXlvjCquKJUkg";
        String token ="eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTU1OTA0ODYxNX0.TLlmBbvcZSqdC4O3mOWkWUJZfwf6T5Oq0y-A8dJuq4iI8w10Fa38DLH53rmhJnqQWn19ytsEtSb6EbyYV1d6e6XFRZl0sNgWsWIOOAI4QpgdSS04IiPqFbBjhI8ncqIC2GXrb5v3LVNKVaf7H5jyZOSOz6elji9bDNuP8lMb_OA";
        UserInfo userInfo = JwtUtils.getUserInfo(publicKey, token);
        System.out.println("id:" + userInfo.getId());
        System.out.println("name:" + userInfo.getName());
    }

    @Test
    public void parseToken1() {
    }

    @Test
    public void getUserInfo() {
    }

    @Test
    public void getUserInfo1() {
    }
}