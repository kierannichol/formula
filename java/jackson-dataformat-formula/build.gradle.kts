plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    implementation(project(":formula-java"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")
}