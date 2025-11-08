plugins {
    // Ajusta estas versiones a las que necesitas (ej. las de tu 'buildscript')
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false

    // ESTA ES LA LÍNEA QUE SOLUCIONA TU ERROR:
    // Le dice a Gradle dónde encontrar el plugin "safeargs"
    id("androidx.navigation.safeargs.kotlin") version "2.7.6" apply false
}
