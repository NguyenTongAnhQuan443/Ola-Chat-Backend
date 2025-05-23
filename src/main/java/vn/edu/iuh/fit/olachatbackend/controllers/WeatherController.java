package vn.edu.iuh.fit.olachatbackend.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.iuh.fit.olachatbackend.services.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    @Autowired
    private WeatherService weatherService;

    // Lấy theo lat, lng
    @GetMapping
    public ResponseEntity<?> getWeather(
            @RequestParam(required = false) Double lat,
            @RequestParam(required = false) Double lng,
            @RequestParam(required = false) String city) {

        if (city != null) {
            return ResponseEntity.ok(weatherService.getWeatherByCity(city));
        }
        if (lat != null && lng != null) {
            return ResponseEntity.ok(weatherService.getWeatherSummary(lat, lng));
        }
        return ResponseEntity.badRequest().body("Missing coordinates or city name");
    }
}
