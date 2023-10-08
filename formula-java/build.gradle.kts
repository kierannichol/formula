plugins {
    id("java-library")
    id("maven-publish")
}

group = "org.formula"
version = System.getenv("RELEASE_VERSION") ?: "1.0-SNAPSHOT"

java {
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
}

publishing {
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/kierannichol/formula")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testImplementation("org.assertj:assertj-core:3.24.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}