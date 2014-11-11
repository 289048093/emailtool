邮件收发系统：
通过对javamail的再次封装实现高效方便地收发邮件，可群发，可网络代理，可方便的收发附件。

        EmailConnection connection = new EmailConnection();
        // set connection ,such as email pwd proxy etc
        EmailClient client =  EmailClientImpl.instanseOf(connection);
        
        //receive email
        List<EmailMessage> receiveMsgs = client.receive(new SentDateTerm(ComparisonTerm.EQ, new java.util.Date()));
        
        //send email
        // set message,such subject context attachments etc
        EmailMessage message = new EmailMessage();
        client.send(message);
        //batch send
        EmailMessage message2 = new EmailMessage();
        // set message,such subject context attachments etc
        client.send(message,message2,...);
