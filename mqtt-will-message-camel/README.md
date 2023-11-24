# mqtt-will-message-camel

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

You can then execute your native executable with: `./build/mqtt-will-message-camel-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- Camel Log ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/log.html)): Log messages to the underlying logging mechanism
- Camel Core ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html)): Camel core functionality and basic Camel languages: Constant, ExchangeProperty, Header, Ref, Simple and Tokenize
- Camel Direct ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/direct.html)): Call another endpoint from the same Camel Context synchronously
- Camel Paho MQTT5 ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/paho-mqtt5.html)): Communicate with MQTT message brokers using Eclipse Paho MQTT v5 Client
- Camel Timer ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/timer.html)): Generate messages in specified intervals using java.util.Timer
- Camel Jackson ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/jackson.html)): Marshal POJOs to JSON and back using Jackson

## Will Message (遺囑訊息)

只有服務端才能知道客戶端是否在線問題，*will message* 能夠為意外離線的客戶端優雅完成善後事宜。

*遺囑訊息在客戶端發起連線時指定*，它和 `Client ID`、`Clean Start`` 這些欄位一起包含在客戶端發送的 `CONNECT` 訊息中。它與一般的訊息發送一樣，可以設定 `topic`、`payload`、`QOS` 等，如下圖

![](https://assets.emqx.com/images/0dcc740dabb41cfee950b1a0d71bc304.jpg?imageMogr2/thumbnail/1520x) From EMQX office

以下任一條件滿足時發布它:
- 服務端偵測到了一個 I/O 錯誤或網路故障
- 客戶端在 Keep Alive 時間內未能通訊 
- 客戶端在沒有發送 Reason Code 為 0x00（正常關閉）的 DISCONNECT 封包的情況下關閉了網路連接 
- 服務端在未收到 Reason Code 為 0x00（正常關閉）的 DISCONNECT 封包的情況下關閉了網路連接，例如客戶端的封包或行為不符合協定要求而被服務端關閉連線

對於遺囑訊息是服務端會話狀態的一部分，當會話結束，遺囑訊息也無法繼續單獨存在。

更多的資訊可以參考官方 [blog](https://www.emqx.com/en/blog/use-of-mqtt-will-message)

定義一個有遺囑訊息的 MQTT 客戶端
```java
pahoMqtt5(mqttConfig.topic())
                .clientId("itachi")
                .brokerUrl(mqttConfig.brokerUrl())
                .keepAliveInterval(5)
                .willQos(2)
                .willRetained(true)
                .willPayload("""
                    offline
                """)
                .willTopic("itachi/offline")
```

透過發送空白訊息，讓應用程式意外關閉客戶端連線

用一個視窗訂閱定義的 `willTopic`

```bash
mattx sub -t itachi/offline

payload:     offline
```

只要此應用程式運行起來就會收到遺囑訊息。