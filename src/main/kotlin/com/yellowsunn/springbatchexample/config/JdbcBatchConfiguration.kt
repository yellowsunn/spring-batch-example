package com.yellowsunn.springbatchexample.config

import com.yellowsunn.springbatchexample.domain.Customer
import com.yellowsunn.springbatchexample.domain.Customer2
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.support.MariaDBPagingQueryProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import java.sql.ResultSet
import javax.sql.DataSource

@Configuration
class JdbcBatchConfiguration(
    private val dataSource: DataSource,
    private val jobRepository: JobRepository,
) {
    @Bean
    fun jdbcBatchJob(jdbcBatchStep: Step): Job {
        return JobBuilder("jdbcBatchJob", jobRepository)
            .start(jdbcBatchStep)
            .build()
    }

    @Bean
    fun jdbcBatchStep(transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("jdbcBatchStep", jobRepository)
            .chunk<Customer, Customer2>(10, transactionManager)
            .reader(jdbcItemReader())
            .processor(jdbcItemProcessor())
            .writer(jdbcItemWriter())
            .build()
    }

    @Bean
    fun jdbcItemReader(): JdbcPagingItemReader<Customer> {
        val jdbcPagingItemReader =
            JdbcPagingItemReader<Customer>().apply {
                setDataSource(dataSource)
                setFetchSize(10)
                setRowMapper { rs: ResultSet, rowNum: Int ->
                    Customer(
                        id = rs.getLong("id"),
                        firstName = rs.getString("first_name"),
                        lastName = rs.getString("last_name"),
                    )
                }
            }

        val queryProvider =
            MariaDBPagingQueryProvider().apply {
                setSelectClause("id, first_name, last_name")
                setFromClause("from customer")
                sortKeys =
                    mapOf(
                        "id" to Order.ASCENDING,
                    )
            }

        jdbcPagingItemReader.setQueryProvider(queryProvider)
        return jdbcPagingItemReader
    }

    @Bean
    fun jdbcItemProcessor(): ItemProcessor<Customer, Customer2> {
        return ItemProcessor<Customer, Customer2> { customer: Customer ->
            Customer2("${customer.firstName} ${customer.lastName}")
        }
    }

    @Bean
    fun jdbcItemWriter(): JdbcBatchItemWriter<Customer2> {
        return JdbcBatchItemWriterBuilder<Customer2>()
            .dataSource(dataSource)
            .sql("insert into customer2(full_name, created_at) values (:fullName, :createdAt)")
            .beanMapped()
            .build()
    }
}
