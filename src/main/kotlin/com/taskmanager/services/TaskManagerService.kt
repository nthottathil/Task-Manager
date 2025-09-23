package com.taskmanager.services

// Fix these imports - use full paths
import com.taskmanager.models.Priority
import com.taskmanager.models.Task
import com.taskmanager.models.TaskStatus
import com.taskmanager.models.TaskStatistics
import com.taskmanager.repository.InMemoryTaskRepository
import com.taskmanager.repository.TaskRepository
import java.time.LocalDateTime
import java.util.PriorityQueue

class TaskManagerService(
    private val repository: TaskRepository = InMemoryTaskRepository()
) {
    private val taskQueue = PriorityQueue<Task>()

    fun createTask(
        title: String,
        description: String,
        priority: Priority,
        dueDate: LocalDateTime? = null,
        tags: Set<String> = emptySet()
    ): Task {
        require(title.isNotBlank()) { "Task title cannot be blank" }
        require(description.isNotBlank()) { "Task description cannot be blank" }

        val task = Task(
            title = title,
            description = description,
            priority = priority,
            dueDate = dueDate,
            tags = tags
        )

        if (repository.addTask(task)) {
            taskQueue.offer(task)
            return task
        } else {
            throw IllegalStateException("Failed to create task")
        }
    }

    fun getTask(id: String): Task? = repository.getTask(id)

    fun getAllTasks(): List<Task> = repository.getAllTasks()

    fun getHighestPriorityTask(): Task? {
        while (taskQueue.isNotEmpty()) {
            val task = taskQueue.peek()
            val currentTask = repository.getTask(task.id)

            if (currentTask == null ||
                currentTask.status == TaskStatus.COMPLETED ||
                currentTask.status == TaskStatus.CANCELLED) {
                taskQueue.poll()
            } else {
                return taskQueue.peek()
            }
        }
        return null
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus): Boolean {
        val task = repository.getTask(taskId) ?: return false
        val updatedTask = task.copy(status = newStatus)
        return repository.updateTask(updatedTask)
    }

    fun updateTask(task: Task): Boolean = repository.updateTask(task)

    fun deleteTask(taskId: String): Boolean = repository.deleteTask(taskId)

    fun searchTasks(query: String): List<Task> = repository.searchTasks(query)

    fun getTasksByStatus(status: TaskStatus): List<Task> {
        return repository.getTasksByStatus(status).sortedBy { it }
    }

    fun getTasksByPriority(priority: Priority): List<Task> {
        return repository.getTasksByPriority(priority).sortedBy { it }
    }

    fun getOverdueTasks(): List<Task> {
        val now = LocalDateTime.now()
        return repository.getAllTasks()
            .filter { task ->
                task.status != TaskStatus.COMPLETED &&
                        task.status != TaskStatus.CANCELLED &&
                        task.dueDate != null &&
                        task.dueDate < now
            }
            .sortedBy { it.dueDate }
    }

    fun getTaskStats(): TaskStatistics {
        val allTasks = repository.getAllTasks()
        return TaskStatistics(
            total = allTasks.size,
            pending = allTasks.count { it.status == TaskStatus.PENDING },
            inProgress = allTasks.count { it.status == TaskStatus.IN_PROGRESS },
            completed = allTasks.count { it.status == TaskStatus.COMPLETED },
            cancelled = allTasks.count { it.status == TaskStatus.CANCELLED },
            overdue = getOverdueTasks().size
        )
    }
}