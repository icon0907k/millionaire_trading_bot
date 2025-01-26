package afterwork.millionaire.documentation.reports;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(
            @Value("${spring.mail.host}") String host,
            @Value("${spring.mail.port}") int port,
            @Value("${spring.mail.username}") String username,
            @Value("${spring.mail.password}") String password
    ) {
        this.mailSender = configureJavaMailSender(host, port, username, password);
    }

    private JavaMailSender configureJavaMailSender(String host, int port, String username, String password) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        return mailSender;
    }

    public void sendEmail(String recipient, String subject, String htmlFilePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(recipient);
            helper.setSubject(subject);

            String htmlContent = Files.readString(Paths.get(htmlFilePath));
            helper.setText(htmlContent, true);

            mailSender.send(message);

            System.out.println("Email sent successfully!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
