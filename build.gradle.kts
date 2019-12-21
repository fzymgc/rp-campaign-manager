import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.google.cloud.tools.jib.gradle.JibExtension
import com.moowork.gradle.node.yarn.YarnTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.OffsetDateTime

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("org.jetbrains.kotlin.plugin.allopen")
    id("com.google.cloud.tools.jib")
    id("com.github.johnrengelman.shadow")
    id("application")
    id("maven-publish")
    id("nebula.release")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.node-gradle.node")
}

//version = gitVersion()
group = "dev.fzymgc.rpcampaignmanager"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
    maven { url = uri("https://jcenter.bintray.com") }
}

val micronautVersion: String by project
val kotlinVersion: String by project
val developmentOnly: Configuration by configurations.creating

dependencies {
    implementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    implementation("io.micronaut:micronaut-management")
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.configuration:micronaut-micrometer-core")
    implementation("io.micronaut:micronaut-security-jwt")
//  implementation("io.micronaut.configuration:micronaut-elasticsearch")
    implementation(
        "io.micronaut.kubernetes:micronaut-kubernetes-discovery-client"
    )
    implementation("javax.annotation:javax.annotation-api")
    implementation("io.micronaut:micronaut-tracing")
    implementation("io.micronaut.configuration:micronaut-cassandra")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut.configuration:micronaut-mongo-reactive")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("io.micronaut.graphql:micronaut-graphql")
    implementation("io.micronaut:micronaut-http-server-netty")
    implementation("io.projectreactor:reactor-core")
    implementation(
        "io.projectreactor.kotlin:reactor-kotlin-extensions:1.0.1.RELEASE"
    )
    kapt(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kapt("io.micronaut:micronaut-inject-java")
    kapt("io.micronaut:micronaut-validation")
    kapt("io.micronaut.configuration:micronaut-openapi")
    kaptTest(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    kaptTest("io.micronaut:micronaut-inject-java")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    runtimeOnly("io.jaegertracing:jaeger-thrift")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    testImplementation(platform("io.micronaut:micronaut-bom:$micronautVersion"))
    testImplementation("io.micronaut.test:micronaut-test-kotlintest")
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit"))
    testImplementation("io.mockk:mockk:1.9.3")
    testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.20")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:2.0.1")
}

configure<JibExtension> {
    from {
        image = "azul/zulu-openjdk-alpine:11"
    }
    to {
        image =
            "docker.pkg.github.com/fzymgc/package-repository/rp-campaign-manager"
//    tags = setOf(
//      "${project.version}".replace(Regex("""[\+]"""),"_")
//    )

        auth {
            username = project.findProperty("gpr.user") as String? ?: System.getenv(
                "GPR_USERNAME"
            )
            password =
                project.findProperty("gpr.password") as String? ?: System.getenv(
                    "GPR_PASSWORD"
                )
        }
    }
    container {
        jvmFlags = "-Dcom.sun.management.jmxremote -noverify".split(" ")
        labels = mapOf(
            "dev.fzymgc.version" to "${project.version}",
            "dev.fzymgc.application" to "rp-campaign-manager",
            "dev.fzymgc.created-at" to "${OffsetDateTime.now()}"
        )
    }
}

val test by tasks.getting(Test::class) {
    classpath += developmentOnly
    useJUnitPlatform()
}

application {
    mainClassName = "dev.fzymgc.rpcampaignmanager.Application"
}

allOpen {
    annotation("io.micronaut.aop.Around")
}

val compileKotlin by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = "1.8"
        //Will retain parameter names for Java reflection
        javaParameters = true
    }
}

val compileTestKotlin: KotlinCompile by tasks.getting(KotlinCompile::class) {
    kotlinOptions {
        jvmTarget = "1.8"
        javaParameters = true
    }
}

val shadowJar by tasks.getting(ShadowJar::class) {
    mergeServiceFiles()
}

val run by tasks.getting(JavaExec::class) {
    classpath += developmentOnly
    jvmArgs(
        "-noverify", "-XX:TieredStopAtLevel=1", "-Dcom.sun.management.jmxremote"
    )
}

publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/fzymgc/package-repository")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv(
                    "GPR_USERNAME"
                )
                password =
                    project.findProperty("gpr.password") as String? ?: System.getenv(
                        "GPR_PASSWORD"
                    )
            }
        }
    }
    publications {
        create<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}

detekt {
    config = files("${rootProject.rootDir}/detekt-config.yml")
}

node {
    version = "13.3.0"
    yarnVersion = "1.21.1"
    download = true
    nodeModulesDir = file("${project.projectDir}/src/main/web-app/node_modules")
}

tasks.register<YarnTask>("clientAppStart") {
    dependsOn("yarn")
    group = "application"
    description = "run the client web app"
    args = "run start".split(" ")
    setWorkingDir(file("${project.projectDir}/src/main/web-app"))
}

tasks.register<YarnTask>("clientAppBuild") {
    dependsOn("yarn", "processResources")
    group = "build"
    description = "Build the client bundle"
    args = "run build".split(" ")
    setWorkingDir(file("${project.projectDir}/src/main/web-app"))
    inputs.files(fileTree("${project.projectDir}/src/main/web-app") {
        include("package.json")
        include("yarn.lock")
        include("public/**")
        include("src/**")
    })
    outputs.dir(file("${project.projectDir}/src/main/web-app/dist"))
}

tasks.register<YarnTask>("clientAppTest") {
    dependsOn("yarn")
    group = "verification"
    description = "Run the client tests"
    args = "run test".split(" ")
    setWorkingDir(file("${project.projectDir}/src/main/web-app"))
}

tasks.register<Delete>("clientAppBuildClean") {
    group = "build"
    description = "Clean the client bundle build"
    delete("${project.projectDir}/src/main/web-app/dist")
}

tasks.getByPath("assemble").dependsOn("clientAppBuild")
tasks.getByPath("clean").dependsOn("clientAppBuildClean")
