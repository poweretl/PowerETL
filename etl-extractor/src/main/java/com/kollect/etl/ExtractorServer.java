package com.kollect.etl;

import com.kollect.etl.service.TransferService;
import com.kollect.etl.service.TransferServiceImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
public class Server {

  public static void main(String[] args) {
    SpringApplication.run(Server.class, args);
  }

}