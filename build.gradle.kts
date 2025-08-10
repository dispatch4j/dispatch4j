plugins {
    java
    `java-library`
    `maven-publish`
    jacoco
    checkstyle
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springframework.boot") version "3.4.1" apply false
    id("com.github.spotbugs") version "6.2.4" apply false
    id("org.owasp.dependencycheck") version "11.1.1" apply false
    id("org.sonarqube") version "6.0.1.5171"
    id("com.diffplug.spotless") version "6.25.0"
    id("me.champeau.jmh") version "0.7.2" apply false
    id("info.solidsoft.pitest") version "1.15.0" apply false
    id("com.github.ben-manes.versions") version "0.51.0"
}

group = "io.github.dispatch4j"
version = project.findProperty("version")?.toString() ?: "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "com.diffplug.spotless")
    
    spotless {
        java {
            googleJavaFormat("1.24.0")
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}

//sonar {
//    properties {
//        property("sonar.host.url", System.getenv("SONAR_HOST_URL") ?: "https://sonarcloud.io")
//        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
//        property("sonar.junit.reportPaths", "**/build/test-results/test")
//    }
//}

tasks.register("integrationTest") {
    group = "verification"
    description = "Runs integration tests"
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "integrationTest" } })
}

tasks.register("dependencyCheckAnalyze") {
    group = "verification"
    description = "Runs OWASP dependency check"
    dependsOn(subprojects.map { it.tasks.matching { task -> task.name == "dependencyCheckAnalyze" } })
}

tasks.register("pitest") {
    group = "verification"
    description = "Runs mutation testing with PITest"
}

tasks.register("jmh") {
    group = "verification"
    description = "Runs JMH benchmarks"
}

tasks.register("revapi") {
    group = "verification"
    description = "Checks API compatibility"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "java-library")
    apply(plugin = "maven-publish")
    apply(plugin = "jacoco")
    apply(plugin = "com.github.spotbugs")
    apply(plugin = "checkstyle")
    apply(plugin = "org.owasp.dependencycheck")

    group = rootProject.group
    version = rootProject.version

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenCentral()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf(
            "-parameters",
            "-Xlint:all",
            "-Xlint:-processing",
            "-Werror"
        ))
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        finalizedBy(tasks.jacocoTestReport)
    }
    
    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required = true
            html.required = true
            csv.required = false
        }
    }
    
    jacoco {
        toolVersion = "0.8.12"
    }
    
    tasks.jacocoTestCoverageVerification {
        violationRules {
            rule {
                limit {
                    minimum = "0.80".toBigDecimal()
                }
            }
        }
    }
    
    configure<com.github.spotbugs.snom.SpotBugsExtension> {
        ignoreFailures = true
        excludeFilter = file("${rootProject.projectDir}/config/spotbugs/exclude.xml")
    }
    
    tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
        reports.create("html") {
            required = true
            outputLocation = file("${project.layout.buildDirectory.get()}/reports/spotbugs/${name}.html")
        }
        reports.create("xml") {
            required = false
        }
    }
    
    checkstyle {
        toolVersion = "10.21.0"
        configFile = file("${rootProject.projectDir}/config/checkstyle/checkstyle.xml")
        isIgnoreFailures = true
    }
    
    configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
        analyzers.assemblyEnabled = false
        analyzers.nodeEnabled = false
        suppressionFile = "${rootProject.projectDir}/config/owasp/suppressions.xml"
        failBuildOnCVSS = 7.0f
        nvd.apiKey = System.getenv("NVD_API_KEY") ?: ""
    }
    

    val junitVersion: String by project
    val assertjVersion: String by project
    val mockitoVersion: String by project

    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        testImplementation("org.assertj:assertj-core:$assertjVersion")
        testImplementation("org.mockito:mockito-core:$mockitoVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        testImplementation("org.awaitility:awaitility:4.2.2")
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
                
                pom {
                    name = project.name
                    description = "High-performance CQRS command dispatcher library for Java"
                    url = "https://github.com/dispatch4j/dispatch4j"
                    
                    licenses {
                        license {
                            name = "Apache License 2.0"
                            url = "https://www.apache.org/licenses/LICENSE-2.0"
                        }
                    }
                    
                    developers {
                        developer {
                            id = "dispatch4j"
                            name = "Dispatch4j Team"
                        }
                    }
                    
                    scm {
                        connection = "scm:git:git://github.com/dispatch4j/dispatch4j.git"
                        developerConnection = "scm:git:ssh://github.com/dispatch4j/dispatch4j.git"
                        url = "https://github.com/dispatch4j/dispatch4j"
                    }
                }
            }
        }
        
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/${System.getenv("GITHUB_REPOSITORY") ?: "dispatch4j/dispatch4j"}")
                credentials {
                    username = project.findProperty("githubUsername")?.toString() ?: System.getenv("GITHUB_ACTOR")
                    password = project.findProperty("githubToken")?.toString() ?: System.getenv("GITHUB_TOKEN")
                }
            }
            
            if (project.hasProperty("mavenCentralUsername")) {
                maven {
                    name = "MavenCentral"
                    url = if (version.toString().endsWith("SNAPSHOT")) {
                        uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    } else {
                        uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    }
                    credentials {
                        username = project.findProperty("mavenCentralUsername")?.toString()
                        password = project.findProperty("mavenCentralPassword")?.toString()
                    }
                }
            }
        }
    }
}