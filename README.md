# JDA Helper

## Installation

### Current Version: [![](https://jitpack.io/v/Bensonheimer992/JDA-Helper.svg)](https://jitpack.io/#Bensonheimer992/JDA-Helper)

<details>
 <summary>Maven</summary>

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
        <version>x.y.z</version>
    </dependency>
```

</details>

<details>
<summary>Gradle</summary>

```
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}
```

```

dependencies {
		implementation 'com.github.User:Repo:Tag'
}

```

</details>

## How to Create a Bot


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

