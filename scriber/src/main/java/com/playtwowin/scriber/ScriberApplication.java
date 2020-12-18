package com.playtwowin.scriber;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import com.playtowin.repository.AdvisorRepo;
import com.playtowin.repository.AffiliateRepo;

@SpringBootApplication(scanBasePackages = "com.playtwowin")
@EnableSwagger2
//@EnableJpaRepositories(basePackageClasses = UserRepository.class)
@EnableJpaRepositories(basePackageClasses = {AdvisorRepo.class, AffiliateRepo.class} )
@EntityScan(basePackages = "com.playtwowin")
public class ScriberApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScriberApplication.class, args);
	}

}
