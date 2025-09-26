// FILE: src/main/kotlin/SimpleTaskApp.kt
// NO PACKAGE DECLARATION - This file is in the root kotlin folder

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

// ============== DATA MODELS ==============
data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: Priority,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val tags: Set<String> = emptySet()
) {
    fun getSummary(): String {
        val dueDateStr = dueDate?.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) ?: "No due date"
        return "$title (${priority.name}) - $status - Due: $dueDateStr"
    }
}

enum class Priority(val value: Int, val emoji: String) {
    LOW(1, "🟢"),
    MEDIUM(2, "🟡"),
    HIGH(3, "🟠"),
    CRITICAL(4, "🔴");

    override fun toString(): String = "$emoji $name"
}

enum class TaskStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class TaskStatistics(
    val total: Int,
    val pending: Int,
    val inProgress: Int,
    val completed: Int,
    val cancelled: Int,
    val overdue: Int
) {
    val completionRate: Double = if (total > 0) completed.toDouble() / total * 100 else 0.0

    fun display() {
        println("   Total Tasks: $total")
        println("   ├─ Pending: $pending")
        println("   ├─ In Progress: $inProgress")
        println("   ├─ Completed: $completed")
        println("   ├─ Cancelled: $cancelled")
        println("   ├─ Overdue: $overdue")
        println("   └─ Completion Rate: ${"%.1f".format(completionRate)}%")
    }
}

// ============== TASK MANAGER ==============
class TaskManager {
    private val tasks = mutableMapOf<String, Task>()
    private val taskQueue = PriorityQueue<Task> { a, b ->
        // Higher priority first, then earlier due date
        val priorityComp = b.priority.value.compareTo(a.priority.value)
        if (priorityComp != 0) priorityComp
        else {
            when {
                a.dueDate == null && b.dueDate == null -> 0
                a.dueDate == null -> 1
                b.dueDate == null -> -1
                else -> a.dueDate.compareTo(b.dueDate)
            }
        }
    }

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

        tasks[task.id] = task
        taskQueue.offer(task)
        return task
    }

    fun getTask(id: String): Task? = tasks[id]

    fun getAllTasks(): List<Task> = tasks.values.toList()

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus): Boolean {
        val task = tasks[taskId] ?: return false
        tasks[taskId] = task.copy(status = newStatus)
        return true
    }

    fun deleteTask(taskId: String): Boolean = tasks.remove(taskId) != null

    fun getTasksByStatus(status: TaskStatus): List<Task> {
        return tasks.values.filter { it.status == status }
            .sortedByDescending { it.priority.value }
    }

    fun getHighestPriorityTask(): Task? {
        // Clean up completed/cancelled tasks from queue
        while (taskQueue.isNotEmpty()) {
            val task = taskQueue.peek()
            val currentTask = tasks[task.id]

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

    fun searchTasks(keyword: String): List<Task> {
        val lowercaseKeyword = keyword.lowercase()
        return tasks.values.filter { task ->
            task.title.lowercase().contains(lowercaseKeyword) ||
                    task.description.lowercase().contains(lowercaseKeyword) ||
                    task.tags.any { it.lowercase().contains(lowercaseKeyword) }
        }
    }

    fun getOverdueTasks(): List<Task> {
        val now = LocalDateTime.now()
        return tasks.values.filter { task ->
            task.status != TaskStatus.COMPLETED &&
                    task.status != TaskStatus.CANCELLED &&
                    task.dueDate != null &&
                    task.dueDate < now
        }.sortedBy { it.dueDate }
    }

    fun getTaskStats(): TaskStatistics {
        val allTasks = tasks.values.toList()
        return TaskStatistics(
            total = allTasks.size,
            pending = allTasks.count { it.status == TaskStatus.PENDING },
            inProgress = allTasks.count { it.status == TaskStatus.IN_PROGRESS },
            completed = allTasks.count { it.status == TaskStatus.COMPLETED },
            cancelled = allTasks.count { it.status == TaskStatus.CANCELLED },
            overdue = getOverdueTasks().size
        )
    }

    fun displayAllTasks() {
        if (tasks.isEmpty()) {
            println("   No tasks found.")
            return
        }

        tasks.values.sortedByDescending { it.priority.value }.forEach { task ->
            println("   ${task.getSummary()}")
        }
    }
}

// ============== MAIN APPLICATION ==============
fun main() {
    // Header
    println("╔════════════════════════════════════════════════╗")
    println("║      🎯 SIMPLE TASK MANAGER v1.0.0 🎯         ║")
    println("╚════════════════════════════════════════════════╝")
    println()

    val taskManager = TaskManager()
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")

    // Create sample tasks
    println("📝 Creating Sample Tasks...")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━")

    val task1 = taskManager.createTask(
        title = "Implement Binary Search Tree",
        description = "Create a balanced BST with insert, delete, and search operations",
        priority = Priority.HIGH,
        dueDate = LocalDateTime.now().plusDays(2),
        tags = setOf("algorithm", "data-structure", "interview")
    )
    println("✅ Created: ${task1.title}")
    println("   Priority: ${task1.priority}")
    println("   Due: ${task1.dueDate?.format(formatter)}")
    println()

    val task2 = taskManager.createTask(
        title = "Review System Design Patterns",
        description = "Study microservices, load balancing, and caching strategies",
        priority = Priority.CRITICAL,
        dueDate = LocalDateTime.now().plusDays(1),
        tags = setOf("system-design", "architecture")
    )
    println("✅ Created: ${task2.title}")
    println("   Priority: ${task2.priority}")
    println("   Due: ${task2.dueDate?.format(formatter)}")
    println()

    val task3 = taskManager.createTask(
        title = "Practice Dynamic Programming",
        description = "Solve classic DP problems like knapsack and longest subsequence",
        priority = Priority.MEDIUM,
        dueDate = LocalDateTime.now().plusDays(3),
        tags = setOf("algorithm", "dynamic-programming")
    )
    println("✅ Created: ${task3.title}")
    println("   Priority: ${task3.priority}")
    println("   Due: ${task3.dueDate?.format(formatter)}")
    println()

    val task4 = taskManager.createTask(
        title = "Write Unit Tests",
        description = "Add comprehensive test coverage for the task manager",
        priority = Priority.LOW,
        dueDate = LocalDateTime.now().plusDays(5),
        tags = setOf("testing", "quality")
    )
    println("✅ Created: ${task4.title}")
    println("   Priority: ${task4.priority}")
    println("   Due: ${task4.dueDate?.format(formatter)}")
    println()

    // Create an overdue task
    val overdueTask = taskManager.createTask(
        title = "Submit Project Proposal",
        description = "Final project proposal submission",
        priority = Priority.CRITICAL,
        dueDate = LocalDateTime.now().minusDays(1),
        tags = setOf("urgent", "project")
    )
    println("⚠️  Created OVERDUE: ${overdueTask.title}")
    println("   Priority: ${overdueTask.priority}")
    println("   Due: ${overdueTask.dueDate?.format(formatter)} (OVERDUE!)")

    // Display highest priority task
    println("\n🎯 Highest Priority Task:")
    println("━━━━━━━━━━━━━━━━━━━━━━━━")
    taskManager.getHighestPriorityTask()?.let { task ->
        println("   ${task.getSummary()}")
        println("   Description: ${task.description}")
        println("   Tags: ${task.tags.joinToString(", ")}")
    }

    // Update some task statuses
    println("\n📊 Updating Task Statuses:")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━")
    taskManager.updateTaskStatus(task2.id, TaskStatus.IN_PROGRESS)
    println("   ✓ '${task2.title}' → IN_PROGRESS")

    taskManager.updateTaskStatus(task3.id, TaskStatus.IN_PROGRESS)
    println("   ✓ '${task3.title}' → IN_PROGRESS")

    taskManager.updateTaskStatus(task4.id, TaskStatus.COMPLETED)
    println("   ✓ '${task4.title}' → COMPLETED")

    // Search functionality
    println("\n🔍 Search Results for 'algorithm':")
    println("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
    val searchResults = taskManager.searchTasks("algorithm")
    if (searchResults.isEmpty()) {
        println("   No tasks found.")
    } else {
        searchResults.forEach { task ->
            println("   • ${task.title}")
            println("     Tags: ${task.tags.joinToString(", ")}")
        }
    }

    // Show overdue tasks
    println("\n⏰ Overdue Tasks:")
    println("━━━━━━━━━━━━━━━━")
    val overdueTasks = taskManager.getOverdueTasks()
    if (overdueTasks.isEmpty()) {
        println("   No overdue tasks! Great job! 👍")
    } else {
        overdueTasks.forEach { task ->
            val daysOverdue = java.time.Duration.between(task.dueDate, LocalDateTime.now()).toDays()
            println("   ⚠️  ${task.title} - ${daysOverdue} day(s) overdue")
        }
    }

    // Display tasks by status
    println("\n📋 Tasks by Status:")
    println("━━━━━━━━━━━━━━━━━")
    TaskStatus.values().forEach { status ->
        val tasksWithStatus = taskManager.getTasksByStatus(status)
        if (tasksWithStatus.isNotEmpty()) {
            println("   $status (${tasksWithStatus.size}):")
            tasksWithStatus.forEach { task ->
                println("     • ${task.title} ${task.priority}")
            }
        }
    }

    // Display statistics
    println("\n📈 Task Statistics:")
    println("━━━━━━━━━━━━━━━━━━")
    val stats = taskManager.getTaskStats()
    stats.display()

    // Demonstrate Kotlin features
    println("\n🚀 Kotlin Features Demo:")
    println("━━━━━━━━━━━━━━━━━━━━━━━")

    // Collection operations
    val highPriorityCount = taskManager.getAllTasks()
        .filter { it.priority == Priority.HIGH || it.priority == Priority.CRITICAL }
        .count()
    println("   High/Critical Priority Tasks: $highPriorityCount")

    // Lambda expressions
    val taskTitles = taskManager.getAllTasks()
        .map { it.title }
        .sorted()
    println("   All Tasks (alphabetical): ${taskTitles.joinToString(", ")}")

    // Footer
    println("\n╔════════════════════════════════════════════════╗")
    println("║         ✨ APPLICATION RUN COMPLETE ✨         ║")
    println("╚════════════════════════════════════════════════╝")
}