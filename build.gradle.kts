plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.spring") version "2.1.21"
     kotlin("plugin.jpa") version "2.1.21"
    id("org.springframework.boot") version "3.4.5"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "me.csy"
version = "1.0"

repositories {
    maven("https://mirrors.cloud.tencent.com/nexus/repository/maven-public/")
    mavenCentral()
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    runtimeOnly("com.mysql:mysql-connector-j")

    implementation("us.codecraft:webmagic-core:1.0.3")
    implementation("us.codecraft:webmagic-extension:1.0.3") {
        exclude(group = "org.slf4j", module = "slf4j-log4j12")
    }
    implementation("org.seleniumhq.selenium:selenium-java:4.35.0")
    implementation("org.seleniumhq.selenium:selenium-chrome-driver:4.35.0")

    implementation("org.dromara.hutool:hutool-all:6.0.0-M22")
    implementation("com.google.guava:guava:33.0.0-jre")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation(kotlin("test"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
