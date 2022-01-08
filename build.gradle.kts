plugins {
    id("org.jetbrains.intellij") version "1.3.0"
    kotlin("jvm") version "1.5.10"
    java
}

group = "com.github.rkbalgi.intellij.plugins"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
}

java{
    java.sourceCompatibility=JavaVersion.VERSION_11
    java.targetCompatibility=JavaVersion.VERSION_11
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.github.rkbalgi:iso4k:1.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
    version.set("2021.2")
}
tasks {
    patchPluginXml {
        changeNotes.set("""
            Add change notes here.<br>
            <em>most HTML tags may be used</em>        """.trimIndent())
    }
}
tasks.getByName<Test>("test") {
    useJUnitPlatform()
}