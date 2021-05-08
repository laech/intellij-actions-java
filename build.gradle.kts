import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.kotlin.jvm").version("1.5.0")
  id("org.jetbrains.intellij").version("0.7.3")
}

group = "com.gitlab.lae.intellij.actions.java"
version = "0.4.4-SNAPSHOT"

repositories {
  mavenCentral()
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
  kotlinOptions.jdkHome =
    javaToolchains
      .compilerFor { languageVersion.set(JavaLanguageVersion.of(8)) }
      .get().metadata.installationPath.asFile.absolutePath
}

tasks.withType<Test> {
  testLogging {
    exceptionFormat = TestExceptionFormat.FULL
  }
}

intellij {
  updateSinceUntilBuild = false
  version =
    project.properties.getOrDefault("intellijVersion", "2019.1").toString()
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
}
