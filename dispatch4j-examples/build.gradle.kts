plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {
    implementation(project(":dispatch4j-spring-boot-starter"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-security")
}

tasks.withType(PublishToMavenRepository::class) {
    enabled = false
}