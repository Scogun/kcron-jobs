package com.ucasoft.kcron

import android.content.Context
import androidx.work.OneTimeWorkRequest
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
            .setInputData(workDataOf("cronExpression" to expression)).build()
        if (workManager.enqueue(request).result.isDone) {
            return innerUUID
        }
        throw Exception()
    }

    companion object {

        @Volatile
        private var instance: KCronJobManager? = null

        fun getInstance(context: Context): KCronJobManager =
            instance ?: synchronized(this) {
                instance ?: KCronJobManager(context).also { instance = it }
            }

        internal fun calculateDelaySeconds(expression: String) : Long {
            val nextRun = KCron.parseAndBuild(expression).nextRun ?: return -1
            return Clock.System.now().until(nextRun.toInstant(TimeZone.currentSystemDefault()), DateTimeUnit.SECOND, TimeZone.currentSystemDefault())
        }
    }
}