package vn.edu.iuh.fit.olachatbackend.services;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.exceptions.BadRequestException;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;
import vn.edu.iuh.fit.olachatbackend.repositories.SubscriberRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmailService_Weather {
    private final JavaMailSender mailSender;
    private final SubscriberRepository subscriberRepository;

    public void subscribe(String to, String subject, Subscriber s) {
        if (subscriberRepository.findByEmail(s.getEmail()).isPresent()) {
            throw new BadRequestException("Email already registered!");
        }
        subscriberRepository.save(s);

        String content = "Chúc mừng bạn đã đăng ký nhận thông báo thời tiết thành công!\n"
                + "Bạn sẽ nhận bản tin thời tiết vào 7h sáng hàng ngày.\n"
                + "Nếu muốn huỷ, hãy sử dụng chức năng hủy đăng ký trên ứng dụng/web.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }

    public void unsubscribe(String email) {
        Optional<Subscriber> s = subscriberRepository.findByEmail(email);
        if (s.isEmpty()) {
            throw new BadRequestException ("Email not found!");
        }
        subscriberRepository.delete(s.get());
    }

    public void sendWeatherEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);
        mailSender.send(message);
    }
}
