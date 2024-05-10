package com.yellowsunn.springbatchexample.config

import org.slf4j.LoggerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.tasklet.TaskletStep
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SimpleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    fun simpleJob(
        simpleStep1: Step,
        simpleStep2: Step,
    ): Job {
        return JobBuilder("simpleJob", jobRepository)
            .start(simpleStep1)
            .next(simpleStep2)
            .build()
    }

    @Bean
    @JobScope
    fun simpleStep1(
        @Value("#{jobParameters['requestDate']}") requestDate: String,
    ): Step {
        return StepBuilder("simpleStep1", jobRepository)
            .tasklet({ contribution, chunkContext ->
                logger.info("step1 executed.requestDate={}", requestDate)
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun simpleStep2(): TaskletStep {
        return StepBuilder("simpleStep2", jobRepository)
            .tasklet({ contribution, chunkContext ->
                logger.info("step2 executed.")
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }
}
