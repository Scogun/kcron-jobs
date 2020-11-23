package com.ucasoft.kcron

expect abstract class KCronJob {

    abstract fun doCronJob() : KCronJobResult
}