package com.tunfin.payment.service;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from:noreply@tunfin.com}")
    private String fromEmail;

    @Value("${spring.mail.host:none}")
    private String mailHost;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendTransferNotification(
            String toEmail,
            String userName,
            String type,
            BigDecimal amount,
            String otherPartyName) {

        log.info(">>> EMAIL: Attempting to send SMTP notification to {}", toEmail);

        if ("none".equals(mailHost) || "smtp.example.com".equals(mailHost)) {
            log.warn(">>> EMAIL: SMTP Host is NOT configured. Logging to console instead.");
            log.info(
                    "Dear {}, you {} {} {} {}.",
                    userName,
                    type.equals("SENDER") ? "sent" : "received",
                    amount,
                    type.equals("SENDER") ? "to" : "from",
                    otherPartyName);
            return;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(
                    "TunFin • Transfer " + (type.equals("SENDER") ? "Confirmation" : "Notification"));

            String actionText = type.equals("SENDER")
                    ? "You have successfully sent"
                    : "You have received";

            String directionText = type.equals("SENDER") ? "to" : "from";

            String htmlBody = """
                    <!DOCTYPE html>
                    <html>
                    <head>
                        <style>
                            body {
                                background-color: #0f0f14;
                                font-family: 'Segoe UI', Arial, sans-serif;
                                color: #ffffff;
                                padding: 30px;
                                display: flex;
                                justify-content: center;
                                align-items: center;
                            }
                            .card {
                                width: 520px;
                                background: #161622;
                                border-radius: 16px;
                                padding: 32px;
                                box-shadow: 0 0 45px rgba(124, 77, 255, 0.25);
                                text-align: center;
                            }
                            .header {
                                font-size: 24px;
                                font-weight: 600;
                                margin-bottom: 24px;
                                color: #ffffff;
                            }
                            .content {
                                font-size: 15px;
                                line-height: 1.7;
                                color: #ffffff;
                            }
                            .amount {
                                font-size: 30px;
                                font-weight: 700;
                                margin: 22px 0;
                                color: #ffffff;
                            }
                            .footer {
                                margin-top: 32px;
                                font-size: 13px;
                                color: #ffffff;
                                opacity: 0.85;
                            }
                            .brand {
                                color: #b79cff;
                                font-weight: 600;
                            }
                        </style>
                    </head>
                    <body>
                        <div class="card">
                            <div class="header">Transfer Notification</div>

                            <div class="content">
                                Dear <strong>%s</strong>,<br><br>

                                %s
                                <div class="amount">%s TND</div>
                                %s <strong>%s</strong>.
                            </div>

                            <div class="footer">
                                Thank you for choosing <span class="brand">TunFin</span>.<br>
                                Secure • Modern • Reliable
                            </div>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    userName,
                    actionText,
                    amount.toPlainString(),
                    directionText,
                    otherPartyName);

            helper.setText(htmlBody, true);
            mailSender.send(mimeMessage);

            log.info(">>> EMAIL: SMTP Notification sent successfully to {}", toEmail);

        } catch (Exception e) {
            log.error(">>> EMAIL: Failed to send SMTP notification to {}: {}", toEmail, e.getMessage());
        }
    }
}
