package ch.ergon.studentmarks.infrastructure.security

import ch.ergon.studentmarks.infrastructure.security.authentication.JobAuthentication
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.AsyncConfigurer
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.scheduling.DelegatingSecurityContextTaskScheduler
import org.springframework.security.task.DelegatingSecurityContextAsyncTaskExecutor
import java.util.concurrent.Executor

/**
 * Runs `@Async` and `@Scheduled` methods as [JobAuthentication], never as the person who triggered them.
 */
@Configuration
class JobActorConfiguration(
    @param:Lazy
    @param:Qualifier(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
    private val applicationTaskExecutor: AsyncTaskExecutor,
    @param:Lazy private val taskScheduler: TaskScheduler,
) : AsyncConfigurer,
    SchedulingConfigurer {
    private val jobContext = SecurityContextHolder.createEmptyContext().apply { authentication = JobAuthentication }

    override fun getAsyncExecutor(): Executor = DelegatingSecurityContextAsyncTaskExecutor(applicationTaskExecutor, jobContext)

    override fun configureTasks(taskRegistrar: ScheduledTaskRegistrar) {
        taskRegistrar.setTaskScheduler(DelegatingSecurityContextTaskScheduler(taskScheduler, jobContext))
    }
}
