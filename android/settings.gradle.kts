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
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TestMarkaz"
include(":app")

// Core modules
include(":core:ui")
include(":core:data")
include(":core:domain")

// Feature modules
include(":feature:home")
include(":feature:test")
include(":feature:progress")
include(":feature:downloads")
include(":feature:profile")
