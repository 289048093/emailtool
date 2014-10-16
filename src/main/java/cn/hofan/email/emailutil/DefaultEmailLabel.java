package cn.hofan.email.emailutil;

public enum DefaultEmailLabel {
    /**
     * Imap Gmail folder label name for {@code [Gmail]/All Mail}
     */
    ALL_MAIL("[Gmail]/All Mail","[Gmail]/所有邮件"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Drafts}
     */
    DRAFTS("[Gmail]/Drafts","[Gmail]/草稿"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Sent Mail}
     */
    SENT_MAIL("[Gmail]/Sent Mail","[Gmail]/已发邮件"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Spam}
     */
    SPAM("[Gmail]/Spam","[Gmail]/垃圾邮件"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Starred}
     */
    STARRED("[Gmail]/Starred","[Gmail]/已加星标"),
    /**
     * Imap Gmail folder label name for {@code INBOX}
     */
    INBOX("INBOX","[Gmail]/收件箱"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Trash}
     */
    TRASH("[Gmail]/Trash","[Gmail]/已删除邮件"),
    /**
     * Imap Gmail folder label name for {@code [Gmail]/Important}
     */
    IMPORTANT("[Gmail]/Important","[Gmail]/重要");
   
    /**
     * Imap Gmail folder label name
     */
    private String name;

    private String zhName;

    /**
     * Constructor with Imap Gmail label name
     *
     * @param name Gmail Imap folder label name
     */
    private DefaultEmailLabel(String name, String zhName) {
        this.name = name;
        this.zhName =zhName;
    }

    /**
     * Gets the {@link #name}
     *
     * @return name of the Gmail Imap folder label.
     */
    public String getName() {
        return name;
    }

    public String getZhName() {
        return zhName;
    }

    public static DefaultEmailLabel nameOf(String name){
        for(DefaultEmailLabel l:values()){
            if(l.name.equals(name)){
                return l;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}