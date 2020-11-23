package com.ucasoft.kcron

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.*
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class KCronJobTests {
    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder().setMinimumLoggingLevel(Log.DEBUG).setExecutor(SynchronousExecutor()).build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun simpleKCronJobRun() {
        val jobId = KCronJobManager.getInstance(context).runJob<MyJob>("0/5 * * ? * * *")
        assertNotNull(jobId)
    }

    class MyJob(context: Context, workerParams: WorkerParameters) : KCronJob(context, workerParams) {

        override fun doCronJob(): KCronJobResult {
            return KCronJobResult.Success
        }
    }
}