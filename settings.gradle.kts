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
        maven { url = uri("https://jitpack.io") }
        // ANT+ SDK isn't easily available on public modern mavens.
        // We will include it via jitpack or local aar if necessary, but for now we'll rely on the open source versions.
    }
}

rootProject.name = "ANT+_Scanner"
include(":app")
