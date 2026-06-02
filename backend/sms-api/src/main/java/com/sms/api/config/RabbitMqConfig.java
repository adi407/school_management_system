package com.sms.api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    // Exchanges
    public static final String NOTIFICATIONS_EXCHANGE = "sms.notifications";
    public static final String JOBS_EXCHANGE          = "sms.jobs";

    // Queues
    public static final String EMAIL_QUEUE            = "sms.email";
    public static final String SMS_QUEUE              = "sms.sms";
    public static final String PUSH_QUEUE             = "sms.push";
    public static final String PDF_GEN_QUEUE          = "sms.pdf-gen";
    public static final String BULK_IMPORT_QUEUE      = "sms.bulk-import";
    public static final String DOC_EXPORT_QUEUE       = "sms.doc-export";
    public static final String ATTENDANCE_ALERT_QUEUE = "sms.attendance-alert";
    public static final String FEE_REMINDER_QUEUE     = "sms.fee-reminder";

    @Bean TopicExchange notificationsExchange() {
        return new TopicExchange(NOTIFICATIONS_EXCHANGE, true, false);
    }

    @Bean DirectExchange jobsExchange() {
        return new DirectExchange(JOBS_EXCHANGE, true, false);
    }

    @Bean Queue emailQueue()     { return QueueBuilder.durable(EMAIL_QUEUE).build(); }
    @Bean Queue smsQueue()       { return QueueBuilder.durable(SMS_QUEUE).build(); }
    @Bean Queue pushQueue()      { return QueueBuilder.durable(PUSH_QUEUE).build(); }
    @Bean Queue pdfGenQueue()    { return QueueBuilder.durable(PDF_GEN_QUEUE).build(); }
    @Bean Queue bulkImportQueue(){ return QueueBuilder.durable(BULK_IMPORT_QUEUE).build(); }
    @Bean Queue docExportQueue() { return QueueBuilder.durable(DOC_EXPORT_QUEUE).build(); }
    @Bean Queue attendanceAlertQueue() { return QueueBuilder.durable(ATTENDANCE_ALERT_QUEUE).build(); }
    @Bean Queue feeReminderQueue()     { return QueueBuilder.durable(FEE_REMINDER_QUEUE).build(); }

    @Bean Binding emailBinding()    { return BindingBuilder.bind(emailQueue()).to(notificationsExchange()).with("notification.email"); }
    @Bean Binding smsBinding()      { return BindingBuilder.bind(smsQueue()).to(notificationsExchange()).with("notification.sms"); }
    @Bean Binding pushBinding()     { return BindingBuilder.bind(pushQueue()).to(notificationsExchange()).with("notification.push"); }
    @Bean Binding pdfGenBinding()   { return BindingBuilder.bind(pdfGenQueue()).to(jobsExchange()).with(PDF_GEN_QUEUE); }
    @Bean Binding bulkImportBinding(){ return BindingBuilder.bind(bulkImportQueue()).to(jobsExchange()).with(BULK_IMPORT_QUEUE); }
    @Bean Binding docExportBinding(){ return BindingBuilder.bind(docExportQueue()).to(jobsExchange()).with(DOC_EXPORT_QUEUE); }
    @Bean Binding attendanceBinding(){ return BindingBuilder.bind(attendanceAlertQueue()).to(jobsExchange()).with(ATTENDANCE_ALERT_QUEUE); }
    @Bean Binding feeReminderBinding(){ return BindingBuilder.bind(feeReminderQueue()).to(jobsExchange()).with(FEE_REMINDER_QUEUE); }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf) {
        RabbitTemplate template = new RabbitTemplate(cf);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
