package cn.hofan.email.emailutil.util;

import cn.hofan.email.emailutil.EmailMessage;
import cn.hofan.email.emailutil.Protocol;
import cn.hofan.email.emailutil.exception.EmailException;
import com.sun.mail.imap.IMAPBodyPart;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.pop3.POP3Folder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author lizhao  2014/10/10.
 */

public class MessageUtil {

    public final static int READ_BYTE_BUF = 1024 * 8;

    public static void copyProperties(EmailMessage src, Message dest) throws EmailException {
        try {
            dest.setFrom(src.getFrom());
            dest.addRecipients(Message.RecipientType.TO, src.getTo().toArray(new Address[src.getTo().size()]));
            if (src.getCc() != null && !src.getCc().isEmpty()) {
                dest.addRecipients(Message.RecipientType.CC, src.getCc().toArray(new Address[src.getCc().size()]));
            }
            dest.setReplyTo(src.getReply() != null ? new Address[]{src.getReply()} : null);
            dest.setSubject(src.getSubject());
            dest.setText(src.getContentText());
            if (src.getAttachments() != null && !src.getAttachments().isEmpty()) {
                Multipart multipart = new MimeMultipart();

                for (EmailMessage.Attachment file : src.getAttachments()) {
                    BodyPart bodyPart = new MimeBodyPart();
                    DataSource fileSource = new ByteArrayDataSource(file.getBytes(), file.getContentType());
                    DataHandler fileDataHandler = new DataHandler(fileSource);
                    bodyPart.setDataHandler(fileDataHandler);
                    bodyPart.setFileName(MimeUtility.encodeText(file.getName(), "UTF-8", null));
                    multipart.addBodyPart(bodyPart);

                }
                dest.setContent(multipart);
            }
        } catch (MessagingException e) {
            throw new EmailException("illegal email_from address ,error msg" + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            throw new EmailException("charset error:" + e.getMessage(), e);
        }

    }

    public static void copyProperties(Message src, EmailMessage dest) throws EmailException {
        try {
            dest.setSubject(src.getSubject());
            Address[] tmp = src.getFrom();
            dest.setFrom((tmp != null && tmp.length > 0 && tmp[0] instanceof InternetAddress) ? (InternetAddress) tmp[0] : null);
            dest.setTo(convert2InternetAddressList(src.getRecipients(Message.RecipientType.TO)));
            dest.setCc(convert2InternetAddressList(src.getRecipients(Message.RecipientType.CC)));
            tmp = src.getReplyTo();
            dest.setReply((tmp != null && tmp.length > 0 && tmp[0] instanceof InternetAddress) ? (InternetAddress) tmp[0] : null);

            dest.setReceiveDate(src.getReceivedDate());
            dest.setSendDate(src.getSentDate());
            dest.setFolderName(src.getFolder().getFullName());


            dest.setIdentity(generateIdentity(src));
            List<EmailMessage.Attachment> attachments = new ArrayList<>();
            StringBuilder contentText = new StringBuilder();
            Object content = src.getContent();
            if (content instanceof String) {
                contentText.append(content);
            } else if (content instanceof Multipart) {
                Multipart parts = (Multipart) content;
                for (int i = 0; i < parts.getCount(); i++) {
                    BodyPart part = parts.getBodyPart(i);
                    if (part.getContent() instanceof String) {
                        contentText.append(part.getContent());
                    }
                    if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
                        EmailMessage.Attachment attachment = getAttachment(part,i);
                        attachments.add(attachment);
                    }
                }
            }
            dest.setContentText(contentText.toString());
            dest.setAttachments(attachments);

        } catch (MessagingException | IOException e) {
            throw new EmailException(e.getMessage(), e);
        }
    }

    public static EmailMessage.Identity generateIdentity(Message src) throws MessagingException {
        EmailMessage.Identity ide = new EmailMessage.Identity();
        ide.setMsgNo(src.getMessageNumber());
        ide.setUid(getSrcUid(src));
        Protocol protocol = Protocol.unknow;
        Folder folder = src.getFolder();
        if (folder instanceof IMAPFolder) {
            protocol = Protocol.imap;
        }
        if (folder instanceof POP3Folder) {
            protocol = Protocol.pop3;
        }
        ide.setProtocol(protocol);
        return ide;
    }

    public static String getSrcUid(Message src) throws MessagingException {
        Folder folder = src.getFolder();
        String uid = null;
        if (folder instanceof UIDFolder) {
            uid = String.valueOf(((UIDFolder) folder).getUID(src));
        } else if (folder instanceof POP3Folder) {
            uid = ((POP3Folder) folder).getUID(src);
        } else {
            uid = "";
        }
        return uid;
    }

    /**
     * @param as
     * @return
     */
    private static List<InternetAddress> convert2InternetAddressList(Address[] as) {
        List<InternetAddress> res = new ArrayList<>();
        if (as == null || as.length == 0) {
            return res;
        }
        for (Address a : as) {
            if (a instanceof InternetAddress) {
                res.add((InternetAddress) a);
            }
        }
        return res;
    }

    public static EmailMessage.Identity parse2Identity(String json) throws IOException {
        return JsonUtil.parse(json, EmailMessage.Identity.class);
    }


    public static EmailMessage.Attachment getAttachment(BodyPart part,int index) throws MessagingException, IOException {
        EmailMessage.Attachment attachment = new EmailMessage.Attachment();
        attachment.setPartIndex(index);
        attachment.setContentType(part.getContentType());
        attachment.setName(part.getFileName());
        InputStream is = part.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();// TODO if attachment size too long ,memory out?!
        byte[] buf = new byte[READ_BYTE_BUF];
        int count;
        while ((count = is.read(buf)) != -1) {
            baos.write(buf, 0, count);
        }
        attachment.setBytes(baos.toByteArray());
        return attachment;
    }
}
