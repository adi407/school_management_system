package com.sms.api.service;

import com.sms.api.config.EmailProperties;
import com.sms.api.entity.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final JavaMailSender mailSender;
    private final EmailProperties props;

    public EmailService(JavaMailSender mailSender, EmailProperties props) {
        this.mailSender = mailSender;
        this.props = props;
    }

    // ── Demo request: confirmation to lead ──────────────────────────────────

    @Async
    public void sendDemoConfirmation(DemoRequest demo) {
        String html = wrap(
            "We've received your demo request",
            "<p>Hi " + esc(demo.getName()) + ",</p>"
            + "<p>Thank you for your interest in SchoolManager! We've received your demo request and will get back to you within 24 hours.</p>"
            + detail("School", demo.getSchoolName())
            + detail("Role", demo.getRole())
            + detail("City", demo.getCity())
            + "<p style='margin-top:24px'>In the meantime, feel free to explore our <a href='" + props.frontendUrl() + "/pricing' style='color:#2563EB'>pricing plans</a>.</p>"
            + "<p>— The SchoolManager Team</p>"
        );
        send(demo.getEmail(), "We've received your demo request — SchoolManager", html);
    }

    // ── Demo request: notification to admin ─────────────────────────────────

    @Async
    public void sendDemoNotification(DemoRequest demo) {
        String html = wrap(
            "New Demo Request",
            "<p>A new demo request has been submitted:</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Name", demo.getName())
            + row("Email", demo.getEmail())
            + row("Phone", demo.getPhone())
            + row("School", demo.getSchoolName())
            + row("City", demo.getCity())
            + row("Role", demo.getRole())
            + row("Message", demo.getMessage())
            + "</table>"
        );
        send(props.adminTo(), "New Demo Request from " + esc(demo.getName()), html);
    }

    // ── Password reset ──────────────────────────────────────────────────────

    @Async
    public void sendPasswordReset(String email, String name, String token) {
        String resetUrl = props.frontendUrl() + "/reset-password?token=" + token;
        String html = wrap(
            "Reset your password",
            "<p>Hi" + (name != null ? " " + esc(name) : "") + ",</p>"
            + "<p>We received a request to reset your password. Click the button below to set a new one:</p>"
            + button("Reset Password", resetUrl)
            + "<p style='margin-top:24px;font-size:13px;color:#71717A'>This link expires in 30 minutes. If you didn't request this, you can safely ignore this email.</p>"
        );
        send(email, "Reset your password — SchoolManager", html);
    }

    // ── Welcome email for new users ─────────────────────────────────────────

    @Async
    public void sendWelcome(User user, String schoolName) {
        String loginUrl = props.frontendUrl() + "/login";
        String html = wrap(
            "Welcome to SchoolManager",
            "<p>Hi " + esc(user.getFullName()) + ",</p>"
            + "<p>Your account has been created" + (schoolName != null ? " for <strong>" + esc(schoolName) + "</strong>" : "") + ".</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Email", user.getEmail())
            + row("Role", user.getRole().name().replace("ROLE_", ""))
            + "</table>"
            + "<p>Sign in with your email and the temporary password provided by your administrator.</p>"
            + button("Sign In", loginUrl)
        );
        send(user.getEmail(), "Welcome to SchoolManager", html);
    }

    // ── Fee payment receipt ─────────────────────────────────────────────────

    @Async
    public void sendFeeReceipt(List<Guardian> guardians, Student student, FeePayment payment) {
        String html = wrap(
            "Fee Payment Receipt",
            "<p>Dear Parent/Guardian,</p>"
            + "<p>This is to confirm a fee payment for <strong>" + esc(student.getFullName()) + "</strong>.</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Student", student.getFullName())
            + row("Admission No", student.getAdmissionNo())
            + row("Amount Paid", "₹" + payment.getAmountPaid().toPlainString())
            + row("Payment Mode", payment.getPaymentMode())
            + row("Receipt No", payment.getReceiptNo())
            + row("Payment Date", payment.getPaymentDate().format(DATE_FMT))
            + (payment.getFeeStructure() != null ? row("Fee Type", payment.getFeeStructure().getFeeType()) : "")
            + "</table>"
            + "<p style='font-size:13px;color:#71717A'>Please keep this email as your payment receipt.</p>"
        );
        String subject = "Fee Receipt — ₹" + payment.getAmountPaid().toPlainString() + " for " + student.getFullName();
        sendToGuardians(guardians, subject, html);
    }

    // ── Fee reminder ────────────────────────────────────────────────────────

    @Async
    public void sendFeeReminder(List<Guardian> guardians, Student student,
                                String feeType, BigDecimal balance, LocalDate dueDate) {
        String dueLine = dueDate != null ? "<p>Due date: <strong>" + dueDate.format(DATE_FMT) + "</strong></p>" : "";
        String html = wrap(
            "Fee Reminder",
            "<p>Dear Parent/Guardian,</p>"
            + "<p>This is a reminder regarding pending fees for <strong>" + esc(student.getFullName()) + "</strong>.</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Student", student.getFullName())
            + row("Fee Type", feeType)
            + row("Pending Amount", "₹" + balance.toPlainString())
            + "</table>"
            + dueLine
            + "<p>Please arrange for payment at the earliest convenience. Contact the school office for any queries.</p>"
        );
        String subject = "Fee Reminder — ₹" + balance.toPlainString() + " pending for " + student.getFullName();
        sendToGuardians(guardians, subject, html);
    }

    // ── Attendance alert ────────────────────────────────────────────────────

    @Async
    public void sendAttendanceAlert(List<Guardian> guardians, Student student,
                                     LocalDate date, String status) {
        String statusLabel = switch (status) {
            case "ABSENT" -> "absent";
            case "LATE" -> "marked late";
            case "EXCUSED" -> "excused";
            default -> status.toLowerCase();
        };
        String className = student.getSchoolClass() != null ? student.getSchoolClass().getName() : "";
        String html = wrap(
            "Attendance Update",
            "<p>Dear Parent/Guardian,</p>"
            + "<p>This is to inform you that <strong>" + esc(student.getFullName()) + "</strong>"
            + " was <strong>" + statusLabel + "</strong> on " + date.format(DATE_FMT) + ".</p>"
            + "<table style='width:100%;border-collapse:collapse;margin:16px 0'>"
            + row("Student", student.getFullName())
            + row("Class", className)
            + row("Date", date.format(DATE_FMT))
            + row("Status", status)
            + "</table>"
            + "<p>If you have any concerns, please contact the school office.</p>"
        );
        String subject = "Attendance: " + student.getFullName() + " was " + statusLabel + " on " + date.format(DATE_FMT);
        sendToGuardians(guardians, subject, html);
    }

    // ── Internal helpers ────────────────────────────────────────────────────

    private void sendToGuardians(List<Guardian> guardians, String subject, String html) {
        guardians.stream()
            .filter(g -> g.getEmail() != null && !g.getEmail().isBlank())
            .forEach(g -> send(g.getEmail(), subject, html));
    }

    private void send(String to, String subject, String html) {
        if (!props.enabled()) {
            log.info("Email disabled — would send to={} subject={}", to, subject);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
            helper.setFrom(props.from());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            mailSender.send(msg);
            log.info("Email sent to={} subject={}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send email to={} subject={}", to, subject, e);
        }
    }

    private static String wrap(String heading, String body) {
        return """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <style>body{margin:0;padding:0;background:#F4F4F5;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif}
            .outer{padding:32px 16px}.inner{max-width:560px;margin:0 auto;background:#fff;border-radius:12px;overflow:hidden;box-shadow:0 1px 3px rgba(0,0,0,.08)}
            .header{background:linear-gradient(135deg,#2563EB,#059669);padding:24px 32px;text-align:center}
            .header h1{margin:0;color:#fff;font-size:18px;font-weight:700;letter-spacing:-0.3px}
            .body{padding:32px;color:#18181B;font-size:15px;line-height:1.7}
            .body p{margin:0 0 14px}
            .footer{padding:20px 32px;text-align:center;font-size:12px;color:#A1A1AA;border-top:1px solid #F4F4F5}
            .footer a{color:#71717A;text-decoration:none}</style></head>
            <body><div class="outer"><div class="inner">
            <div class="header"><h1>""" + esc(heading) + """
            </h1></div>
            <div class="body">""" + body + """
            </div>
            <div class="footer">
            <p><strong>SchoolManager</strong> — Modern school management for Indian schools</p>
            <p><a href="https://schoolmanager.live">schoolmanager.live</a> &middot; <a href="mailto:support@schoolmanager.live">support@schoolmanager.live</a></p>
            </div></div></div></body></html>""";
    }

    private static String button(String label, String url) {
        return "<div style='text-align:center;margin:24px 0'>"
            + "<a href='" + url + "' style='display:inline-block;padding:12px 32px;background:#2563EB;color:#fff;"
            + "text-decoration:none;border-radius:8px;font-weight:700;font-size:15px'>"
            + esc(label) + "</a></div>";
    }

    private static String row(String label, String value) {
        if (value == null || value.isBlank()) return "";
        return "<tr><td style='padding:8px 12px;border-bottom:1px solid #F4F4F5;font-weight:600;color:#3F3F46;width:140px'>"
            + esc(label) + "</td><td style='padding:8px 12px;border-bottom:1px solid #F4F4F5;color:#18181B'>"
            + esc(value) + "</td></tr>";
    }

    private static String detail(String label, String value) {
        if (value == null || value.isBlank()) return "";
        return "<p style='margin:4px 0;font-size:14px'><strong>" + esc(label) + ":</strong> " + esc(value) + "</p>";
    }

    private static String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
