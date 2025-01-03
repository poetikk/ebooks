plugins {
    kotlin("jvm") version "2.0.10"
    application
}

group = "no.rmy.wiki2epub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    val ktor_version = "3.0.0"
    val logbackVersion = "1.4.14"

    implementation("io.documentnode:epub4j-core:4.2.1")
    // implementation("org.sweble.wikitext:swc-parser-lazy:3.1.9")
    implementation("io.ktor:ktor-client:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    // implementation("io.ktor:ktor-client-logging:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")
    implementation("net.seeseekey:epubwriter:1.0.4")
    implementation("ch.qos.logback:logback-classic:${logbackVersion}")
    implementation(kotlin("reflect"))
    
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation(kotlin("test"))
}

sourceSets {
    main {
        java.srcDir("src/main/kotlin")
        resources.srcDir("/src/main/resources")
    }
    test {
        java.srcDir("src/test/kotlin")
        resources.srcDir("src/test/resources")
    }
}
application {
    mainClass.set("no.rmy.wiki2epub.MainKt")
}

tasks.test {
    useJUnitPlatform()
}
tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}