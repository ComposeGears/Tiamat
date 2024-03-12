plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        create("io.github.composegears.m2p") {
            id = "io.github.composegears.m2p"
            implementationClass = "M2P"
        }
    }
}