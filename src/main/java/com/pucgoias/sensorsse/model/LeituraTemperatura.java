package com.pucgoias.sensorsse.model;

/**
 * Representa uma leitura de temperatura de um sensor.
 *
 * @param sensor    Identificador do sensor (ex.: "sala", "server", "externo")
 * @param valor     Temperatura lida em graus Celsius
 * @param timestamp Momento da leitura em milissegundos (epoch)
 */
public record LeituraTemperatura(
        String sensor,
        double valor,
        long timestamp
) {
    /**
     * Cria uma leitura com o timestamp atual.
     */
    public static LeituraTemperatura agora(String sensor, double valor) {
        return new LeituraTemperatura(sensor, valor, System.currentTimeMillis());
    }
}
