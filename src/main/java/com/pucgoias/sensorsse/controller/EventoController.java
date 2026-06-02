package com.pucgoias.sensorsse.controller;

import com.pucgoias.sensorsse.service.EventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

/**
 * Controller responsável pelo endpoint SSE de sensores de temperatura.
 *
 * <p>Expõe dois endpoints:
 * <ul>
 *   <li>{@code GET /api/sensores/stream} — abre a conexão SSE</li>
 *   <li>{@code GET /api/sensores/status} — retorna o número de clientes conectados</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/sensores")
public class EventoController {

    private static final Logger log = LoggerFactory.getLogger(EventoController.class);

    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    /**
     * Abre uma conexão SSE persistente para receber eventos de temperatura.
     *
     * <p>O header {@code Last-Event-ID} é enviado automaticamente pelo browser
     * em reconexões, permitindo retransmissão de eventos perdidos (não implementado
     * nesta versão — ponto de extensão futuro com Redis ou banco de dados).
     *
     * @param lastEventId ID do último evento recebido pelo cliente (opcional)
     * @return {@link SseEmitter} com timeout infinito (-1L)
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter stream(
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {

        if (lastEventId != null) {
            log.info("Reconexão de cliente com Last-Event-ID={}", lastEventId);
        }

        // -1L = sem timeout (conexão mantida até o cliente desconectar)
        SseEmitter emitter = new SseEmitter(-1L);

        eventoService.registrar(emitter);

        // Callbacks de limpeza: garante remoção do emitter em qualquer cenário de encerramento
        emitter.onCompletion(() -> eventoService.remover(emitter));
        emitter.onTimeout(()     -> eventoService.remover(emitter));
        emitter.onError((ex)     -> eventoService.remover(emitter));

        return emitter;
    }

    /**
     * Retorna informações sobre o estado atual do servidor SSE.
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
                "clientesConectados", eventoService.totalConectados(),
                "status", "online"
        ));
    }
}
