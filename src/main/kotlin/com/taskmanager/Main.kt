package com.taskmanager

// THESE IMPORTS ARE REQUIRED - THEY MUST BE HERE
import com.taskmanager.models.Priority
import com.taskmanager.models.TaskStatus
import com.taskmanager.services.TaskManagerService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    println("╔════════════════════════════════════════╗")
    println("║     KOTLIN TASK MANAGER v1.0.0        ║")
    println("╚════════════════════════════════════════╝\n")

    val taskManager = TaskManagerService()
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    // Create sample tasks
    println("📝 Creating sample tasks...\n")

    val task1 = taskManager.createTask(
        title = "Implement binary search tree",
        description = "Create a balanced BST with insert, delete, and search operations",
        priority = Priority.HIGH,
        dueDate = LocalDateTime.now().plusDays(2),
        tags = setOf("algorithm", "data-structure", "interview-prep")
    )
    println("✅ Created: ${task1.title}")
    println("   Priority: ${task1.priority}")
    println("   Due: ${task1.dueDate?.format(formatter) ?: "No due date"}")

    val task2 = taskManager.createTask(
        title = "Review system design patterns",
        description = "Study microservices, load balancing, and caching strategies",
        priority = Priority.CRITICAL,
        dueDate = LocalDateTime.now().plusDays(1),
        tags = setOf("system-design", "interview-prep", "architecture")
    )
    println("✅ Created: ${task2.title}")
    println("   Priority: ${task2.priority}")
    println("   Due: ${task2.dueDate?.format(formatter) ?: "No due date"}")

    // Update task status
    println("\n📊 Updating Task Status:")
    println("────────────────────────")
    taskManager.updateTaskStatus(task2.id, TaskStatus.IN_PROGRESS)
    println("   ✓ '${task2.title}' → IN_PROGRESS")

    // Get task statistics
    println("\n📈 Task Statistics:")
    println("──────────────────")
    val stats = taskManager.getTaskStats()
    println("   Total Tasks: ${stats.total}")
    println("   Pending: ${stats.pending}")
    println("   In Progress: ${stats.inProgress}")
    println("   Completed: ${stats.completed}")
    println("   Completion Rate: ${"%.1f".format(stats.completionRate)}%")

    println("\n╔════════════════════════════════════════╗")
    println("║        APPLICATION RUN COMPLETE        ║")
    println("╚════════════════════════════════════════╝")
}