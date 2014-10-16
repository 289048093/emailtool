package cn.hofan.spat.emailutil.test;

import jdk.nashorn.internal.ir.Node;
import jdk.nashorn.internal.parser.JSONParser;
import jdk.nashorn.internal.runtime.ErrorManager;
import jdk.nashorn.internal.runtime.Source;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.MappingJsonFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import javax.mail.internet.AddressException;
import java.io.*;

/**
 * @author lizhao  2014/10/10.
 */

public class MyTest {

    @Test
    public void test() throws AddressException, IOException {
        String ss = String.format("{\"%s\":\"%s\",\"%s\":%d}", "uid", "asdf", "msgNo", 12312);
        System.out.println(ss);

    }

    @Test
    public void test2(){
        System.getProperties().put("test.test",true);
        System.out.println(System.getProperties().get("test.test").getClass().getName());
        Exception exception = new Exception("", null);
    }

    @Test
    public void test3() {
        File file = new File("test.txt");
        System.out.println(file.getAbsolutePath());
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            os.write(new byte[]{65,66,67,68,69,95,96,97});
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
