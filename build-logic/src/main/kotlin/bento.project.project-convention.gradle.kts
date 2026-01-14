/*******************************************************************************
This is an unpublished work of SAIC.
Copyright (c) 2019 SAIC. All Rights Reserved.
 ******************************************************************************/

import software.coley.gradle.artifacts.TestFxAlignmentRule
import bento.gradle.lifecycle.BuildLifecycle.ALL_CLASSES_TASK_NAME
import bento.gradle.lifecycle.TestLifecycle.enableJacoco
import bento.gradle.lifecycle.TestLifecycle.getTestReportMode
import bento.gradle.lifecycle.TestReportMode.ALL
import bento.gradle.lifecycle.TestReportMode.CI
import bento.gradle.lifecycle.TestReportMode.DEV
import bento.gradle.project.ProjectConstants.JAVA_VERSION
import bento.gradle.project.JvmTarget
import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition.JAVAX_ACTIVATION_API
import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition.JAVAX_ANNOTATION_API
import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition.JAVAX_INJECT_API
import org.gradlex.jvm.dependency.conflict.detection.rules.CapabilityDefinition.JAVAX_VALIDATION_API
import org.gradlex.jvm.dependency.conflict.resolution.JvmDependencyConflictsExtension

plugins {
    id("java-library")
    id("com.autonomousapps.dependency-analysis")
    id("jacoco")
    id("bento.project.build-lifecycle")
    id("jvm-test-suite")
    id("bento.test.unit-test-suite")
    id("org.gradlex.jvm-dependency-conflict-detection")
    id("org.gradlex.jvm-dependency-conflict-resolution")
}

// Version catalog type-safe accessors not available in
// precompiled script plugins:
// https://github.com/gradle/gradle/issues/15383.
// Using version catalog API instead.
val versionCatalog = versionCatalogs.named("libs")

// Accommodate all the different ways Gradle and its plugins take the Java
// version...
val javaMajorVersionAsInt: Int = JAVA_VERSION.majorVersion.toInt()
val javaLanguageVersion: JavaLanguageVersion =
    JavaLanguageVersion.of(JAVA_VERSION.toString())
val jvmTargetVersion: JvmTarget = JvmTarget.fromTarget(JAVA_VERSION.toString())

val csvCodeCoverageRequired = false
val htmlCodeCoverageRequired = false
val xmlCodeCoverageRequired = true

val testReportMode = getTestReportMode(project)
val enableJacoco = enableJacoco(project)

configure<JvmDependencyConflictsExtension> {

    conflictResolution {
        select(JAVAX_ACTIVATION_API, "jakarta.activation:jakarta.activation-api")
        select(JAVAX_ANNOTATION_API, "jakarta.annotation:jakarta.annotation-api")
        select(JAVAX_INJECT_API, "jakarta.inject:jakarta.inject-api")
        select(JAVAX_VALIDATION_API, "jakarta.validation:jakarta.validation-api")
    }
}

dependencies {

    components.all<TestFxAlignmentRule>()

    api(platform(project(":platform")))
}

java {
    // Should be removed if project becomes modularized
    modularity.inferModulePath.set(false)
}

jacoco {

    val jacocoVersion =
        versionCatalog.findVersion("jacoco").get().requiredVersion

    toolVersion = jacocoVersion
}

@Suppress("UnstableApiUsage")
testing {
    suites {
        configureEach {
            if (this is JvmTestSuite) {

                val junitVersion =
                    versionCatalog.findVersion("junit").get()
                        .requiredVersion

                useJUnitJupiter(junitVersion)

                targets {
                    all {
                        testTask.configure {
                            // Set system properties for the test JVM(s)
                            systemProperty("jregw.home", rootDir.path)
                            systemProperty("project.name", rootProject.name)
                            // Turns out we really DO need a graphics environment
                            // to initialize the JavaFx platform used by some
                            // JUnit tests. Therefore, we can NOT run JUnit
                            // tests headless.
                            systemProperty("java.awt.headless", "false")
                            systemProperty("spring.main.banner-mode", "off")

                            extensions.configure(JacocoTaskExtension::class) {
                                isEnabled = enableJacoco
                            }
                        }
                    }
                }

                dependencies {

                    implementation(platform(project(":platform")))

                    implementation(
                        versionCatalog.findLibrary("assertj-core").get()
                    )
                    implementation(
                        versionCatalog.findLibrary("junit-jupiter-api").get()
                    )
                }
            }
        }
    }
}

tasks {

    val classes by existing

    named(ALL_CLASSES_TASK_NAME) {
        dependsOn(classes)
    }

    register<DependencyReportTask>("allDependencies").configure {
        description = "Displays all dependencies declared in all subprojects."
        group = "help"
    }

    named<JacocoReport>("jacocoTestReport").configure {

        enabled = enableJacoco

        reports {
            csv.required.set(csvCodeCoverageRequired)
            html.required.set(htmlCodeCoverageRequired)
            xml.required.set(xmlCodeCoverageRequired)
        }
    }

    // Use the specific JAR task names (e.g. jar, javadocJar, etc.) instead of
    // withType<Jar> to prevent using the same file names and overwriting JAR
    // files when other JAR tasks are executed.
    named<Jar>("jar").configure {

        // Name JARs using full project name, based on the project path.
        // Otherwise, we will have JARs with the same names that will collide
        // when aggregated into the lib directory during installation.
        val projectJarName =
            "${project.path}.jar"
                // Delete the leading ':'
                .substring(1)
                // Replace the remaining ':' with '.'
                .replace(':', '.')

        archiveFileName.set(projectJarName)
    }

    // Create custom MANIFEST.MF entries for all JARs.
    withType<Jar>().configureEach {

        val buildJdk = provider {
            val javaVendor = System.getProperty("java.vendor")
            val javaVersion = System.getProperty("java.version")
            "$javaVendor $javaVersion"
        }
        val buildOs = provider {
            val osName = System.getProperty("os.name")
            val osArch = System.getProperty("os.arch")
            "$osName $osArch"
        }
        val buildTool = "Gradle ${gradle.gradleVersion}"
        val implementationTitle = project.description ?: project.name

        manifest {
            attributes(
                "Build-JDK" to buildJdk,
                "Build-OS" to buildOs,
                "Build-Tool" to buildTool,
                "Implementation-Title" to implementationTitle,
                "Implementation-Vendor" to "SAIC",
                "Manifest-Version" to 1.0,
                "Specification-Title" to "JREGW",
                "Specification-Vendor" to "SAIC",
            )
        }
    }

    withType<JavaCompile>().configureEach {

        with(options) {
            // The character encoding to be used when reading source files into
            // the Java compiler. Defaults to null, in which case the platform
            // default encoding will be used. This is separable from the value
            // set in gradle.properties, which (currently) specifies encoding as
            // UTF-8. UTF-8 is a multibyte encoding that can represent any
            // Unicode character. ISO 8859-1 is a single-byte encoding that can
            // represent the first 256 Unicode characters. Both encode ASCII
            // exactly the same way.
            encoding = "UTF-8"

            release = javaMajorVersionAsInt

            // Required for Spring 6.1+:
            // https://github.com/spring-projects/spring-framework/wiki/Spring-Framework-6.1-Release-Notes#parameter-name-retention
            compilerArgs.add("-parameters")

            isFork = true
            forkOptions.memoryMaximumSize = "1g"
        }
    }

    withType<JavaExec>().configureEach {

        javaLauncher = javaToolchains.launcherFor {
            languageVersion = javaLanguageVersion
        }
    }

    withType<Test>().configureEach {

        javaLauncher = javaToolchains.launcherFor {
            languageVersion = javaLanguageVersion
        }

        reports.html.required.set(testReportMode in listOf(ALL, DEV))
        reports.junitXml.required.set(testReportMode in listOf(ALL, CI))
    }
}
