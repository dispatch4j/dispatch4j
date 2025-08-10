val classgraphVersion: String by project

dependencies {
    api(project(":dispatch4j-core"))
    
    // ClassGraph for annotation scanning
    implementation("io.github.classgraph:classgraph:$classgraphVersion")
}