package cn.hofan.spat.emailutil.test.client;

import cn.hofan.email.emailutil.EmailClient;
import cn.hofan.email.emailutil.EmailConnection;
import cn.hofan.email.emailutil.EmailMessage;
import cn.hofan.email.emailutil.exception.EmailException;
import cn.hofan.email.emailutil.impl.EmailClientImpl;
import org.junit.Before;
import org.junit.Test;

import javax.mail.Folder;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SentDateTerm;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

/**
 * @author lizhao  2014/10/10.
 */

public class EmailClientTest {

    EmailConnection connection = new EmailConnection();
    EmailClient client = null;

    @Before
    public void before(){
        connection.setAuth(new PasswordAuthentication("c.onlinecs@gmail.com","na6e/r46fef**"));
        connection.setImapHost("imap.gmail.com");
        connection.setImapPort(993);
        connection.setUseSslSend(true);
        connection.setSmtpHost("smtp.gmail.com");
        connection.setSmtpPort(465);
//        connection.setProxy(new Proxy(Proxy.Type.SOCKS,new InetSocketAddress("127.0.0.1",8098)));
       client =  EmailClientImpl.instanseOf(connection);
    }

    @Test
    public void testListFolder() throws EmailException {
        List<Folder> folders = client.listFolders();
        System.out.println(folders);

    }


    @Test
    public void testReceive() throws EmailException {
        List<EmailMessage> emails = client.receive(new ReceivedDateTerm(ComparisonTerm.GT, Date.from(Instant.now().plus(-3, ChronoUnit.DAYS))));
        System.out.println(emails);
//        EmailConnection connection = new EmailConnection();
//        // set connection ,such as email pwd proxy etc
//        EmailClient client =  EmailClientImpl.instanseOf(connection);
//        //receive email
//        List<EmailMessage> receiveMsgs = client.receive(new SentDateTerm(ComparisonTerm.EQ, new java.util.Date()));
//        //send email
//        // set message,such subject context attachments etc
//        EmailMessage message = new EmailMessage();
//        client.send(message);
//        //batch send
//        EmailMessage message2 = new EmailMessage();
//        // set message,such subject context attachments etc
//        client.send(message,message2,...);

    }

    @Test
    public void testSend() throws AddressException, EmailException {
        EmailMessage message = new EmailMessage();
        message.setContentText("测试邮件内容");
        message.setSubject("测试邮件标题");
        message.setFrom(new InternetAddress(connection.getAuth().getUserName()));
        message.setTo(Arrays.asList(new InternetAddress[]{new InternetAddress("lizhao@hofan.cn")}));
        message.setCc(Arrays.asList(new InternetAddress[]{new InternetAddress("289048093@qq.com")}));
        client.send(message);
    }
}
