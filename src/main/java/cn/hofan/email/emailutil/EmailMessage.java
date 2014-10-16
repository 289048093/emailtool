package cn.hofan.email.emailutil;

import cn.hofan.email.emailutil.exception.EmailException;
import cn.hofan.email.emailutil.util.MessageUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lizhao  2014/10/9.
 */

public class EmailMessage {

    private static final Logger log = LoggerFactory.getLogger(EmailMessage.class);

    private Message source;

    private String folderName;

    private String subject;

    private String contentText;

    private InternetAddress from;

    private List<InternetAddress> to;

    private List<InternetAddress> cc;

    private InternetAddress reply;

    private List<Attachment> attachments;

    private Date receiveDate;

    private Date sendDate;

    private Identity identity;

    public static class Identity {
        Protocol protocol;
        String uid;
        int msgNo;

        public Protocol getProtocol() {
            return protocol;
        }

        public void setProtocol(Protocol protocol) {
            this.protocol = protocol;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getMsgNo() {
            return msgNo;
        }

        public void setMsgNo(int msgNo) {
            this.msgNo = msgNo;
        }

        @Override
        public String toString() {
            return String.format("{\"protocol\":\"%s\",\"uid\":\"%s\",\"msgNo\":%d}", protocol != null ? protocol.name() : Protocol.unknow.name(), uid, msgNo);
        }
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getContentText() {
        return contentText;
    }

    public void setContentText(String contentText) {
        this.contentText = contentText;
    }

    public InternetAddress getFrom() {
        return from;
    }

    public void setFrom(InternetAddress from) {
        this.from = from;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void setFrom(String email, String name) {
        if (StringUtils.isBlank(email)) {
            this.from = null;
        }
        try {
            this.from = new InternetAddress(email, name);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public InternetAddress getReply() {
        return reply;
    }

    public void setReply(InternetAddress reply) {
        this.reply = reply;
    }

    public void setReply(String email, String name) {
        if (StringUtils.isBlank(email)) {
            this.reply = null;
        }
        try {
            this.reply = new InternetAddress(email, name);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<InternetAddress> getTo() {
        return to;
    }

    /**
     * convert to linkedList , avoid UnsupportedOperationException when invoke add method,eg Arrays.ArrayList
     *
     * @param to
     */
    public void setTo(List<InternetAddress> to) {
        if (to == null) {
            this.to = null;
            return;
        }
        this.to = new LinkedList<>(to);
    }

    public void addTo(InternetAddress t) {
        if (t == null) {
            return;
        }
        if (to == null) {
            to = new LinkedList<>();
        }
        to.add(t);
    }

    public void addTo(String email) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (to == null) {
            to = new LinkedList<>();
        }
        try {
            to.add(new InternetAddress(email));
        } catch (AddressException e) {
            log.error(e.getMessage(), e);// ignore
        }
    }

    public void addTo(String email, String name) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (to == null) {
            to = new LinkedList<>();
        }
        try {
            to.add(new InternetAddress(email, name));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);// ignore
        }
    }

    public List<InternetAddress> getCc() {
        return cc;
    }

    /**
     * convert to linkedList , avoid UnsupportedOperationException when invoke add method,eg Arrays.ArrayList
     *
     * @param cc
     */
    public void setCc(List<InternetAddress> cc) {
        if (cc == null) {
            this.cc = null;
            return;
        }
        this.cc = new LinkedList<>(cc);
    }

    public void addCc(InternetAddress t) {
        if (t == null) {
            return;
        }
        if (cc == null) {
            cc = new LinkedList<>();
        }
        cc.add(t);
    }

    public void addCc(String email, String name) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (cc == null) {
            cc = new LinkedList<>();
        }
        try {
            cc.add(new InternetAddress(email, name));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);// ignore
        }
    }

    public void addCc(String email) {
        if (StringUtils.isBlank(email)) {
            return;
        }
        if (cc == null) {
            cc = new LinkedList<>();
        }
        try {
            cc.add(new InternetAddress(email));
        } catch (AddressException e) {
            log.error(e.getMessage(), e);// ignore
        }
    }


    public EmailMessage() {
    }

    /**
     * 附件延迟下载
     *
     * @param source
     */
    public EmailMessage(Message source) throws EmailException {
        this.source = source;
        MessageUtil.copyProperties(source, this);
    }


    public List<Attachment> getAttachments() {
        if (attachments == null) {

            attachments = new ArrayList<>();
            if (source == null) {
                log.warn("getAttachments() no source message, if this message is received ,use constructor: EmailMessage(Message)");
                return null;
            }

            Object content = null;
            try {
                content = source.getContent();
                if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart part = multipart.getBodyPart(i);
                        if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                            EmailMessage.Attachment attachment = MessageUtil.getAttachment(part, i);
                            attachments.add(attachment);
                        }
                    }
                }
            } catch (IOException | MessagingException e) {
                e.printStackTrace();
            }
        }
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public String getUid() {
        return identity != null ? identity.toString() : null;
    }

    public Date getReceiveDate() {
        return receiveDate;
    }

    public void setReceiveDate(Date receiveDate) {
        this.receiveDate = receiveDate;
    }

    public Date getSendDate() {
        return sendDate;
    }

    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    /**
     * 附件
     */
    public static class Attachment {
        private int partIndex;
        private String name;
        private String contentType;
        private byte[] bytes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContentType() {
            return contentType;
        }

        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public void setBytes(byte[] bytes) {
            this.bytes = bytes;
        }

        public int getPartIndex() {
            return partIndex;
        }

        public void setPartIndex(int partIndex) {
            this.partIndex = partIndex;
        }
    }

}
