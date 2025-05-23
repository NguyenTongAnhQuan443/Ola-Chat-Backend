package vn.edu.iuh.fit.olachatbackend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import vn.edu.iuh.fit.olachatbackend.models.Subscriber;
import vn.edu.iuh.fit.olachatbackend.repositories.SubscriberRepository;

import java.util.List;

@Service
public class ScheduledWeatherEmailService {

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private WeatherService weatherService;
    @Autowired
    private EmailService_Weather emailService_Weather;

    // Cron: giây, phút, giờ, ngày, tháng, thứ
    // 0 0 7 * * * : Mỗi ngày vào 7:00:00 sáng
    @Scheduled(cron = "0 0 7 * * *")
    public void sendWeatherEmails() {
        List<Subscriber> subscribers = subscriberRepository.findAll();
        for (Subscriber s : subscribers) {
            try {
                String weather = weatherService.getWeatherSummary(s.getLatitude(), s.getLongitude());
                emailService_Weather.sendWeatherEmail(
                        s.getEmail(),
                        "Daily Weather Notification",
                        weather
                );
                System.out.println("Sent weather email to: " + s.getEmail());
            } catch (Exception ex) {
                System.err.println("Failed to send email to " + s.getEmail() + ": " + ex.getMessage());
            }
        }
    }
}
