package vn.edu.iuh.fit.olachatbackend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);
    
    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        GoogleCredentials credentials;
        
        // Try to load from environment variable location first (for Render)
        String credentialPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialPath != null && !credentialPath.isEmpty()) {
            File credentialFile = new File(credentialPath);
            if (credentialFile.exists()) {
                logger.info("Loading Firebase credentials from: {}", credentialPath);
                try (FileInputStream serviceAccount = new FileInputStream(credentialFile)) {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
            } else {
                // Fall back to classpath resource (for local development)
                logger.info("File specified by GOOGLE_APPLICATION_CREDENTIALS not found, falling back to classpath resource");
                try (InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream()) {
                    credentials = GoogleCredentials.fromStream(serviceAccount);
                }
            }
        } else {
            // Fall back to classpath resource (for local development)
            logger.info("Loading Firebase credentials from classpath resource");
            try (InputStream serviceAccount = new ClassPathResource("serviceAccountKey.json").getInputStream()) {
                credentials = GoogleCredentials.fromStream(serviceAccount);
            }
        }

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(options);
        } else {
            return FirebaseApp.getInstance();
        }
    }

    @Bean
    public FirebaseMessaging firebaseMessaging(FirebaseApp firebaseApp) {
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}