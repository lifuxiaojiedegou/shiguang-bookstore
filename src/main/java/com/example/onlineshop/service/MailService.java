package com.example.onlineshop.service;

// AI-assisted: Claude Code, 2026-10; reviewed and modified by 方喆.

import com.example.onlineshop.model.MailMessage;
import com.example.onlineshop.repository.MailRepository;
import jakarta.mail.internet.MimeMessage;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final MailRepository mails;
    private final JavaMailSender sender;
    private final String mode;
    private final Path directory;
    private final String from;

    public MailService(MailRepository mails, JavaMailSender sender,
                       @Value("${shop.mail.mode:file}") String mode,
                       @Value("${shop.mail.directory:./data/mail}") String directory,
                       @Value("${shop.mail.from:campus-shop@example.com}") String from) {
        this.mails = mails;
        this.sender = sender;
        this.mode = mode;
        this.directory = Path.of(directory);
        this.from = from;
    }

    public void deliver(long mailId) {
        if (!mails.claim(mailId)) {
            return;
        }
        MailMessage mail = mails.findById(mailId).orElseThrow();
        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(mail.recipient());
            helper.setSubject(mail.subject());
            helper.setText(mail.body());
            helper.setSentDate(new Date());
            message.saveChanges();

            if ("smtp".equalsIgnoreCase(mode)) {
                sender.send(message);
                mails.markSent(mailId);
            } else {
                Files.createDirectories(directory);
                try (OutputStream out = Files.newOutputStream(
                        directory.resolve("mail-" + mailId + ".eml"))) {
                    message.writeTo(out);
                }
                mails.markSaved(mailId);
            }
        } catch (Exception exception) {
            mails.markFailed(mailId, exception.getClass().getSimpleName());
        }
    }
}