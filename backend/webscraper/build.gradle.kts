plugins {
	java
	id("org.springframework.boot") version "3.4.2"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.cyphercola"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.seleniumhq.selenium:selenium-java:4.28.1")
	implementation("com.bucket4j:bucket4j-core:8.10.1")
	implementation("org.springframework.boot:spring-boot-starter-web:3.4.2")
	implementation("org.springframework.retry:spring-retry:2.0.11")
	implementation("org.springframework.boot:spring-boot-starter")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
