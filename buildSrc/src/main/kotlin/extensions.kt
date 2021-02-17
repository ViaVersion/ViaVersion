import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

fun relocate(shadow: ShadowJar) {
    shadow.relocate("org.yaml.snakeyaml", "us.myles.viaversion.libs.snakeyaml")
    shadow.relocate("javassist", "us.myles.viaversion.libs.javassist")
    shadow.relocate("com.google.gson", "us.myles.viaversion.libs.gson")
    shadow.relocate("com.github.steveice10.opennbt", "us.myles.viaversion.libs.opennbt")

    shadow.relocate("net.md_5.bungee", "us.myles.viaversion.libs.bungeecordchat") {
        include("net.md_5.bungee.api.chat.*")
        include("net.md_5.bungee.api.ChatColor")
        include("net.md_5.bungee.api.ChatMessageType")
        include("net.md_5.bungee.chat.*")
    }
}

fun relocateAll(shadow: ShadowJar) {
    relocate(shadow)
    shadow.relocate("it.unimi.dsi.fastutil", "us.myles.viaversion.libs.fastutil") {
        // We only want int and Object maps
        include("it.unimi.dsi.fastutil.ints.*")
        include("it.unimi.dsi.fastutil.objects.*")
        include("it.unimi.dsi.fastutil.*.class")
        // Object types
        exclude("it.unimi.dsi.fastutil.*.*Reference*")
        exclude("it.unimi.dsi.fastutil.*.*Boolean*")
        exclude("it.unimi.dsi.fastutil.*.*Byte*")
        exclude("it.unimi.dsi.fastutil.*.*Short*")
        exclude("it.unimi.dsi.fastutil.*.*Float*")
        exclude("it.unimi.dsi.fastutil.*.*Double*")
        exclude("it.unimi.dsi.fastutil.*.*Long*")
        exclude("it.unimi.dsi.fastutil.*.*Char*")
        // Map types
        exclude("it.unimi.dsi.fastutil.*.*Custom*")
        exclude("it.unimi.dsi.fastutil.*.*Linked*")
        exclude("it.unimi.dsi.fastutil.*.*Sorted*")
        exclude("it.unimi.dsi.fastutil.*.*Tree*")
        exclude("it.unimi.dsi.fastutil.*.*Heap*")
        exclude("it.unimi.dsi.fastutil.*.*Queue*")
        // Crossing fingers
        exclude("it.unimi.dsi.fastutil.*.*Big*")
        exclude("it.unimi.dsi.fastutil.*.*Synchronized*")
        exclude("it.unimi.dsi.fastutil.*.*Unmodifiable*")
    }
}
