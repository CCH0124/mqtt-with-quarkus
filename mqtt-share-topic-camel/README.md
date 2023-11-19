# mqtt-share-topic-camel

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

You can then execute your native executable with: `./build/mqtt-share-topic-camel-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Related Guides

- Camel Log ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/log.html)): Log messages to the underlying logging mechanism
- Camel Core ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/core.html)): Camel core functionality and basic Camel languages: Constant, ExchangeProperty, Header, Ref, Simple and Tokenize
- Camel Direct ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/direct.html)): Call another endpoint from the same Camel Context synchronously
- Camel Paho MQTT5 ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/paho-mqtt5.html)): Communicate with MQTT message brokers using Eclipse Paho MQTT v5 Client
- Camel Timer ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/timer.html)): Generate messages in specified intervals using java.util.Timer
- Camel Jackson ([guide](https://camel.apache.org/camel-quarkus/latest/reference/extensions/jackson.html)): Marshal POJOs to JSON and back using Jackson

## Build Image

```bash
gradle build -Dquarkus.container-image.push=true  -Dquarkus.container-image.build=true -Dquarkus.container-image.additional-tags=$(git log -1 --pretty=format:%h),latest
```

## None Share topic

先至 `podman` 目錄下，將環境啟用

```bash
podman compose -f podman-compose.yaml up -d
```

再不做任何的共享訂閱時，會發生如下情況，使用 `mqttx` 發布一個訊息

```bash
 mqttx pub -t itachi/status -m "{test: "hello2"}"
[11/19/2023] [11:07:49 AM] › …  Connecting...
[11/19/2023] [11:07:49 AM] › ✔  Connected
[11/19/2023] [11:07:49 AM] › …  Message publishing...
[11/19/2023] [11:07:49 AM] › ✔  Message published
```

發布後，兩個容器都會收到同一個被發送的訊息

```bash
% podman logs -f 635ebf84893b
2023-11-19 03:07:49,677 INFO  [itachi-share-topic-635ebf84893b] (MQTT Call: itachi-share-topic-635ebf84893b) Payload: {test: hello2}
```

```bash
% podman logs -f 1dfe31c72bba
2023-11-19 03:07:49,691 INFO  [itachi-share-topic-1dfe31c72bba] (MQTT Call: itachi-share-topic-1dfe31c72bba) Payload: {test: hello2}
```

這表示了如果 `subcriber` 要進行擴展時會重複處理同一個資訊(在某些場景下)，這也表示唯一性是不穩定的。如果透過 EMQX 的共享訂閱，則可以避免。

客戶端可以分為多個訂閱組，訊息仍會被轉發到所有訂閱組，*但每個訂閱組內只有一個客戶端接收訊息*。可以為一組訂閱者的原始主題添加前綴以啟用共享訂閱。 EMQX 支援兩種格式的共享訂閱前綴，分別為帶有群組的共享訂閱（前綴為 `$share/<group-name>/`）和不帶群組的共享訂閱（前綴為 `$queue/`）。

|前缀格式|	示例|	前缀|	實際上topic|
|---|---|---|---|
|帶有群組格式	|$share/abc/t/1	|$share/abc/	|t/1|
|不帶有群組格式|	$queue/t/1	|$queue/|	t/1|


## Share Topic

準備環境，至 `podman` 目錄下運行

```bash
podman compose -f docker-compose-share-topic.yaml up -d
```

其 topic 被配置成 `$share/group1/itachi/status`


- 前缀 `$share` 聲明是一個共享訂閱
- `group1` 是一個可用來區分群的名稱
- `/itachi/status` 是原始 topic



使用 `mqttx` 發布訊息

```bash
% mqttx pub -t itachi/status -m "group1"
```

會發現共同的 group1 裡面的 `subscriber` 只有一個會收到訊息。下圖為官方的示例

![](https://www.emqx.io/docs/assets/shared_subscription_group.f42d424c.png) From EMQX Office

如果訂閱者 s1、s2 和 s3 是群組 g1 的成員，訂閱者 s4 和 s5 是群組 g2 的成員，而所有訂閱者都訂閱了原始主題 t1。共享訂閱的主題必須是 `$share/g1/t1` 和 `$share/g2/t1`。當 EMQX 發布訊息 msg1 到原始主題 t1 時： 
- EMQX 將 msg1 傳送給 g1 和 g2 兩組
- s1、s2、s3 中的一個訂閱者將接收 msg1
- s4 和 s5 中的一個訂閱者將接收 msg1。