val slf4jVersion: String by project
val jakartaAnnotationVersion: String by project

dependencies {
    // Minimal core dependencies
    api("org.slf4j:slf4j-api:$slf4jVersion")
    
    // Jakarta annotations for @Nullable, @Nonnull etc.
    compileOnly("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
}

tasks.test {
    useJUnitPlatform()
}