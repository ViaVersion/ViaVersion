# ViaVersion

[![Latest Release](https://img.shields.io/github/v/release/ViaVersion/ViaVersion)](https://viaversion.com)
[![Build Status](https://github.com/ViaVersion/ViaVersion/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/ViaVersion/ViaVersion/actions)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://viaversion.com/discord)

**Allows the connection of higher client versions to lower server versions -
it works on any platform you can think of.**

The base ViaVersion jar runs on Paper and Velocity. We also have projects integrating ViaVersion to run
on Fabric, Forge, Sponge, or as a standalone proxy to join from basically any client version on
any server version from the past decade. **See [HERE](https://viaversion.com) for an overview of the different Via\* projects.**

Note that ViaVersion will be able to **run best on either Paper servers or through [ViaFabricPlus](https://github.com/ViaVersion/ViaFabricPlus?tab=readme-ov-file#viafabricplus)** due to having
direct access to client/server state and more extensive API.

Supported Versions:

![Table (https://ci.viaversion.com/userContent/images/T.png)](https://ci.viaversion.com/userContent/images/T.png)

**User Docs:** https://docs.viaversion.com

Snapshot support
--------
**ViaVersion will only be released a few days *after* a Minecraft update** unless the protocol changes of the update were trivial. If you want early-access, usually days or even weeks before the final release, you can subscribe to either:
- [GitHub Sponsors](https://github.com/sponsors/kennytv/sponsorships?sponsor=kennytv&tier_id=385613&preview=false) (preferred option. Use the `/verify` command on this Discord after), or alternatively
- [Patreon](https://www.patreon.com/kennytv/membership) (see the highest tier and make sure to link Patreon to your Discord account under Settings->Connections)
  This also includes access to a private repository with the code, which will be pushed to the public repository after the given delay on a Minecraft update.

Releases/Dev Builds
--------
You can find official releases in the following places:

- **Hangar (for our plugins)**: https://hangar.papermc.io/ViaVersion/ViaVersion
- **Modrinth (for our mods)**: https://modrinth.com/mod/viaversion
- **GitHub**: https://github.com/ViaVersion/ViaVersion/releases

Dev builds for **all** of our projects are on our Jenkins server:

- **Jenkins**: https://ci.viaversion.com

ViaVersion as a Dependency
----------

**JavaDocs:** https://jd.viaversion.com

**Maven:**

```xml
<repository>
    <id>viaversion-repo</id>
    <url>https://repo.viaversion.com</url>
</repository>
```

```xml
<dependency>
    <groupId>com.viaversion</groupId>
    <artifactId>viaversion-api</artifactId>
    <version>[5.0.0,6.0.0)</version>
    <scope>provided</scope>
</dependency>
```

**Gradle:**

```kotlin
repositories {
    maven("https://repo.viaversion.com")
}

dependencies {
    compileOnly("com.viaversion:viaversion-api:VERSION") // Replace the version
}
```

If you need access to the existing protocol or platform implementations, use the parent artifact `viaversion`.
Please note the [differences in licensing](#license).

Note: If you want to make your own platform implementation of ViaVersion (and additional addons),
you can use the [ViaLoader](https://github.com/ViaVersion/ViaLoader) project.

Building
--------
After cloning this repository, build the project with Gradle by running `./gradlew build` and take the created jar out
of the `build/libs` directory.

You need JDK 17 or newer to build ViaVersion.

Mapping Files
--------------
Mapping files are generated and managed in our [Mappings repository](https://github.com/ViaVersion/Mappings). The generated mapping output is stored [here](./common/src/main/resources/assets/viaversion).

Resources
--------

- **[Via Mappings Generator](https://github.com/ViaVersion/Mappings)**
- **[Mojang mappings](https://minecraft.wiki/w/Obfuscation_map)** (Thank you, Mojang, very cool)
- Previously: **[Burger](https://github.com/Pokechu22/Burger)** (see [PAaaS](https://github.com/Matsv/Paaas)) and **wiki.vg**

License
--------
The entirety of the [API directory](api) is licensed under the MIT License;
see [licenses/MIT.md](licenses/MIT.md) for
details.

Everything else, unless explicitly stated otherwise, is licensed under the GNU General Public License v3, including the
end-product as a whole; see [licenses/GPL.md](licenses/GPL.md) for details.

Special thanks to all our [Contributors](https://github.com/ViaVersion/ViaVersion/graphs/contributors).
