package br.com.veridoc.api.documento;

import java.util.UUID;

/**
 * O que outros domínios podem saber de um documento para avaliá-lo: o tipo e o que a empresa
 * declarou sobre o titular. É uma cópia imutável, e não a entidade, para que ninguém fora do
 * pacote documento consiga alterar o status sem passar pelo DocumentoService.
 */
public record DadosDeclarados(UUID documentoId, TipoDocumento tipo, String cpfDeclarado, String nomeDeclarado) {
}
