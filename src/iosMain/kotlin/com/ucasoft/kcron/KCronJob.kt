package com.ucasoft.kcron

actual abstract class KCronJob {

    actual abstract fun doCronJob(): KCronJobResult
}