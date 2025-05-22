//package vn.edu.iuh.fit.olachatbackend.configs;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.firebase.FirebaseApp;
//import com.google.firebase.FirebaseOptions;
//import com.google.firebase.messaging.FirebaseMessaging;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.io.ClassPathResource;
//
//import java.io.InputStream;
//
//@Configuration
//public class FirebaseConfig {
//
//    @Bean
//    public FirebaseApp firebaseApp() throws Exception {
//        InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream();
//
//        FirebaseOptions options = FirebaseOptions.builder()
//                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
//                .build();
//
//        if (FirebaseApp.getApps().isEmpty()) {
//            return FirebaseApp.initializeApp(options);
//        } else {
//            return FirebaseApp.getInstance();
//        }
//    }
//
//    @Bean
//    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
//        return FirebaseMessaging.getInstance(firebaseApp);
//    }
//}

package vn.edu.iuh.fit.olachatbackend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.*;

@Configuration
public class FirebaseConfig {

    @Bean
    public FirebaseApp firebaseApp() throws Exception {
        // Lấy chuỗi JSON từ biến môi trường
        String firebaseConfig = System.getenv("FIREBASE_CONFIG");
        if (firebaseConfig == null || firebaseConfig.isEmpty()) {
            throw new FileNotFoundException("FIREBASE_CONFIG environment variable is not set or empty");
        }

        // Ghi nội dung JSON ra file tạm thời
        File tempFile = File.createTempFile("serviceAccountKey", ".json");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(firebaseConfig);
        }

        try (FileInputStream serviceAccount = new FileInputStream(tempFile)) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                return FirebaseApp.initializeApp(options);
            } else {
                return FirebaseApp.getInstance();
            }
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
