plugins {
    java
    `java-library`
    `maven-publish`
    jacoco
    id("io.spring.dependency-management") version "1.1.7" apply false
    id("org.springframework.boot") version "3.5.4" apply false
    id("com.github.spotbugs") version "6.2.6" apply false
    id("org.owasp.dependencycheck") version "12.1.3" apply false
    id("org.sonarqube") version "6.2.0.5505"
    id("com.diffplug.spotless") version "7.2.1"
    id("me.champeau.jmh") version "0.7.3" apply false
    id("info.solidsoft.pitest") version "1.15.0" apply false
    id("com.github.ben-manes.versions") version "0.52.0"
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
            googleJavaFormat("1.28.0")
                .aosp()
            endWithNewline()
            formatAnnotations()
            removeUnusedImports()
            trimTrailingWhitespace()
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "dispatch4j_dispatch4j")
        property("sonar.projectName", "Dispatch4j")
        property("sonar.organization", "dispatch4j")
        property("sonar.projectVersion", version)
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.language", "java")
        property("sonar.java.source", "21")
        property("sonar.java.target", "21")
        property("sonar.java.binaries", "**/build/classes/java/main")
        property("sonar.java.test.binaries", "**/build/classes/java/test")
        property("sonar.java.test.libraries", "**/build/libs/*.jar")
        property("sonar.exclusions", "**/examples/**,**/test/**,**/*Test.java,**/*Tests.java,**/build/**")
        property("sonar.cpd.exclusions", "**/examples/**")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.coverage.jacoco.xmlReportPaths", "**/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.junit.reportPaths", "**/build/test-results/test")
    }
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
        toolVersion = "0.8.13"
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
    val awaitilityVersion: String by project
    val junitLauncherVersion: String by project

    dependencies {
        testRuntimeOnly("org.junit.platform:junit-platform-launcher:$junitLauncherVersion")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        testImplementation("org.assertj:assertj-core:$assertjVersion")
        testImplementation("org.mockito:mockito-core:$mockitoVersion")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockitoVersion")
        testImplementation("org.awaitility:awaitility:$awaitilityVersion")
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