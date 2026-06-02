# 🌡️ Sensor SSE Monitor

**ADS1242 — Mensageria e Streams em Aplicações**
Pontifícia Universidade Católica de Goiás — PUC Goiás

Aplicação Spring Boot que simula leituras de sensores de temperatura e as transmite em tempo real via **Server-Sent Events (SSE)** para um dashboard HTML.

---

## 📋 Pré-requisitos

| Ferramenta | Versão Mínima |
|---|---|
| Java (JDK) | 21 |
| Maven | 3.9+ |

Verifique com:
```bash
java -version
mvn -version
```

---

## 🚀 Como Executar

### 1. Clone / extraia o projeto

```bash
cd sensor-sse-app
```

### 2. Compile e inicie o servidor

```bash
mvn spring-boot:run
```

O servidor inicia em `http://localhost:8080`.

### 3. Acesse o dashboard

Abra no browser:
```
http://localhost:8080
```

---

## 📡 Endpoints da API

| Método | Endpoint | Descrição |
|---|---|---|
| `GET` | `/api/sensores/stream` | Abre a conexão SSE — retorna `text/event-stream` |
| `GET` | `/api/sensores/status` | Retorna JSON com total de clientes conectados |

### Exemplo — consumir o stream via cURL

```bash
curl -N -H "Accept: text/event-stream" http://localhost:8080/api/sensores/stream
```

Saída esperada:
```
id: 1748000000000
event: temperatura
data: {"sensor":"sala","valor":22.4,"timestamp":1748000000000}

id: 1748000000001
event: temperatura
data: {"sensor":"server","valor":41.7,"timestamp":1748000000001}
```

### Exemplo — verificar status

```bash
curl http://localhost:8080/api/sensores/status
```

```json
{"clientesConectados": 1, "status": "online"}
```

---

## 🏗️ Arquitetura do Projeto

```
src/main/java/com/pucgoias/sensorsse/
│
├── SensorSseApplication.java        # Entry point — @SpringBootApplication
│
├── config/
│   ├── AsyncConfig.java             # @EnableAsync + ThreadPoolTaskExecutor
│   └── CorsConfig.java             # Configuração global de CORS
│
├── model/
│   └── LeituraTemperatura.java      # Record: sensor, valor, timestamp
│
├── service/
│   └── EventoService.java           # Gerencia emitters + publica eventos (@Async)
│
├── controller/
│   └── EventoController.java        # GET /api/sensores/stream e /status
│
└── simulator/
    └── SensorSimulador.java         # @Scheduled — emite leituras a cada 2s

src/main/resources/
├── application.properties           # Configurações do servidor
└── static/
    └── index.html                   # Dashboard HTML com EventSource API
```

---

## ⚙️ Configurações (application.properties)

| Propriedade | Valor | Descrição |
|---|---|---|
| `server.port` | `8080` | Porta HTTP |
| `spring.mvc.async.request-timeout` | `-1` | Sem timeout para SSE |
| `logging.level.com.pucgoias` | `DEBUG` | Log detalhado da aplicação |

---

## 🌡️ Sensores Simulados

| Sensor | ID | Faixa de Temperatura |
|---|---|---|
| Sala | SNS-001 | 18 – 28 °C |
| Server | SNS-002 | 35 – 55 °C |
| Externo | SNS-003 | 10 – 40 °C |

Frequência de emissão: **a cada 2 segundos** (todos os sensores).
Heartbeat SSE: **a cada 20 segundos** (mantém conexão em proxies).

---

## 🔌 Protocolo SSE

Formato de cada evento emitido:

```
id: <timestamp-ms>
event: temperatura
data: {"sensor":"sala","valor":22.4,"timestamp":1748000000000}
retry: 3000

```

O browser reconecta automaticamente em caso de falha, enviando o header `Last-Event-ID` com o último ID recebido.

---

## 🖥️ Funcionalidades do Dashboard

- **3 cards** — um por sensor, com temperatura atual, mín/máx/média e sparkline
- **Indicador de conexão** — CONECTANDO / CONECTADO / RECONECTANDO / ENCERRADO
- **Log terminal** — stream de eventos em tempo real
- **Alerta visual** — quando a temperatura excede o limite configurado
- **Estatísticas** — total de eventos, taxa (ev/min), sensores ativos, reconexões

---

## 📚 Referências

- [W3C — Server-Sent Events Living Standard](https://www.w3.org/TR/eventsource/)
- [MDN — Using server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events)
- [Spring — SseEmitter Reference](https://docs.spring.io/spring-framework/reference/web/webmvc/mvc-ann-async.html)

---

*Prof. Rafael Leal Martins · PUC Goiás · ADS1242*
