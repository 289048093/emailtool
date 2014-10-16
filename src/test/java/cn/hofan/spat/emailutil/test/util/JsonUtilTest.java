package cn.hofan.spat.emailutil.test.util;

import cn.hofan.email.emailutil.EmailMessage;
import cn.hofan.email.emailutil.Protocol;
import cn.hofan.email.emailutil.util.JsonUtil;
import org.junit.Test;

import java.io.IOException;

import static  org.junit.Assert.*;

/**
 * @author lizhao  2014/10/13.
 */

public class JsonUtilTest {

    @Test
    public void testGetByKey() throws IOException {
        String json = "{uid:\"aabbcc\",msgNo:123421}";
        assertEquals(JsonUtil.getString(json, "uid"), "aabbcc");

    }

    @Test
    public void testGetByKey2() throws IOException {
        String json = "{\"uid\":\"aabbcc\",msgNo:123421}";
        assertEquals(JsonUtil.getString(json, "uid"),"aabbcc");
    }
    @Test
         public void testGetByKey3() throws IOException {
        String json = "{\"uid\":\"aabbcc\",msgNo:123421}";
        assertEquals(JsonUtil.getInt(json, "msgNo"),new Integer(123421));
    }

    @Test
    public void testGetByKey4() throws IOException {
        String json = "{\"uid\":\"aabbcc\",\"msgNo2\":123421}";
        assertNull(JsonUtil.getInt(json, "msgNo"));
    }

    @Test
    public void testParse() throws IOException {
        String json = "{\"protocol\":\"imap\",\"uid\":\"aabbcc\",\"msgNo\":123421}";
        EmailMessage.Identity id = JsonUtil.parse(json, EmailMessage.Identity.class);

        assertEquals(id.getProtocol(), Protocol.imap);
        System.out.println(id);
    }

}
