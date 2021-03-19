<p align="center">
<img src="https://bg-software.com/imgs/wildtools-logo.png" />
<h2 align="center">The most optimized all-in-one tools plugin!</h2>
</p>
<br>
<p align="center">
<a href="https://bg-software.com/discord/"><img src="https://img.shields.io/discord/293212540723396608?color=7289DA&label=Discord&logo=discord&logoColor=7289DA&link=https://bg-software.com/discord/"></a>
<a href="https://bg-software.com/patreon/"><img src="https://img.shields.io/badge/-Support_on_Patreon-F96854.svg?logo=patreon&style=flat&logoColor=white&link=https://bg-software.com/patreon/"></a><br>
<a href=""><img src="https://img.shields.io/maintenance/yes/2020"></a>
</p>

## Compiling

You can compile the project using gradlew.<br>
Run `gradlew shadowJar build` in console to build the project.<br>
You can find already compiled jars on our [Jenkins](https://hub.bg-software.com/) hub!<br>
You must add yourself all the private jars or purchase access to our private repository.

##### Private Jars:
- ChunkCollectors by Cloth [[link]](https://www.mc-market.org/resources/13522/)
- CMI by Zrips [[link]](https://www.spigotmc.org/resources/3742/)
- ShopGUIPlus by brcdev [[link]](https://www.spigotmc.org/resources/6515/)
- Lands by Angeschossen [[link]](https://www.spigotmc.org/resources/53313/)
- mcMMO (v1 & v2) by nossr50 [[link]](https://www.spigotmc.org/resources/64348/)
- SuperMobCoins by Swanis [[link]](https://www.mc-market.org/resources/8309/)
- QuantumShop by NightExpress [[link]](https://www.spigotmc.org/resources/50696/)
- Residence by Zrips [[link]](https://www.spigotmc.org/resources/11480/)

## API

<<<<<<< HEAD
You can hook into the plugin by using the built-in API module.<br>
The API module is safe to be used, its methods will not be renamed or changed, and will not have methods removed 
without any further warning.<br>
You can add the API as a dependency using Maven or Gradle:<br>

#### Maven
```
<repository>
    <id>bg-repo</id>
    <url>https://repo.bg-software.com/repository/api/</url>
</repository>

<dependency>
    <groupId>com.bgsoftware</groupId>
    <artifactId>WildToolsAPI</artifactId>
    <version>latest</version>
</dependency>
```

#### Gradle
=======
The plugin is packed with a rich API for interacting with tools and more. When hooking into the plugin, it's highly recommended to only use the API and not the compiled plugin, as the API methods are not only commented, but also will not get removed or changed unless they are marked as deprecated. This means that by using the API, you won't have to do any additional changes to your code between updates.

##### Maven
```
<repositories>
    <repository>
        <id>bg-repo</id>
        <url>https://repo.bg-software.com/repository/api/</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.bgsoftware</groupId>
        <artifactId>WildToolsAPI</artifactId>
        <version>latest</version>
    </dependency>
</dependencies>
```
##### Gradle
>>>>>>> 5dac590 (Updated readme)
```
repositories {
    maven { url 'https://repo.bg-software.com/repository/api/' }
}

dependencies {
    compileOnly 'com.bgsoftware:WildToolsAPI:latest'
}
```

## Updates

This plugin is provided "as is", which means no updates or new features are guaranteed. We will do our best to keep 
updating and pushing new updates, and you are more than welcome to contribute your time as well and make pull requests
for bug fixes. 

## License

This plugin is licensed under GNU GPL v3.0
