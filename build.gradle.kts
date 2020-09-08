import org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE
import org.gradle.internal.os.OperatingSystem.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URL

plugins {
    java
    kotlin("jvm") version "1.4.0"
    `maven-publish`
    id("org.jetbrains.dokka") version "1.4.0"
    id("com.github.johnrengelman.shadow").version("6.0.0")
}

group = "com.github.kotlin_graphics"
val moduleName = "$group.gli"

val kotestVersion = "4.2.0"
val kx = "com.github.kotlin-graphics"
val unsignedVersion = "1a71e303"
val koolVersion = "4359096c"
val glmVersion = "557908c3"
val lwjglVersion = "3.2.3"
val lwjglNatives = "natives-" + when (current()) {
    WINDOWS -> "windows"
    LINUX -> "linux"
    else -> "macos"
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("$kx:kotlin-unsigned:$unsignedVersion")
    implementation("$kx:kool:$koolVersion")
    implementation("$kx:glm:$glmVersion")

    // https://mvnrepository.com/artifact/com.twelvemonkeys.imageio/imageio-core
    listOf("", /*"-batik",*/ "-bmp", "-core", "-icns", "-iff", "-jpeg", "-metadata", "-pcx", "-pdf", "-pict", "-pnm",
            "-psd", "-sgi", "-tga", "-thumbsdb", "-tiff"/*, "-reference", "-clippath", "-hdr"*/).forEach {
        implementation("com.twelvemonkeys.imageio:imageio$it:3.5")
    }
    // https://mvnrepository.com/artifact/org.apache.xmlgraphics/batik-transcoder
    //implementation "org.apache.xmlgraphics:batik-transcoder:1.12"

    implementation(platform("org.lwjgl:lwjgl-bom:$lwjglVersion"))
    listOf("", "-jemalloc", "-opengl").forEach {
        implementation("org.lwjgl", "lwjgl$it")
        runtimeOnly("org.lwjgl", "lwjgl$it", classifier = lwjglNatives)
    }

    testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
}

tasks {
    dokkaHtml {
        dokkaSourceSets.configureEach {
            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(URL("https://github.com/kotlin-graphics/gli/tree/master/src/main/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }

    withType<KotlinCompile>().all {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs += listOf("-Xinline-classes", "-Xopt-in=kotlin.RequiresOptIn")
        }
        sourceCompatibility = "1.8"
    }

    withType<Test> { useJUnitPlatform() }
}

val dokkaJavadocJar by tasks.register<Jar>("dokkaJavadocJar") {
    dependsOn(tasks.dokkaJavadoc)
    from(tasks.dokkaJavadoc.get().outputDirectory.get())
    archiveClassifier.set("javadoc")
}

val dokkaHtmlJar by tasks.register<Jar>("dokkaHtmlJar") {
    dependsOn(tasks.dokkaHtml)
    from(tasks.dokkaHtml.get().outputDirectory.get())
    archiveClassifier.set("html-doc")
}

val sourceJar = task("sourceJar", Jar::class) {
    dependsOn(tasks.classes)
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

artifacts {
    archives(dokkaJavadocJar)
    archives(dokkaHtmlJar)
    archives(sourceJar)
}

publishing {
    publications.create<MavenPublication>("mavenJava") {
        from(components["java"])
        artifact(sourceJar)
    }
    repositories.maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/kotlin-graphics/gli")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

// == Add access to the 'modular' variant of kotlin("stdlib"): Put this into a buildSrc plugin and reuse it in all your subprojects
configurations.all { attributes.attribute(TARGET_JVM_VERSION_ATTRIBUTE, 8) }