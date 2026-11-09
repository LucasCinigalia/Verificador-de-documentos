package br.com.veridoc.api.analise;

import br.com.veridoc.api.documento.TipoDocumento.Campo;
import jakarta.validation.Valid;

/**
 * Campos extraídos de um documento. Datas vêm no formato ISO (yyyy-MM-dd).
 * Campos podem ser nulos: um campo que o agente não encontrou é informação para as regras.
 */
public record DadosExtraidos(
        @Valid CampoExtraido nome,
        @Valid CampoExtraido cpf,
        @Valid CampoExtraido dataNascimento,
        @Valid CampoExtraido dataValidade) {

    public CampoExtraido campo(Campo campo) {
        return switch (campo) {
            case NOME -> nome;
            case CPF -> cpf;
            case DATA_NASCIMENTO -> dataNascimento;
            case DATA_VALIDADE -> dataValidade;
        };
    }
}
