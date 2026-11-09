package br.com.veridoc.api.documento;

import java.util.Set;

/**
 * Ciclo de vida de um documento. RECEBIDO → CONCLUIDO direto existe enquanto não há Kafka
 * (Etapa 4): hoje o resultado da análise chega pelo endpoint de simulação.
 */
public enum StatusDocumento {
    RECEBIDO,
    EM_ANALISE,
    CONCLUIDO;

    public boolean podeIrPara(StatusDocumento destino) {
        return switch (this) {
            case RECEBIDO -> Set.of(EM_ANALISE, CONCLUIDO).contains(destino);
            case EM_ANALISE -> destino == CONCLUIDO;
            case CONCLUIDO -> false;
        };
    }
}
