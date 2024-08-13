plugins {
    id("buildlogic.kotlin-library-conventions")
}

dependencies {
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.2")
}

tasks.processTestResources {
    from("../../formula-test") {
        include("*.yml")
    }
}