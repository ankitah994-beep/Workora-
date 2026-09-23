herepluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // सुरक्षित अल्टरनेटिव सर्वर (यदि मुख्य सर्वर डाउन या ब्लॉक हो)
        maven { url = uri("https://repo1.maven.org/maven2/") }
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
}

rootProject.name = "Workora"
include(":app")
