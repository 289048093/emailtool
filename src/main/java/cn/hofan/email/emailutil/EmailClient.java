package cn.hofan.email.emailutil;

import cn.hofan.email.emailutil.exception.EmailException;

import javax.mail.Folder;
import javax.mail.search.SearchTerm;
import java.util.List;

/**
 * @author lizhao  2014/10/9.
 */

public interface EmailClient {

    void setConnection(EmailConnection connection);

    EmailConnection getConnection();

    /**
     * 发送邮件
     *
     * @param msg
     * @throws cn.hofan.email.emailutil.exception.EmailException
     */
    void send(EmailMessage... msg) throws EmailException;

    /**
     * 接收邮件
     *
     * @param term
     * @return
     * @throws EmailException
     */
    List<EmailMessage> receive(SearchTerm... term) throws EmailException;


    List<EmailMessage> receive(String folder, SearchTerm... term) throws EmailException;

    /**
     * 获取所有文件夹
     *
     * @return
     * @throws EmailException
     */
    List<Folder> listFolders() throws EmailException;

}
