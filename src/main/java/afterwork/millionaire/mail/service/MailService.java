package afterwork.millionaire.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * HTML 템플릿을 활용한 이메일 전송
     * @param recipient 받는 사람 이메일
     * @param subject 이메일 제목
     * @param content 이메일 본문 내용
     */
    public void sendOverseasEmail(String recipient, String subject, String content,String templatePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Thymeleaf 템플릿 변수 설정
            Context context = new Context();
            context.setVariable("content", content);  // 본문 내용 삽입

            // 템플릿 파일 로드 및 처리
            //String templatePath = "mail/overseas.html";
            String htmlContent = loadHtmlTemplate(templatePath, context);

            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // HTML 적용

            // 이메일 전송
            mailSender.send(message);
            log.info("이메일 전송 성공: {}", recipient);
        } catch (MessagingException e) {
            log.error("이메일 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * HTML 템플릿을 로드하고 Thymeleaf로 처리
     * @param templatePath 리소스 폴더 내 템플릿 경로
     * @param context Thymeleaf 변수 컨텍스트
     * @return 처리된 HTML 문자열
     */
    private String loadHtmlTemplate(String templatePath, Context context) {
        try {
            ClassPathResource resource = new ClassPathResource(templatePath);
            String html = Files.readString(Paths.get(resource.getURI()), StandardCharsets.UTF_8);
            return templateEngine.process(html, context);
        } catch (Exception e) {
            log.error("HTML 템플릿 로딩 실패: {}", e.getMessage(), e);
            return "<p>Error loading email template</p>";
        }
    }
}
