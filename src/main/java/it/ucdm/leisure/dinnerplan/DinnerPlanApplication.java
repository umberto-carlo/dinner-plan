package it.ucdm.leisure.dinnerplan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@org.springframework.scheduling.annotation.EnableAsync
@SpringBootApplication
public class DinnerPlanApplication {

	public static void main(String[] args) {
		SpringApplication.run(DinnerPlanApplication.class, args);
	}

}
