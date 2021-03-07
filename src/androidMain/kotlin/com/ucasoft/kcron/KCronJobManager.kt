package com.ucasoft.kcron

import android.content.Context
import androidx.work.OneTimeWorkRequest
import androidx.work.Operation
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import java.lang.Exception
import java.util.*
import java.util.concurrent.TimeUnit

class KCronJobManager(context: Context) {

    private val workManager = WorkManager.getInstance(context)

    inline fun <reified J : KCronJob> runJob(expression: String) : UUID? {
        return runJob(J::class.java, expression, UUID.randomUUID())
    }

    fun runJob(clazz: Class<out KCronJob>, expression: String, innerUUID: UUID) : UUID? {
        val delaySeconds = calculateDelaySeconds(expression)
        if (delaySeconds < 0) {
            return null
        }
        val request = OneTimeWorkRequest.Builder(clazz)
            .addTag(innerUUID.toString())
            .setInitialDelay(delaySeconds, TimeUnit.SECONDS)
            .setInputData(workDataOf("cronExpression" to expression, "innerId" to innerUUID.toString())).build()
        val result = workManager.enqueue(request).result.get()
        if (result is Operation.State.SUCCESS) {
            return innerUUID
        }
        throw Exception()
    }

    fun removeJob(jobUUID: UUID) {
        workManager.cancelAllWorkByTag(jobUUID.toString())
    }

    companion object {

        @Volatile
        private var instance: KCronJobManager? = null

        fun getInstance(context: Context): KCronJobManager =
            instance ?: synchronized(this) {
                instance ?: KCronJobManager(context).also { instance = it }
            }

        internal fun calculateDelaySeconds(expression: String) : Long {
            val nextRun = KCron.parseAndBuild(expression).nextRunList(2) ?: return -1
            var delay = Clock.System.now().until(
                nextRun[0].toInstant(TimeZone.currentSystemDefault()),
                DateTimeUnit.SECOND,
                TimeZone.currentSystemDefault()
            )
            if (delay < 1) {
                delay = Clock.System.now().until(
                    nextRun[1].toInstant(TimeZone.currentSystemDefault()),
                    DateTimeUnit.SECOND,
                    TimeZone.currentSystemDefault()
                )
            }
            return delay
        }
    }
}