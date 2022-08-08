
plugins {
    java
    `java-library`
    `maven-publish`
}

group = "com.mojang"
version = "1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://libraries.minecraft.net")
}

configurations {
}

dependencies {
    implementation("com.google.code.findbugs:jsr305:2.0.1")
    implementation("com.google.code.gson:gson:2.8.0")
    implementation("org.apache.logging.log4j:log4j-api:2.8.1")
    implementation("com.google.guava:guava:21.0")
    implementation("org.apache.commons:commons-lang3:3.5")
    implementation("it.unimi.dsi:fastutil:7.1.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

task<Jar>("sourcesJar") {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

tasks.test {
    useJUnitPlatform()
}

artifacts {
    archives(tasks.jar)
    archives(tasks.getByName("sourcesJar"))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.properties["group"] as? String?
            artifactId = project.name
            version = project.properties["version"] as? String?

            from(components["java"])
        }
    }
}
