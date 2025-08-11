plugins {
    id("io.spring.dependency-management")
}

dependencies {
    api(project(":dispatch4j-core"))
    
    implementation("org.springframework.boot:spring-boot-autoconfigure")
    implementation("org.springframework:spring-context")
    
    // Optional dependencies
    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    compileOnly("org.springframework.security:spring-security-core")
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-core")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.4")
    }
}