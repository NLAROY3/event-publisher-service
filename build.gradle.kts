import org.sonarqube.gradle.SonarTask
import org.springframework.boot.gradle.tasks.run.BootRun
import pl.allegro.tech.build.axion.release.OutputCurrentVersionTask

buildscript {
    dependencies {
        classpath("javax.activation:javax.activation-api:1.2.0")
    }
}

plugins {
    java
    jacoco
    id("com.diffplug.spotless") version ("6.+")
    id("org.springframework.boot") version ("3.0.6")
    id("io.spring.dependency-management") version ("1.+")
    id("org.liquibase.gradle") version ("2.+")
    id("org.owasp.dependencycheck") version ("8.+")
    id("org.sonarqube") version ("4.+")
    id("pl.allegro.tech.build.axion-release") version ("1.+")
    id("com.google.cloud.tools.jib") version ("3.3.1")
}

scmVersion {
    versionCreator("versionWithBranch")
    versionIncrementer("incrementMinor")
}
version = scmVersion.version

tasks.withType<OutputCurrentVersionTask> {
    doNotTrackState("Workaround for gradle 8")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

val apiModelVersion: String by project

dependencies {
    implementation("com.atradius:andromeda-sdk-observability-starter:0.11.+")
    implementation("com.atradius:andromeda-sdk-starter-security:0.1.6-SNAPSHOT")
    implementation("com.atradius.shared.components:event-publisher-model:$apiModelVersion")
    implementation("com.atradius.shared.components:andromeda-event-framework:0.2.+")

    implementation("io.netty:netty-transport-native-epoll")
    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.swagger.core.v3:swagger-annotations:2.+")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("jakarta.validation:jakarta.validation-api")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.+")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("com.microsoft.azure:azure-client-authentication:1.7.14")
    implementation("com.azure:azure-identity:1.11.0")
    implementation("com.azure:azure-identity-broker:1.0.0")
    implementation("com.microsoft.azure:msal4j:1.14.0")
    implementation("com.azure.spring:spring-cloud-azure-starter")

    compileOnly("org.projectlombok:lombok")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    annotationProcessor("org.projectlombok:lombok")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    testCompileOnly("org.projectlombok:lombok")
}

configurations {
    compileOnly {
        extendsFrom(configurations.getByName("annotationProcessor"))
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<BootRun> {
    systemProperty("file.encoding", "UTF-8")
}

tasks.withType<SonarTask> {
    dependsOn("jacocoTestReport")
}

//dependencyCheck {
//    suppressionFile = "${project.rootDir}/gradle/suppressions.xml"
//}

spotless {
    java {
        googleJavaFormat()
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
    }
}

tasks.jacocoTestReport {
    reports {
        html.required.set(true)
        xml.required.set(true)
    }
}

tasks.jar {
    enabled = false
}

sonarqube {
    properties {
        property("sonar.projectKey", project.name)
    }
}

// Writes the version (determined by the Axion plugin) to a version.txt file in the build directory,
// used for containerizing with the correct version
tasks.register("writeVersion") {
    group = "build"
    doLast {
        project.logger.lifecycle("Version: " + project.version.toString())
        file("${project.buildDir}/version.txt")
            .also { it.parentFile.mkdirs() }
            .writeText(project.version.toString())
    }
}

jib {
    from {
        setImage(project.provider {
            if (project.hasProperty("commonBaseImageVersion")) {
                "acritsdevops.azurecr.io/sc/common-docker-image:${project.properties["commonBaseImageVersion"]}"
            } else {
                "docker://invalid-image"
            }
        })
    }
    to {
        image = "acritsdevops.azurecr.io/sc-org/event-publisher-service"
        tags = setOf(project.version.toString())
    }

    container {
        creationTime.set("USE_CURRENT_TIMESTAMP")
        ports = listOf("8080", "8081")
        labels.set(mapOf("maintainer" to "Shared Components Platform <sc-platform@atradius.com>",
                "app-name" to project.name,
                "service-version" to version.toString()))
        jvmFlags = listOf("-Dspring.profiles.active=docker,azure")
        workingDirectory = "/app"
    }

    extraDirectories{
        paths {
            path {
                setFrom("src/main/resources")
                into = "/jre/lib/security"
                includes.add("cacerts")
            }
        }
    }
}
