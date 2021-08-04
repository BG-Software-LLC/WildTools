# WildTools

WildTools 2 - The most optimized all-in-one tools plugin!

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