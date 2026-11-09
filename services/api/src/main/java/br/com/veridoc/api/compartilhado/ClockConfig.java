package br.com.veridoc.api.compartilhado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.Duration;
import java.time.ZoneId;

/**
 * Um único relógio injetável: regras e serviços não chamam now() direto, e os testes fixam a data.
 * Avança em microssegundos, a precisão do timestamptz do PostgreSQL, para que o valor devolvido
 * na resposta seja o mesmo que fica gravado.
 */
@Configuration
public class ClockConfig {

    @Bean
    Clock clock(@Value("${veridoc.fuso-horario}") ZoneId fusoHorario) {
        return Clock.tick(Clock.system(fusoHorario), Duration.ofNanos(1_000));
    }
}
