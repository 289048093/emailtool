package cn.hofan.email.emailutil;

import cn.hofan.email.emailutil.exception.EmailException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;

/**
 * @author lizhao  2014/10/9.
 */

public class EmailConnection {

    private static final Logger log = LoggerFactory.getLogger(EmailConnection.class);

    PasswordAuthentication auth;
    String smtpHost;
    int smtpPort;
    String popHost;
    int popPort;
    String imapHost;
    int imapPort;
    boolean useSslSend;
    boolean useSslReceive;
    Proxy proxy;

    public String getReceiveProtocolHost() {
        return StringUtils.defaultIfBlank(imapHost, popHost);
    }

    public int getReceiveProtocolPort() {
        return (StringUtils.isNotBlank(imapHost) && imapPort > 0) ? imapPort : popPort;
    }

    public String getReceiveProtocol() {
        if (StringUtils.isNotBlank(imapHost)) {
            if (imapHost.contains("pop")) {//防止填错的情况
                popHost = imapHost;
                popPort = imapPort;
                return useSslReceive ? "pop3s" : "pop3";
            }
            return useSslReceive ? "imaps" : "imap";
        }
        if (popHost.contains("imap")) {
            imapHost = popHost;
            imapPort = popPort;
            return useSslReceive ? "imaps" : "imap";
        }
        return useSslReceive ? "pop3s" : "pop3";
    }

    public String getSendProtocol() {
        return useSslSend ? "smtps" : "smtp";
    }

    public void checkReceive() throws EmailException {
        checkAuth();
        checkProxyConnect();
        if (StringUtils.isBlank(getReceiveProtocolHost())) {
            throw new EmailException("no imap host or pop host");
        }
        if (getReceiveProtocolPort() < 1) {
            throw new EmailException("wrong imap port or pop port,must greater then 0");
        }
    }

    public void checkSend() throws EmailException {
        checkAuth();
        checkProxyConnect();
        if (StringUtils.isBlank(smtpHost)) {
            throw new EmailException("wrong smtpHost,can not empty");
        }
        if (smtpPort < 1) {
            throw new EmailException("wrong smtp port,must greater then 0");
        }
    }

    private void checkProxyConnect() throws EmailException {
        if (proxy != null) {
            Socket socket = new Socket();
            try {
                socket.connect(proxy.address());
                if (!socket.isConnected()) {
                    catchProxyValideException(null);
                }
            } catch (IOException e) {
                catchProxyValideException(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    //ignore
                }
            }

        }
    }

    /**
     * 检查是否设置 代理无法连接时自动屏蔽代理 ，否则抛出异常
     * @param e
     * @throws EmailException
     */
    private void catchProxyValideException(Exception e) throws EmailException {
        Object prop = System.getProperties().get("mail.proxy.invalid.disable");
        if ("true".equals(prop) || Boolean.valueOf("true").equals(prop)) {
            proxy = null;
            return;
        }
        throw new EmailException("email connection proxy can not connect:" + StringUtils.trimToEmpty(e.getMessage()), e);
    }

    public void checkAuth() throws EmailException {
        if (auth == null) {
            throw new EmailException("auth can not empty");
        }
        if (StringUtils.isBlank(auth.getUserName())) {
            throw new EmailException("auth username can not empty");
        }
        try {
            new InternetAddress(auth.getUserName()).validate();
        } catch (AddressException e) {
            throw new EmailException("username is invalid :" + e.getMessage(), e);
        }
    }

    public boolean isUseSslReceive() {
        return useSslReceive;
    }

    public void setUseSslReceive(boolean useSslReceive) {
        this.useSslReceive = useSslReceive;
    }

    public PasswordAuthentication getAuth() {
        return auth;
    }

    public void setAuth(PasswordAuthentication auth) {
        this.auth = auth;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getPopHost() {
        return popHost;
    }

    public void setPopHost(String popHost) {
        this.popHost = popHost;
    }

    public int getPopPort() {
        return popPort;
    }

    public void setPopPort(int popPort) {
        this.popPort = popPort;
    }

    public String getImapHost() {
        return imapHost;
    }

    public void setImapHost(String imapHost) {
        this.imapHost = imapHost;
    }

    public int getImapPort() {
        return imapPort;
    }

    public void setImapPort(int imapPort) {
        this.imapPort = imapPort;
    }

    public boolean isUseSslSend() {
        return useSslSend;
    }

    public void setUseSslSend(boolean useSslSend) {
        this.useSslSend = useSslSend;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
}
