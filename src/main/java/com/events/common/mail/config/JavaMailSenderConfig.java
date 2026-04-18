package com.events.common.mail.config;

import com.events.common.config.properties.AppProperties;
import com.events.common.config.properties.MailProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@RequiredArgsConstructor
public class JavaMailSenderConfig {
    private final MailProperties mailProperties;

//    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.host());
        mailSender.setPort(mailProperties.port());

        mailSender.setUsername(mailProperties.username());
        mailSender.setPassword(mailProperties.password());

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", mailProperties.protocol());
        props.put("mail.smtp.auth", mailProperties.properties().mail().smtp().auth());
        props.put("mail.smtp.starttls.enable", mailProperties.properties().mail().smtp().starttlsEnable());
        props.put("mail.debug", "true");

        return mailSender;
    }

}
