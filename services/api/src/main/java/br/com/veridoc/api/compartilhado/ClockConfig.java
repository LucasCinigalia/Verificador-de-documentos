package br.com.veridoc.api.compartilhado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Um único relógio injetável: regras e serviços não chamam now() direto, e os testes fixam a data. */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${veridoc.fuso-horario}") ZoneId fusoHorario) {
        return Clock.system(fusoHorario);
    }
}
