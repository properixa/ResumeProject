plugins {
    id("java")
    war
}

group = "org.servicehub"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.springframework:spring-webmvc:7.0.8")
    implementation("org.flywaydb:flyway-core:12.8.1")
    runtimeOnly("org.flywaydb:flyway-database-postgresql:12.8.1")
    implementation("org.springframework.data:spring-data-jpa:4.1.0")
    implementation("org.hibernate.orm:hibernate-core:7.4.1.Final")
    implementation("com.zaxxer:HikariCP:7.0.2")
    runtimeOnly("org.postgresql:postgresql:42.7.11")
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.1.0")
    compileOnly("org.projectlombok:lombok:1.18.46")
    annotationProcessor("org.projectlombok:lombok:1.18.46")
    implementation("tools.jackson.core:jackson-databind:3.2.0")
    implementation("org.mapstruct:mapstruct:1.6.3")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.3")
    implementation("ch.qos.logback:logback-classic:1.5.34")
    implementation("org.hibernate.validator:hibernate-validator:9.1.0.Final")
    implementation("org.springframework.security:spring-security-config:7.1.0")
    implementation("org.springframework.security:spring-security-web:7.1.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
}

tasks.test {
    useJUnitPlatform()
}

tasks.war {
    archiveFileName.set("service-hub-api.war")
}