package in.bankportal.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public final class Mailer {
    private Mailer() {}

    public static void sendOtp(String to, String otp) throws MessagingException {
        sendText(to, "Bankwise login verification code",
            "Your Bankwise verification OTP is: " + otp +
            "\n\nThis code expires in 10 minutes. Do not share it with anyone.");
    }

    public static void sendText(String to, String subject, String text) throws MessagingException {
        String from = env("BANK_SMTP_EMAIL", "ganeshbajaru41@gmail.com");
        String appPassword = System.getenv("BANK_SMTP_APP_PASSWORD");
        if (appPassword == null || appPassword.isBlank()) {
            throw new MessagingException("SMTP is not configured. Set BANK_SMTP_APP_PASSWORD to a Gmail App Password.");
        }
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        Session session = Session.getInstance(props, new Authenticator() {
            @Override protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, appPassword);
            }
        });
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(text);
        Transport.send(message);
    }

    private static String env(String key, String fallback) {
        String v = System.getenv(key);
        return v == null || v.isBlank() ? fallback : v;
    }
}
