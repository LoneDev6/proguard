/*
 * ProGuard -- shrinking, optimization, obfuscation, and preverification
 *             of Java bytecode.
 *
 * Copyright (c) 2002-2021 Guardsquare NV
 */

package proguard.gradle.plugin.android.dsl

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.string.shouldContain
import testutils.AndroidProject
import testutils.applicationModule
import testutils.createGradleRunner
import testutils.createTestKitDir

class ConfigurationTest : FreeSpec({
    val testKitDir = createTestKitDir()

    "Given a project with a configuration block specifying a ProGuard configuration file that does not exist" - {
        val project = autoClose(AndroidProject().apply {
            addModule(applicationModule("app", buildDotGradle = """
            plugins {
                id 'com.android.application'
                id 'proguard'
            }
            android {
                compileSdkVersion 30

                buildTypes {
                    release {
                        minifyEnabled false
                    }
                }
            }

            proguard {
                configurations {
                    release {
                        configuration 'non-existing-file.txt'
                    }
                }
            }""".trimIndent()))
        }.create())

        "When the project is evaluated" - {
            val result = createGradleRunner(project.rootDir, testKitDir).buildAndFail()

            "Then the build should fail with an error message" {
                result.output shouldContain "ProGuard configuration file .*non-existing-file.txt was set but does not exist.".toRegex()
            }
        }
    }

    "Given a project with a configuration for a minified variant" - {
        val project = autoClose(AndroidProject().apply {
            addModule(applicationModule("app", buildDotGradle = """
            plugins {
                id 'com.android.application'
                id 'proguard'
            }
            android {
                compileSdkVersion 30

                buildTypes {
                    release {
                        minifyEnabled true
                    }
                }
            }

            proguard {
                configurations {
                    release {
                        defaultConfiguration 'proguard-android.txt'
                    }
                }
            }""".trimIndent()))
        }.create())


        "When the project is evaluated" - {
            val result = createGradleRunner(project.rootDir, testKitDir).buildAndFail()

            "Then the build should fail with an error message" {
                result.output shouldContain "The option 'minifyEnabled' is set to 'true' for variant 'release', but should be 'false' for variants processed by ProGuard"
            }
        }
    }
})