package com.com.taskmanager.models

import java.time.LocalDateTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: Priority,
    val status: TaskStatus = TaskStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val dueDate: LocalDateTime? = null,
    val tags: Set<String> = emptySet()
) : Comparable<Task> {
    override fun compareTo(other: Task): Int {
        // First compare by priority, then by due date, then by creation time
        val priorityComparison = other.priority.value.compareTo(this.priority.value)
        if (priorityComparison != 0) return priorityComparison

        return when {
            dueDate == null && other.dueDate == null -> createdAt.compareTo(other.createdAt)
            dueDate == null -> 1
            other.dueDate == null -> -1
            else -> dueDate.compareTo(other.dueDate)
        }
    }
}

enum class Priority(val value: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3),
    CRITICAL(4)
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
}

