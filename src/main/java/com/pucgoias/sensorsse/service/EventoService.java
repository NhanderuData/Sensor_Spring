package com.pucgoias.sensorsse.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Serviço central de gerenciamento e publicação de eventos SSE.
 *
 * <p>Mantém a lista de {@link SseEmitter} ativos de forma thread-safe
 * e publica eventos assincronamente para todos os clientes conectados.
 *
 * <p>Emitters com conexão encerrada são detectados na tentativa de envio
 * e removidos automaticamente, evitando vazamento de memória.
 */
@Service
public class EventoService {

    private static final Logger log = LoggerFactory.getLogger(EventoService.class);

    // CopyOnWriteArrayList garante thread-safety em leituras concorrentes
    private final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

    /**
     * Registra um novo emitter (cliente conectado).
     */
    public void registrar(SseEmitter emitter) {
        emitters.add(emitter);
        log.info("Cliente conectado. Total de conexões ativas: {}", emitters.size());
    }

    /**
     * Remove um emitter (cliente desconectado).
     */
    public void remover(SseEmitter emitter) {
        emitters.remove(emitter);
        log.info("Cliente desconectado. Total de conexões ativas: {}", emitters.size());
    }

    /**
     * Publica um evento para todos os clientes conectados.
     *
     * <p>Executado de forma assíncrona para não bloquear a thread HTTP.
     * Emitters que falharem no envio são coletados e removidos em lote.
     *
     * @param tipo    Nome do evento (campo {@code event} no protocolo SSE)
     * @param payload Objeto a ser serializado como JSON no campo {@code data}
     */
    @Async
    public void publicar(String tipo, Object payload) {
        SseEmitter.SseEventBuilder evento = SseEmitter.event()
                .id(String.valueOf(System.currentTimeMillis()))
                .name(tipo)
                .data(payload)
                .reconnectTime(3000);

        List<SseEmitter> mortos = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(evento);
            } catch (IOException | IllegalStateException e) {
                // Conexão encerrada — marcar para remoção
                mortos.add(emitter);
                log.debug("Emitter encerrado removido: {}", e.getMessage());
            }
        }

        if (!mortos.isEmpty()) {
            emitters.removeAll(mortos);
            log.info("{} emitter(s) inativo(s) removido(s). Ativos: {}", mortos.size(), emitters.size());
        }
    }

    /**
     * Envia um heartbeat (comentário SSE) para manter conexões ativas em proxies.
     */
    @Async
    public void heartbeat() {
        SseEmitter.SseEventBuilder hb = SseEmitter.event().comment("heartbeat");
        List<SseEmitter> mortos = new ArrayList<>();

        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(hb);
            } catch (Exception e) {
                mortos.add(emitter);
            }
        }

        emitters.removeAll(mortos);
    }

    /**
     * Retorna o número de clientes atualmente conectados.
     */
    public int totalConectados() {
        return emitters.size();
    }
}
