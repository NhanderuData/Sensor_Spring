package com.pucgoias.sensorsse.simulator;

import com.pucgoias.sensorsse.model.LeituraTemperatura;
import com.pucgoias.sensorsse.service.EventoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Simula leituras periódicas de sensores de temperatura.
 *
 * <p>A cada 2 segundos, gera valores aleatórios para os sensores
 * definidos e os publica via SSE para todos os clientes conectados.
 *
 * <p>Cada sensor possui uma faixa de temperatura base realista:
 * <ul>
 *   <li><b>sala</b>: 18–28 °C (ambiente controlado)</li>
 *   <li><b>server</b>: 35–55 °C (sala de servidores)</li>
 *   <li><b>externo</b>: 10–40 °C (ambiente externo com maior variação)</li>
 * </ul>
 */
@Component
public class SensorSimulador {

    private static final Logger log = LoggerFactory.getLogger(SensorSimulador.class);

    private final EventoService eventoService;
    private final Random random = new Random();

    // Configuração dos sensores: nome, temperatura mínima, amplitude
    private static final Object[][] SENSORES = {
            {"sala",     18.0, 10.0},
            {"server",   35.0, 20.0},
            {"externo",  10.0, 30.0}
    };

    public SensorSimulador(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    /**
     * Emite leituras de todos os sensores a cada 2 segundos.
     */
    @Scheduled(fixedRate = 2000)
    public void emitirLeituras() {
        for (Object[] config : SENSORES) {
            String nome   = (String) config[0];
            double base   = (double) config[1];
            double amp    = (double) config[2];

            double valor  = base + random.nextDouble() * amp;
            double arred  = Math.round(valor * 10.0) / 10.0;

            LeituraTemperatura leitura = LeituraTemperatura.agora(nome, arred);
            eventoService.publicar("temperatura", leitura);

            log.debug("Emitido — sensor={} valor={}°C", nome, arred);
        }
    }

    /**
     * Envia heartbeat a cada 20 segundos para manter conexões abertas em proxies.
     */
    @Scheduled(fixedRate = 20000)
    public void heartbeat() {
        eventoService.heartbeat();
        log.debug("Heartbeat SSE enviado para {} cliente(s)", eventoService.totalConectados());
    }
}
