pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        val localProperties = java.util.Properties().apply {
            val file = rootProject.projectDir.resolve("local.properties")
            if (file.exists()) load(file.inputStream())
        }
        maven {
            url = uri("https://cardinalcommerceprod.jfrog.io/artifactory/android")
            credentials {
                username = localProperties.getProperty("cardinal.username")
                password = localProperties.getProperty("cardinal.password")
            }
        }
    }
}

rootProject.name = "PasCher"
include(":app")
 