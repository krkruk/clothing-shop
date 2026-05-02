plugins {
    java
    id("org.springframework.boot") version "4.0.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.openapi.generator") version "7.12.0"
}

group = "com.clothingshop"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

// --- OpenAPI Code Generation ---
openApiGenerate {
    generatorName.set("spring")
    inputSpec.set("${rootDir}/../openapi/spec.yaml")
    outputDir.set("${layout.buildDirectory.get()}/generated")
    apiPackage.set("com.clothingshop.api")
    modelPackage.set("com.clothingshop.model")
    configOptions.set(mapOf(
        "interfaceOnly" to "true",
        "useTags" to "true",
        "openApiNullable" to "false",
        "documentationProvider" to "springdoc",
        "skipDefaultInterface" to "true",
        "useJakartaEe" to "true"
    ))
}

// --- Source Sets ---
sourceSets {
    main {
        java {
            srcDir("${layout.buildDirectory.get()}/generated/src/main/java")
        }
    }
    create("testComponent") {
        java {
            srcDir("src/testComponent/java")
        }
        resources {
            srcDir("src/testComponent/resources")
        }
        compileClasspath += sourceSets["test"].output + sourceSets["main"].output + configurations["testRuntimeClasspath"]
        runtimeClasspath += output + compileClasspath
    }
}

// --- Dependency Management for AWS SDK and TestContainers ---
dependencyManagement {
    imports {
        mavenBom("software.amazon.awssdk:bom:2.31.8")
        mavenBom("org.testcontainers:testcontainers-bom:1.21.0")
    }
}

dependencies {
    // Spring Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    // Database
    implementation("org.springframework.boot:spring-boot-starter-liquibase")
    runtimeOnly("org.postgresql:postgresql")

    // AWS S3 SDK (for MinIO)
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:auth")

    // OpenAPI Generated Code Dependencies
    implementation("io.swagger.core.v3:swagger-annotations-jakarta:2.2.28")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")

    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // Test (unit)
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-resttestclient")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // TestContainers (shared between test and testComponent)
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:minio")
    testImplementation("org.testcontainers:junit-jupiter")

    // testComponent-specific (inherits test deps via classpath)
    "testComponentImplementation"(sourceSets["test"].output)
    "testComponentImplementation"("org.junit.jupiter:junit-jupiter")
    "testComponentImplementation"("org.springframework.boot:spring-boot-starter-test")
    "testComponentImplementation"("org.testcontainers:testcontainers")
    "testComponentImplementation"("org.testcontainers:postgresql")
    "testComponentImplementation"("org.testcontainers:minio")
    "testComponentImplementation"("org.testcontainers:junit-jupiter")
    "testComponentCompileOnly"("org.projectlombok:lombok")
    "testComponentAnnotationProcessor"("org.projectlombok:lombok")
}

// --- Compile depends on OpenAPI generation ---
tasks.compileJava {
    dependsOn(tasks.openApiGenerate)
}

// --- Unit test configuration ---
tasks.test {
    useJUnitPlatform {
        excludeTags("component")
    }
}

// --- Component test task ---
val testComponentTask = tasks.register<Test>("testComponent") {
    group = "verification"
    description = "Runs component tests (requires Docker/Podman for TestContainers)"
    testClassesDirs = sourceSets["testComponent"].output.classesDirs
    classpath = sourceSets["testComponent"].runtimeClasspath
    useJUnitPlatform {
        includeTags("component")
    }
    shouldRunAfter(tasks.test)
}

// --- Build depends on both test and testComponent ---
tasks.build {
    dependsOn(testComponentTask)
}
