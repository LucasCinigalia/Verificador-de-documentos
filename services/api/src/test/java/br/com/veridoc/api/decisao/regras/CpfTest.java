package br.com.veridoc.api.decisao.regras;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class CpfTest {

    @ParameterizedTest
    @ValueSource(strings = {"52998224725", "529.982.247-25", "12345678909"})
    void aceitaCpfComDigitosVerificadoresCorretos(String cpf) {
        assertThat(Cpf.valido(cpf)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"52998224724", "1234567890", "111.111.111-11", "00000000000", "abc"})
    void rejeitaCpfInvalido(String cpf) {
        assertThat(Cpf.valido(cpf)).isFalse();
    }
}
