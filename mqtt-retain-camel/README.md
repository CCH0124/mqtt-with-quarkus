# mqtt-retain-camel

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using: 
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/mqtt-camel-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- Camel Log ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/log.html)): Log messages to the underlying logging mechanism
- Camel Core ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html)): Camel core functionality and basic Camel languages: Constant, ExchangeProperty, Header, Ref, Simple and Tokenize
- Camel Paho MQTT5 ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/paho-mqtt5.html)): Communicate with MQTT message brokers using Eclipse Paho MQTT v5 Client
- Camel Timer ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/timer.html)): Generate messages in specified intervals using java.util.Timer

### 保留訊息
將某個特定主題下發布的消息標記為*保留訊息*，並將其儲存為 EMQX 上的持久訊息。當任何新的訂閱者訂閱與保留訊息的 topic 相符的 topic 時，會立即接收到該訊息，即使該訊息是在它們訂閱該主題之前發布的。

建立一個 `itachi` 的客戶端作為*發布者*。且將 `retained` 設置為 `true`。
```java
pahoMqtt5(mqttConfig.topic())
                .clientId("itachi")
                .automaticReconnect(true)
                .cleanStart(true)
                .keepAliveInterval(60)
                .qos(0)
                .retained(true)
                .brokerUrl(mqttConfig.brokerUrl())
```

並向 `itachi/status` 發送訊息。

此時運行此專案其會發送訊息至 `itachi/status` 的 topic，如下：

```bash
...
2023-11-18 20:02:57,553 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":28,"temp":71}.
> :qu2023-11-18 20:02:59,551 INFO  [com.cch.rou.MqttRouter] (Camel (camel-2) thread #2 - timer://generate-payload) Sending Device: itachi
2023-11-18 20:02:59,553 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":9,"temp":45}.
> :qu2023-11-18 20:03:01,551 INFO  [com.cch.rou.MqttRouter] (Camel (camel-2) thread #2 - timer://generate-payload) Sending Device: itachi
2023-11-18 20:03:01,553 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":30,"temp":60}.
> :qu2023-11-18 20:03:03,551 INFO  [com.cch.rou.MqttRouter] (Camel (camel-2) thread #2 - timer://generate-payload) Sending Device: itachi
2023-11-18 20:03:03,553 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":40,"temp":81}.
> :qu2023-11-18 20:03:05,551 INFO  [com.cch.rou.MqttRouter] (Camel (camel-2) thread #2 - timer://generate-payload) Sending Device: itachi
2023-11-18 20:03:05,552 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":28,"temp":47}.
> :qu2023-11-18 20:03:07,551 INFO  [com.cch.rou.MqttRouter] (Camel (camel-2) thread #2 - timer://generate-payload) Sending Device: itachi
2023-11-18 20:03:07,552 INFO  [qos-test] (Camel (camel-2) thread #2 - timer://generate-payload) send message: {"deviceName":"itachi","humidity":20,"temp":97}.
```

最後一筆將成為該 `itachi/status` topic 的保留訊息。 


此時使用 `mqttx` cli 方式進行訂閱

```bash
% mqttx sub -t itachi/status
[11/18/2023] [8:26:56 PM] › …  Connecting...
[11/18/2023] [8:26:56 PM] › ✔  Connected
[11/18/2023] [8:26:56 PM] › …  Subscribing to itachi/status...
[11/18/2023] [8:26:56 PM] › ✔  Subscribed to itachi/status
[11/18/2023] [8:26:56 PM] › payload: {"deviceName":"itachi","humidity":20,"temp":97}
retain: true
```

看到 `subscriber` 只有收到最後一筆訊息，因為 EMQX 僅儲存每個主題的最新保留訊息。

如果將 `retained` 設定為 `false`，則不會有任何訊息被 EMQX 保留。