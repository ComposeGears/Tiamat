plugins {
    kotlin("jvm")
    alias(libs.plugins.m2p)
}

version = "1.0.0"

dependencies {
    implementation(projects.tiamatDestinations)
    implementation(libs.ksp.api)
}

m2p {
    description = "Tiamat Destinations Processor"
}