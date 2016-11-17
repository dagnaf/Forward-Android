package io.github.dagnaf.forward;

import android.util.Log;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by david on 11/16/16.
 */
public class MailUtils {
    private static final String TAG = MailUtils.class.getSimpleName();

    // TODO
    // Support more emails
    // Use resources values or array
    private static Properties generateProps(String domain) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", true);
        switch (domain) {
            case "163.com":
                // TODO
                // com.sun.mail.smtp.SMTPSendFailedException: 554 DT:SPM 163
                props.put("mail.smtp.host", "smtp.163.com");
                props.put("mail.smtp.port", "994");
                props.put("mail.smtp.ssl.enable", true);
                return props;
            case "qq.com":
                // Need author
                props.put("mail.smtp.host", "smtp.qq.com");
                props.put("mail.smtp.port", "465");
                props.put("mail.smtp.ssl.enable", true);
                return props;
            default:
                return null;
        }
    }

    private static String emailDomain(String email) {
        return email.substring(email.indexOf("@")+1);
    }

    // TODO
    // Add string subject as parameter
    // Validate email address
    // Use string resources
    public static boolean sendMail(final String from_email, final String pw, String to_email, String body) {
        Properties props = generateProps(emailDomain(from_email));
        if (props == null) return false;
        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from_email, pw);
            }
        });

        boolean result = false;
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from_email));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to_email));
            message.setSubject("Forward");
            message.setText(body + "\n This email is brought to you by Forward.");
            Transport.send(message);
            result = true;
            Log.i(TAG, "sendMail: successful");
        } catch (MessagingException e) {
            Log.i(TAG, "sendMail: " + e);
        }
        return result;
    }

    public static boolean verify(final String email, final String pw) {
        Properties props = generateProps(emailDomain(email));
        if (props == null) return false;

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, pw);
            }
        });

        boolean result;
        Transport transport = null;
        try {
            transport = session.getTransport("smtp");
            transport.connect();
            result = true;
        } catch (MessagingException e) {
            Log.i(TAG, "verify: connect " + e);
            result = false;
        } finally {
            try {
                if (transport != null) transport.close();
            } catch (MessagingException e) {
                Log.i(TAG, "verify: close " + e);
                result = false;
            }
        }
        return result;
    }
}
