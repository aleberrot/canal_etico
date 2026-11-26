package com.pantteon.canal_etico.infra;
import java.time.Clock;
import org.springframework.context.annotation.*;
@Configuration
public class TimeConfiguration { @Bean public Clock clock(){return Clock.systemUTC();} }

