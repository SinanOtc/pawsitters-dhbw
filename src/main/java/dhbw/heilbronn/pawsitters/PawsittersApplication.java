package dhbw.heilbronn.pawsitters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduling aktiviert für den täglichen CareRequestExpiryScheduler.
 * Schließt abgelaufene CareRequests täglich + einmalig beim start.
 */
@SpringBootApplication
@EnableScheduling
public class PawsittersApplication {

	public static void main(String[] args) {
		SpringApplication.run(PawsittersApplication.class, args);
	}

}
