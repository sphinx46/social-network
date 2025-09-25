package ru.vsu.cs.OOP.mordvinovil.task2.social_network;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CommunicationNetworkApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommunicationNetworkApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}

