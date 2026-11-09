package br.com.veridoc.api.decisao;

import br.com.veridoc.api.analise.AnaliseService;
import br.com.veridoc.api.analise.DadosExtraidos;
import br.com.veridoc.api.auditoria.AuditoriaService;
import br.com.veridoc.api.auditoria.TipoEventoAuditoria;
import br.com.veridoc.api.compartilhado.RecursoNaoEncontradoException;
import br.com.veridoc.api.decisao.regras.ContextoAvaliacao;
import br.com.veridoc.api.decisao.regras.MotorDeRisco;
import br.com.veridoc.api.documento.DocumentoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DecisaoService {

    private final DocumentoService documentoService;
    private final AnaliseService analiseService;
    private final DecisaoRepository decisoes;
    private final MotorDeRisco motor;
    private final AuditoriaService auditoria;
    private final Clock clock;

    DecisaoService(DocumentoService documentoService, AnaliseService analiseService, DecisaoRepository decisoes,
                   MotorDeRisco motor, AuditoriaService auditoria, Clock clock) {
        this.documentoService = documentoService;
        this.analiseService = analiseService;
        this.decisoes = decisoes;
        this.motor = motor;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    /**
     * Registra o resultado da extração e decide. Tudo numa transação: troca de status,
     * análise, decisão e auditoria são gravadas juntas ou nada é gravado.
     * O documento é concluído primeiro: em chamadas simultâneas, a segunda falha ali,
     * no lock otimista, antes de tentar gravar qualquer outra coisa.
     */
    @Transactional
    public Decisao registrarAnalise(UUID documentoId, DadosExtraidos dados) {
        var declarados = documentoService.concluirAnalise(documentoId);
        analiseService.registrar(documentoId, dados);
        Instant agora = Instant.now(clock);

        var contexto = new ContextoAvaliacao(declarados.tipo(), declarados.cpfDeclarado(),
                declarados.nomeDeclarado(), dados, LocalDate.now(clock));
        var avaliacao = motor.avaliar(contexto);
        var decisao = decisoes.save(new Decisao(documentoId, avaliacao.resultado(), avaliacao.motivos(), agora));

        String codigos = avaliacao.motivos().stream().map(Motivo::codigo).collect(Collectors.joining(", "));
        auditoria.registrar(documentoId, TipoEventoAuditoria.DECISAO_TOMADA,
                "%s (%s)".formatted(avaliacao.resultado(), codigos));
        return decisao;
    }

    @Transactional(readOnly = true)
    public Decisao buscarPorDocumento(UUID documentoId) {
        var documento = documentoService.buscar(documentoId);
        return decisoes.findByDocumentoId(documentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Documento %s ainda não tem decisão (status %s)".formatted(documentoId, documento.getStatus())));
    }
}
