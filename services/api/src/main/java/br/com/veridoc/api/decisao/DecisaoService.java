package br.com.veridoc.api.decisao;

import br.com.veridoc.api.analise.Analise;
import br.com.veridoc.api.analise.AnaliseRepository;
import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import br.com.veridoc.api.decisao.regras.ContextoAvaliacao;
import br.com.veridoc.api.decisao.regras.MotorDeRisco;
import br.com.veridoc.api.documento.DocumentoRepository;
import br.com.veridoc.api.documento.StatusDocumento;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DecisaoService {

    private final DocumentoRepository documentos;
    private final AnaliseRepository analises;
    private final DecisaoRepository decisoes;
    private final MotorDeRisco motor;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public DecisaoService(DocumentoRepository documentos, AnaliseRepository analises, DecisaoRepository decisoes,
                          MotorDeRisco motor, AuditoriaService auditoria, Clock clock) {
        this.documentos = documentos;
        this.analises = analises;
        this.decisoes = decisoes;
        this.motor = motor;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    /**
     * Registra o resultado da extração e decide. Tudo numa transação: análise, decisão,
     * troca de status e auditoria são gravadas juntas ou nada é gravado.
     * Duas chamadas simultâneas para o mesmo documento: a segunda falha no lock otimista
     * (versão do documento mudou) ou na unique de decisao.documento_id.
     */
    @Transactional
    public Decisao registrarAnalise(UUID documentoId, DadosExtraidos dados) {
        var documento = documentos.findById(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento %s não encontrado".formatted(documentoId)));
        Instant agora = Instant.now(clock);
        documento.alterarStatus(StatusDocumento.CONCLUIDO, agora);

        analises.save(new Analise(documentoId, dados, agora));
        auditoria.registrar(documentoId, TipoEventoAuditoria.ANALISE_REGISTRADA, "Resultado da extração registrado");

        var contexto = new ContextoAvaliacao(documento.getTipo(), documento.getCpfDeclarado(),
                documento.getNomeDeclarado(), dados, LocalDate.now(clock));
        var avaliacao = motor.avaliar(contexto);
        var decisao = decisoes.save(new Decisao(documentoId, avaliacao.resultado(), avaliacao.motivos(), agora));

        String codigos = avaliacao.motivos().stream().map(Motivo::codigo).collect(Collectors.joining(", "));
        auditoria.registrar(documentoId, TipoEventoAuditoria.DECISAO_TOMADA,
                "%s (%s)".formatted(avaliacao.resultado(), codigos));

        documentos.saveAndFlush(documento);
        return decisao;
    }

    @Transactional(readOnly = true)
    public Decisao buscarPorDocumento(UUID documentoId) {
        var documento = documentos.findById(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento %s não encontrado".formatted(documentoId)));
        return decisoes.findByDocumentoId(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Documento %s ainda não tem decisão (status %s)".formatted(documentoId, documento.getStatus())));
    }
}
