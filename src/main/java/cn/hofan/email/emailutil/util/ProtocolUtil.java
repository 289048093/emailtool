package cn.hofan.email.emailutil.util;

import cn.hofan.email.emailutil.EmailConnection;
import cn.hofan.email.emailutil.Protocol;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lizhao  2014/10/16.
 */

public class ProtocolUtil {
    private final static Logger log = LoggerFactory.getLogger(ProtocolUtil.class);

    public static final String CFG_FILE_NAME = "mail_protocol_setting.properties";

    private static class EmailSettings {
        Protocol sendProtocol;
        String sendHost;
        int sendPort;
        boolean sendSsl;
        Protocol receiveProtocol;
        String receiveHost;
        int receivePort;
        boolean receiveSsl;

        boolean checkSend() {
            return StringUtils.isNotBlank(sendHost) && sendPort > 0;
        }

        boolean checkReceive() {
            return StringUtils.isNotBlank(receiveHost) && receivePort > 0;
        }

        boolean check() {
            return checkReceive() && checkSend();
        }
    }

    public static void setDefaultSettingIfPresent(EmailConnection connection) {
        if (connection.getAuth() == null || StringUtils.isBlank(connection.getAuth().getUserName())) {
            return;
        }
        EmailSettings es = getDefaultSettings(connection.getAuth().getUserName());
        if (es.checkSend()) {
            if (es.sendProtocol == null || es.sendProtocol == Protocol.smtp) {
                connection.setSmtpHost(es.sendHost);
                connection.setSmtpPort(es.sendPort);
            }
            connection.setUseSslSend(es.sendSsl);
        }
        if (es.checkReceive()) {
            connection.setUseSslReceive(es.receiveSsl);
            if (es.receiveProtocol != null) {
                if (es.receiveProtocol == Protocol.pop3) {
                    connection.setPopHost(es.receiveHost);
                    connection.setPopPort(es.receivePort);
                } else {
                    connection.setImapHost(es.receiveHost);
                    connection.setImapPort(es.receivePort);
                }
            }
        }
    }

    public static EmailSettings getDefaultSettings(String email) {
        EmailSettings es = new EmailSettings();
        String host = email.replaceAll(".+@", "");
        ConfigUtil cu = ConfigUtil.instanseOf(CFG_FILE_NAME);
        String sendPs = cu.getString(host + ".send.protocol");
        String recPs = cu.getString(host + ".receive.protocol");
        try {
            if (StringUtils.isNotBlank(sendPs)) {
                es.sendProtocol = Protocol.valueOf(sendPs.toLowerCase());
            }
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage(), e);
            //ignore
        }
        try {
            if (StringUtils.isNotBlank(recPs)) {
                es.receiveProtocol = Protocol.valueOf(recPs.toLowerCase());
            }
        } catch (IllegalArgumentException e) {
            log.warn(e.getMessage(), e);
            //ignore
        }
        es.sendHost = cu.getString(host + ".send.host");
        es.sendPort = cu.getInt(host + ".send.port");
        es.sendSsl = cu.getBoolean(host + ".send.ssl");
        es.receiveHost = cu.getString(host + ".receive.host");
        es.receivePort = cu.getInt(host + ".receive.port");
        es.receiveSsl = cu.getBoolean(host + ".receive.ssl");
        return es;
    }
}
