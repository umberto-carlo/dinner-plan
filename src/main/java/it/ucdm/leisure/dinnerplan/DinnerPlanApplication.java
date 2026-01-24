package it.ucdm.leisure.dinnerplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@org.springframework.scheduling.annotation.EnableAsync
@EnableScheduling
@SpringBootApplication
public class DinnerPlanApplication {

	public static void main(String[] args) {
		SpringApplication.run(DinnerPlanApplication.class, args);
	}

}
