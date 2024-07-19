# JDA Helper

## Installation

---

```xml
<repository>
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
</repository>
```
```xml
<dependency>
    <groupId>com.github.Bensonheimer992</groupId>
    <artifactId>JDA-Helper</artifactId>
    <version>-SNAPSHOT</version>
</dependency>
```

## How to Create a Bot

---

### Playing Activity

```java
public class Main {
    public static void main(String[] args) {
        JDAHelper.createBot(token, OnlineStatus.ONLINE, Activity.ActivityType.PLAYING, "Minecraft",  Color.decode("0x0d3070"));
    }
}
```

### Streaming Activity

```java
public class Main {
    public static void main(String[] args) {
        JDAHelper.createBot(token, OnlineStatus.ONLINE, Activity.ActivityType.STREAMING, "Minecraft", "https://twitch.tv/bensonheimer992",  Color.decode("0x0d3070"));
    }
}
```

#### Replace the String Inside Color.decode("string") with your desired Embed Color

