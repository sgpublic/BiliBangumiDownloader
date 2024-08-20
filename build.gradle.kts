// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(bilidl.plugins.android.application) apply false

    alias(bilidl.plugins.kotlin.android) apply false
    alias(bilidl.plugins.kotlin.plugin.parcelize) apply false
    alias(bilidl.plugins.kotlin.kapt) apply false
    alias(bilidl.plugins.kotlin.plugin.lombok) apply false

    alias(bilidl.plugins.protobuf) apply false
    alias(bilidl.plugins.lombok) apply false
    alias(bilidl.plugins.github.release) apply false
    alias(bilidl.plugins.android.assemble) apply false
}
