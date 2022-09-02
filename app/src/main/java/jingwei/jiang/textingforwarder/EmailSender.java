package jingwei.jiang.textingforwarder;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class EmailSender {
    private final Session session;

    /**
     * Initialize a emailSender instance.
     * @param protocol
     * @param host
     * @param port
     * @param userName
     * @param password
     */
    public EmailSender(String protocol, String host, String port, String userName, String password) {
        Properties properties = System.getProperties();

        // Setup mail server
        properties.setProperty( "mail.transport.protocol", protocol);
        properties.setProperty( "mail.smtp.host", host) ;
        properties.setProperty("mail.smtp.port", port);
        properties.setProperty( "mail.smtp.auth", "true");

        session = Session.getDefaultInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, password);
            }
        });
    }

    /**
     * Send an email.
     * @param from
     * @param to
     * @param subject
     * @param body
     * @throws MessagingException
     */
    public void send(String from, String to, String subject, String body) {
        Thread thread = new Thread(() -> {
            try {
                // Create a default MimeMessage object.
                MimeMessage message = new MimeMessage(session);
                // Set From: header field of the header.
                message.setFrom(new InternetAddress(from));
                // Set To: header field of the header.
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
                // Set Subject: header field
                message.setSubject(subject);
                // Now set the actual message
                message.setText(body);
                // Send message
                Transport.send(message);
            } catch (MessagingException exception) {
                throw new RuntimeException(exception);
            }
        });
        thread.start();
    }
}
