plugins {
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":dispatch4j-spring-boot-autoconfigure"))
    api(project(":dispatch4j-core"))
    
    // Spring Boot Starter dependencies
    api("org.springframework.boot:spring-boot-starter")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.5")
    }
}