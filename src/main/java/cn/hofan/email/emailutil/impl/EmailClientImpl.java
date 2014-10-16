package cn.hofan.email.emailutil.impl;

import cn.hofan.email.emailutil.*;
import cn.hofan.email.emailutil.exception.EmailException;
import cn.hofan.email.emailutil.exception.EmailReceiveException;
import cn.hofan.email.emailutil.exception.EmailSendException;
import cn.hofan.email.emailutil.util.MessageUtil;
import cn.hofan.email.emailutil.util.ProtocolUtil;
import com.sun.mail.imap.IMAPFolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.search.AndTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author lizhao  2014/10/9.
 */

public class EmailClientImpl implements EmailClient {

    private static final Logger log = LoggerFactory.getLogger(EmailClientImpl.class);

    private EmailConnection connection;

    private DefaultEmailLabel defaultLabel = DefaultEmailLabel.INBOX;

    private EmailClientImpl() {
    }

    public static EmailClient instanseOf(EmailConnection connection) {
        EmailClientImpl client = new EmailClientImpl();
        ProtocolUtil.setDefaultSettingIfPresent(connection);
        client.connection = connection;
        return client;
    }

    @Override
    public void setConnection(EmailConnection connection) {
        throw new UnsupportedOperationException("client connection can not change");
    }

    private InetSocketAddress getAddr(String protocol) {
        switch (protocol) {
            case "smtps":
            case "smtp":
                return new InetSocketAddress(connection.getSmtpHost(), connection.getSmtpPort());
            case "pop3s":
            case "pop3":
                return new InetSocketAddress(connection.getPopHost(), connection.getPopPort());
            case "imaps":
            case "imap":
                return new InetSocketAddress(connection.getImapHost(), connection.getImapPort());
            default:
                return new InetSocketAddress(connection.getSmtpHost(), connection.getSmtpPort());
        }

    }

    private Properties initProperties(EmailConnection connection, String protocol) {
        Properties properties = System.getProperties();
        if (protocol.contains("smtp")) {
            properties.put("mail.transport.protocol", protocol);
        } else {
            properties.put("mail.store.protocol", protocol);
        }
        properties.put("mail.debug", log.isDebugEnabled());
        properties.put("mail.socket.debug", log.isDebugEnabled());

        properties.put("mail." + protocol + ".host", getAddr(protocol).getHostName());
        properties.put("mail." + protocol + ".port", getAddr(protocol).getPort());
        properties.put("mail.mime.ignoreunknownencoding", true);//忽略未知的编码，避免当接收的邮件编码未知时，会抛出异常

        if (connection.getProxy() != null) {
            Proxy proxy = connection.getProxy();
            if (proxy.type() == Proxy.Type.SOCKS) {
                properties.put("mail." + protocol + ".socks.host", ((InetSocketAddress) proxy.address()).getHostName());
                properties.put("mail." + protocol + ".socks.port", ((InetSocketAddress) proxy.address()).getPort());
            } else {
                log.warn("only support socks proxy !");
            }
        }
        return properties;
    }


    @Override
    public EmailConnection getConnection() {
        return connection;
    }

    @Override
    public void send(EmailMessage... msgs) throws EmailSendException {

        Transport transport = null;
        try {
            connection.checkSend();
            Session session = getSession(connection.getSendProtocol());

            transport = getTransport(session);
            for (EmailMessage msg : msgs) {
                log.debug("send email,from:{},to:{},cc:{},subject:{},smtp host:{},smtp port:{},proxy:",
                        msg.getFrom(), msg.getTo(), msg.getCc(), msg.getSubject(),
                        connection.getSmtpHost(), connection.getSmtpPort(), connection.getProxy());

                Message message = new MimeMessage((Session) null);
                MessageUtil.copyProperties(msg, message);
                transport.sendMessage(message, msg.getTo().toArray(new Address[msg.getTo().size()]));

            }
            transport.close();
        } catch (Exception e) {
            throw new EmailSendException(e.getMessage(), e);
        } finally {
            if (transport != null) {
                try {
                    transport.close();
                } catch (MessagingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private Transport getTransport(Session session) throws MessagingException {
        Transport transport = session.getTransport();
        transport.connect(connection.getAuth().getUserName(), connection.getAuth().getPassword());
        return transport;
    }


    @Override
    public List<EmailMessage> receive(SearchTerm... terms) throws EmailException {
        return receive(DefaultEmailLabel.INBOX.getName(), terms);
    }


    public EmailMessage receiveByUid(String folderName, String uid) throws EmailException {
        Store store = null;
        try {
            connection.checkReceive();
            Session session = getSession(connection.getReceiveProtocol());
            store = getStore(session);
            Folder folder = getFolder(folderName, store);
            if (folder == null) {
                throw new EmailReceiveException("找不到该邮件目录:" + folderName);
            }
            EmailMessage.Identity id = MessageUtil.parse2Identity(uid);
            String srcUid = id.getUid();
            if (folder instanceof IMAPFolder && id.getProtocol() == Protocol.imap) {
                Message msg = ((IMAPFolder) folder).getMessageByUID(Long.parseLong(srcUid));
                return new EmailMessage(msg);
            }
            int msgNo = id.getMsgNo();
            Message msg = folder.getMessage(msgNo);
            if (srcUid.equals(MessageUtil.getSrcUid(msg))) {
                return new EmailMessage(msg);
            }
            Message[] msgs = folder.getMessages();
            for (Message m : msgs) {
                if (srcUid.equals(MessageUtil.getSrcUid(m))) {
                    return new EmailMessage(m);
                }
            }

        } catch (MessagingException | IOException e) {
            throw new EmailException("email receive error,error msg:" + e.getMessage(), e);
        }
        return null;

    }

    @Override
    public List<EmailMessage> receive(String folderName, SearchTerm... terms) throws EmailException {
        log.debug("starting receive email,from:{},protocol host:{},protocol port:{}",
                connection.getAuth().getUserName(), StringUtils.isNoneBlank(connection.getImapHost()) ? connection.getImapHost() : connection.getPopHost(),
                StringUtils.isNoneBlank(connection.getImapHost()) ? connection.getImapPort() : connection.getPopPort());

        Store store = null;
        try {
            connection.checkReceive();
            Session session = getSession(connection.getReceiveProtocol());
            store = getStore(session);

            Folder folder = getFolder(folderName, store);
            if (folder == null) {
                throw new EmailReceiveException("找不到该邮件目录:" + folderName);
            }
            folder.open(Folder.READ_ONLY);
            Message[] messages = null;
            if (terms == null) {
                messages = folder.getMessages();
            } else {
                SearchTerm term = terms.length == 1 ? terms[0] : new AndTerm(terms);
                messages = folder.search(term);
            }
            if (messages == null || messages.length == 0) {
                log.debug("receive message empty ,email address:{}", connection.getAuth().getUserName());
                return new ArrayList<>();
            }
            List<EmailMessage> res = new ArrayList<>(messages.length);
            EmailMessage tmp = null;
            for (Message m : messages) {
                res.add(new EmailMessage(m));
            }
            log.debug("receive message size:{},email address:{}", res.size(), connection.getAuth().getUserName());
            return res;
        } catch (NoSuchProviderException e) {
            log.error(e.getMessage(), e);
            throw new EmailReceiveException("wrong procotol,check the setting:mail.transport.protocol,error msg:" + e.getMessage(), e);
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            throw new EmailReceiveException("email receive error,error msg:" + e.getMessage(), e);

        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Override
    public List<Folder> listFolders() throws EmailException {
        log.debug("listFolders ,from:{},protocol host:{},protocol port:{}",
                connection.getAuth().getUserName(), StringUtils.isNoneBlank(connection.getImapHost()) ? connection.getImapHost() : connection.getPopHost(),
                StringUtils.isNoneBlank(connection.getImapHost()) ? connection.getImapPort() : connection.getPopPort());

        Session session = getSession(connection.getReceiveProtocol());
        Store store = null;
        try {
            store = getStore(session);
            Folder folder = store.getDefaultFolder();
            List<Folder> res = new ArrayList<>();
            getChildrenFolders(folder, res);
            return res;
        } catch (MessagingException e) {
            log.error(e.getMessage(), e);
            throw new EmailException(e.getMessage(), e);
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    private void getChildrenFolders(Folder folder, List<Folder> folders) throws MessagingException {
        Folder[] children = folder.list();
        if (children != null && children.length > 0) {
            for (Folder c : children) {
                folders.add(c);
                getChildrenFolders(c, folders);
            }
        }
    }

    private Folder getFolder(String name, Store store) throws EmailException {
        if (store == null) {
            throw new EmailException("Gmail IMAP store cannot be null");
        }
        try {
            name = (name == null)
                    ? DefaultEmailLabel.INBOX.getName()
                    : name;
            Folder folder = store.getFolder(name);
            if (folder.exists()) {
                return folder;
            }
            DefaultEmailLabel label = DefaultEmailLabel.nameOf(name);
            folder = store.getFolder(label.getZhName());
            if (folder.exists()) {
                return folder;
            }

        } catch (final Exception e) {
            throw new EmailException("EmailClient failed getting "
                    + "Folder: " + name, e);
        }

        throw new EmailException("EmailClient Folder name cannot be null");
    }

    private Session getSession(String protocol) {
        return Session.getInstance(initProperties(connection, protocol));
    }

    private Store getStore(Session session) throws EmailException {
        try {
            Store store = session.getStore();
            store.addConnectionListener(new EmailConnectionHandler(
                    new ConnectionInfo(connection.getAuth().getUserName(),
                            connection.getReceiveProtocolHost(),
                            connection.getReceiveProtocolPort())));
            store.connect(connection.getAuth().getUserName(), connection.getAuth().getPassword());
            return store;
        } catch (MessagingException e) {
            throw new EmailException(e.getMessage(), e);
        }
    }
}
