import net.fabricmc.loom.api.LoomGradleExtensionAPI
import java.io.ByteArrayOutputStream

plugins {
    java
    `maven-publish`
    id("fabric-loom") version "1.7-SNAPSHOT"
    id("net.kyori.blossom") version "2.1.0" // https://github.com/KyoriPowered/blossom
    id("com.modrinth.minotaur") version "2.+"
}

println("Tier Tower v${"mod_version"()}")

val isRelease = System.getenv("RELEASE_BUILD")?.toBoolean() ?: false
val buildNumber = System.getenv("GITHUB_RUN_NUMBER")?.toInt()
val gitHash = "\"${calculateGitHash() + (if (hasUnstaged()) "-modified" else "")}\""
val accessWidenerFile = file("src/main/resources/tier_tower.accesswidener")

base.archivesName.set("archives_base_name"())
group = "maven_group"()

// Formats the mod version to include the Mincraft version and build number (if present)
val build = buildNumber?.let { "-build.${it}" } ?: "-local"

version = "${"mod_version"()}+fabric-mc${"minecraft_version"() + if (isRelease) "" else build}"

java {
    withSourcesJar()

    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 17
}

repositories {
    mavenCentral()
    exclusiveMaven("https://maven.parchmentmc.org", "org.parchmentmc.data") // Parchment mappings
    maven("https://maven.terraformersmc.com/releases/") // Mod Menu, EMI
    exclusiveMaven("https://api.modrinth.com/maven", "maven.modrinth") // LazyDFU
    maven("https://mvn.devos.one/snapshots/") // Create Fabric, Porting Lib, Forge Tags, Milk Lib, Registrate Fabric
    maven("https://mvn.devos.one/releases") // Porting Lib Releases
    exclusiveMaven("https://maven.createmod.net", "net.createmod", "dev.engine-room") // Ponder
    maven("https://jitpack.io/") // Mixin Extras, Fabric ASM
    maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven/") // forge config api port
    exclusiveMaven("https://maven.jamieswhiteshirt.com/libs-release", "com.jamieswhiteshirt") // Reach Entity Attributes
    //exclusiveMaven("https://maven.tterrag.com/", "com.jozufozu.flywheel") // Flywheel
    exclusiveMaven("https://modmaven.dev/", "com.jozufozu.flywheel") // Flywheel
}

val loom = project.extensions.getByType<LoomGradleExtensionAPI>()
loom.apply {
    runs.configureEach {
        vmArg("-XX:+AllowEnhancedClassRedefinition")
        vmArg("-XX:+IgnoreUnrecognizedVMOptions")
        vmArg("-Dmixin.debug.export=true")
        vmArg("-Dmixin.env.remapRefMap=true")
        vmArg("-Dmixin.env.refMapRemappingFile=${projectDir}/build/createSrgToMcp/output.srg")
    }
}

loom {
    accessWidenerPath = accessWidenerFile

    runs {
        create("datagen") {
            client()

            name = "Minecraft Data"
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${project.file("src/generated/resources")}")
            vmArg("-Dfabric-api.datagen.modid=tier_tower")
            vmArg("-Dporting_lib.datagen.existing_resources=${project.file("src/main/resources")}")
            programArgs("--existing", project.file("src/main/resources").absolutePath)

            environmentVariable("DATAGEN", "TRUE")
        }
    }
}

configurations.configureEach {
    resolutionStrategy {
        force("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${"minecraft_version"()}")
    // layered mappings - Mojmap names, parchment and QM docs and parameters
    @Suppress("UnstableApiUsage")
    mappings(loom.layered {
        officialMojangMappings { nameSyntheticMembers = false }
        parchment("org.parchmentmc.data:parchment-${"minecraft_version"()}:${"parchment_version"()}@zip")
    })

    // Used to decompile mixin dumps, needs to be on the classpath
    // Uncomment if you want it to decompile mixin exports, beware it has very verbose logging.
    //implementation("org.vineflower:vineflower:1.10.0")

    modImplementation("net.fabricmc:fabric-loader:${"fabric_loader_version"()}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${"fabric_api_version"()}")

    testImplementation("net.fabricmc:fabric-loader-junit:${"fabric_loader_version"()}")

    modApi(include("com.tterrag.registrate_fabric:Registrate:${"registrate_version"()}")!!)

    include(modImplementation("me.lucko:fabric-permissions-api:${"fabric_permissions_api_version"()}") {
        isTransitive = false
    })

    // Development QOL
    modLocalRuntime("maven.modrinth:lazydfu:${"lazydfu_version"()}")
    modLocalRuntime("com.terraformersmc:modmenu:${"modmenu_version"()}")

    modLocalRuntime("dev.emi:emi-fabric:${"emi_version"()}")

    // Compat

    modCompileOnly("dev.emi:emi-fabric:${"emi_version"()}:api")

    val createCoordinates = if ("create6"().toBoolean()) {
        "com.simibubi.create:create-fabric:${"create_fabric6_version"()}"
    } else {
        "com.simibubi.create:create-fabric-${"minecraft_version"()}:${"create_fabric_version"()}"
    }
    modCompileOnly(createCoordinates)
    if ("enable_create"().toBoolean()) {
        // Create - dependencies are added transitively
        modLocalRuntime(createCoordinates)
    }
}

tasks.processResources {
    val properties = mapOf(
        "version" to version,
        "minecraft_version" to "minecraft_version"(),
        "fabric_api_version" to "fabric_api_version"(),
        "fabric_loader_version" to "fabric_loader_version"(),
    )

    inputs.properties(properties)

    filesMatching("fabric.mod.json") {
        expand(properties)
    }

    // don't add development or to-do files into built jar
    exclude("**/*.bbmodel", "**/*.lnk", "**/*.xcf", "**/*.md", "**/*.txt", "**/*.blend", "**/*.blend1")
}

tasks.test {
    useJUnitPlatform()
}

sourceSets.main {
    resources { // include generated resources in resources
        srcDir("src/generated/resources")
        exclude(".cache/**")
    }
    blossom.javaSources {
        property("version", "mod_version"())
        property("gitCommit", gitHash)
    }
}

tasks.jar {
    archiveClassifier = "dev"

    manifest {
        attributes(mapOf("Git-Hash" to gitHash))
    }
}

tasks.named<Jar>("sourcesJar") {
    manifest {
        attributes(mapOf("Git-Hash" to gitHash))
    }
}

operator fun String.invoke(): String {
    return rootProject.ext[this] as? String
        ?: throw IllegalStateException("Property $this is not defined")
}

fun calculateGitHash(): String {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "rev-parse", "HEAD")
        standardOutput = stdout
    }
    return stdout.toString().trim()
}

fun hasUnstaged(): Boolean {
    val stdout = ByteArrayOutputStream()
    exec {
        commandLine("git", "status", "--porcelain")
        standardOutput = stdout
    }
    val result = stdout.toString().replace("M gradlew", "").trimEnd()
    if (result.isNotEmpty())
        println("Found stageable results:\n${result}\n")
    return result.isNotEmpty()
}

fun RepositoryHandler.exclusiveMaven(url: String, vararg groups: String) {
    exclusiveContent {
        forRepository { maven(url) }
        filter {
            groups.forEach {
                @Suppress("UnstableApiUsage")
                includeGroupAndSubgroups(it)
            }
        }
    }
}

modrinth {
    token = System.getenv("MODRINTH_TOKEN")
    projectId = "modrinth_id"()
    versionName = "Tier Tower v${"mod_version"()} Fabric ${"minecraft_version"()}"
    versionNumber = project.version.toString()
    versionType = System.getenv().getOrDefault("RELEASE_TYPE", "release")
    uploadFile = tasks.remapJar.get()
    gameVersions.add("minecraft_version"())
    loaders.add("fabric")
    changelog = System.getenv("CHANGELOG")
    syncBodyFrom = "<!--DO NOT EDIT MANUALLY: synced from gh readme-->\n" + rootProject.file("README.md").readText()
    dependencies {
        required.project("fabric-api")
        embedded.project("fabric-permissions-api")
    }
}
