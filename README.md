# ViaVersion - Spigot, Sponge, BungeeCord, Velocity

[![Latest Release](https://img.shields.io/github/v/release/ViaVersion/ViaVersion)](https://viaversion.com)
[![Build Status](https://github.com/ViaVersion/ViaVersion/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/ViaVersion/ViaVersion/actions)
[![Discord](https://img.shields.io/badge/chat-on%20discord-blue.svg)](https://viaversion.com/discord)

**Allows the connection of higher client versions to lower server versions**

Supported Versions:

![Table (https://i.imgur.com/uDM9HR8.png)](https://i.imgur.com/uDM9HR8.png)

On Paper you may also use ProtocolSupport, but ensure you have the right build for your server version.

**User Docs:** https://docs.viaversion.com/display/VIAVERSION/


Sources:
--------
**[wiki.vg](https://wiki.vg)** (Used for information regarding packet structure, we also contribute back)

**[Burger](https://github.com/Pokechu22/Burger)** (See [PAaaS](https://github.com/Matsv/Paaas))

**[OpenNBT](https://github.com/ViaVersion/OpenNBT)**


Releases / Dev Builds:
--------
You can find official releases here:

https://www.spigotmc.org/resources/viaversion.19254/history

----------

You can find official dev builds here:

**Jenkins:** https://ci.viaversion.com

**JavaDocs:** https://jd.viaversion.com

**Maven Repository:**
```xml
<repository>
    <id>viaversion-repo</id>
    <url>https://repo.viaversion.com</url>
</repository>
```

**API-artifact:**
```xml
<dependency>
    <groupId>com.viaversion</groupId>
    <artifactId>viaversion-api</artifactId>
    <version>LATEST</version>
    <scope>provided</scope>
</dependency>
```

Replace the version depending on your needs.

If you need access to the existing protocol or platform implementations, use the parent artifact `viaversion`.
Please note the [differences in licensing](#license).


Building:
--------
After cloning this repository, build the project with Gradle by running `./gradlew build` and take the created jar out of
the `build/libs` directory.


License:
--------
The entirety of the [API directory](api) (including the legacy API directory) is licensed under the MIT License; see [licenses/MIT.md](licenses/MIT.md) for
details.

Everything else, unless explicitly stated otherwise, is licensed under the GNU General Public License, including the end
product as a whole; see [licenses/GPL.md](licenses/GPL.md) for details.

Special thanks to all our [Contributors](https://github.com/ViaVersion/ViaVersion/graphs/contributors).
