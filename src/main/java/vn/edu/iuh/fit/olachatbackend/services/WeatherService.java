package vn.edu.iuh.fit.olachatbackend.services;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class WeatherService {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${weather.api.key}")
    private String apiKey;

    public String getWeatherByCity(String city) {
        String url = String.format(
                "http://api.weatherapi.com/v1/current.json?key=%s&q=%s",
                apiKey, city
        );
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject obj = new JSONObject(response);

            JSONObject current = obj.getJSONObject("current");
            JSONObject condition = current.getJSONObject("condition");
            String temp = current.get("temp_c").toString();
            String wind = current.get("wind_kph").toString();
            String humidity = current.get("humidity").toString();
            String text = condition.getString("text");
            String cityName = obj.getJSONObject("location").getString("name");

            return String.format(
                    "Weather for %s:\n- %s\n- Temperature: %s°C\n- Wind: %s kph\n- Humidity: %s%%",
                    cityName, text, temp, wind, humidity
            );
        } catch (Exception e) {
            return "Unable to fetch weather data for city: " + city;
        }
    }

    public String getWeatherSummary(double lat, double lng) {
        String url = String.format(
                "http://api.weatherapi.com/v1/current.json?key=%s&q=%f,%f",
                apiKey, lat, lng
        );
        try {
            String response = restTemplate.getForObject(url, String.class);
            JSONObject obj = new JSONObject(response);

            JSONObject current = obj.getJSONObject("current");
            JSONObject condition = current.getJSONObject("condition");
            String temp = current.get("temp_c").toString();
            String wind = current.get("wind_kph").toString();
            String humidity = current.get("humidity").toString();
            String text = condition.getString("text");
            String city = obj.getJSONObject("location").getString("name");

            return String.format(
                    "Weather for %s:\n- %s\n- Temperature: %s°C\n- Wind: %s kph\n- Humidity: %s%%",
                    city, text, temp, wind, humidity
            );
        } catch (Exception e) {
            return "Unable to fetch weather data.";
        }
    }
}
