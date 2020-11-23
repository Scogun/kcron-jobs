package com.ucasoft.kcron

import android.content.Context
import androidx.work.*
import java.util.*

actual abstract class KCronJob(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        if (doCronJob() == KCronJobResult.Failure) {
            return Result.failure()
        }

        val cronExpression = inputData.getString(cronExpressionKey)!!
        val uuidTag = UUID.fromString(tags.elementAt(1))!!
        KCronJobManager.getInstance(context).runJob(this::class.java, cronExpression, uuidTag)
        return Result.success()
    }

    actual abstract fun doCronJob(): KCronJobResult

    companion object {
        private const val cronExpressionKey = "cronExpression"
    }
}