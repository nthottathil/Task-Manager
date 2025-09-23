package com.taskmanager

// THESE IMPORTS ARE REQUIRED - ADD THEM AT THE TOP
import com.taskmanager.models.Priority
import com.taskmanager.models.Task
import com.taskmanager.models.TaskStatus
import com.taskmanager.models.TaskStatistics
import com.taskmanager.services.TaskManagerService
import com.taskmanager.utils.Trie
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import java.time.LocalDateTime

class TaskManagerServiceTest {

    private lateinit var taskManager: TaskManagerService

    @BeforeEach
    fun setup() {
        taskManager = TaskManagerService()
    }

    @Test
    @DisplayName("Should create a task successfully")
    fun testCreateTask() {
        val task = taskManager.createTask(
            title = "Test Task",
            description = "Test Description",
            priority = Priority.HIGH,
            tags = setOf("test", "unit-test")
        )

        assertNotNull(task)
        assertEquals("Test Task", task.title)
        assertEquals("Test Description", task.description)
        assertEquals(Priority.HIGH, task.priority)
        assertEquals(TaskStatus.PENDING, task.status)
        assertTrue(task.tags.contains("test"))
    }

    @Test
    @DisplayName("Should throw exception for blank title")
    fun testCreateTaskWithBlankTitle() {
        assertThrows(IllegalArgumentException::class.java) {
            taskManager.createTask(
                title = "",
                description = "Description",
                priority = Priority.LOW
            )
        }
    }

    @Test
    @DisplayName("Should update task status")
    fun testUpdateTaskStatus() {
        val task = taskManager.createTask(
            title = "Status Test",
            description = "Testing status update",
            priority = Priority.MEDIUM
        )

        val updated = taskManager.updateTaskStatus(task.id, TaskStatus.IN_PROGRESS)
        assertTrue(updated)

        val updatedTask = taskManager.getTask(task.id)
        assertNotNull(updatedTask)
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask?.status)
    }

    @Test
    @DisplayName("Should get highest priority task")
    fun testGetHighestPriorityTask() {
        taskManager.createTask("Low", "Low priority", Priority.LOW)
        taskManager.createTask("Medium", "Medium priority", Priority.MEDIUM)
        val criticalTask = taskManager.createTask("Critical", "Critical priority", Priority.CRITICAL)
        taskManager.createTask("High", "High priority", Priority.HIGH)

        val highest = taskManager.getHighestPriorityTask()
        assertNotNull(highest)
        assertEquals(criticalTask.id, highest?.id)
        assertEquals(Priority.CRITICAL, highest?.priority)
    }

    @Test
    @DisplayName("Should search tasks by keyword")
    fun testSearchTasks() {
        taskManager.createTask(
            title = "Implement algorithm",
            description = "Binary search tree",
            priority = Priority.HIGH,
            tags = setOf("algorithm", "tree")
        )

        taskManager.createTask(
            title = "Study data structures",
            description = "Learn about trees and graphs",
            priority = Priority.MEDIUM,
            tags = setOf("data-structure")
        )

        taskManager.createTask(
            title = "Review code",
            description = "Code review for PR",
            priority = Priority.LOW,
            tags = setOf("review")
        )

        val searchResults = taskManager.searchTasks("algorithm")
        assertEquals(1, searchResults.size)
        assertTrue(searchResults[0].title.contains("algorithm", ignoreCase = true))
    }

    @Test
    @DisplayName("Should filter tasks by status")
    fun testGetTasksByStatus() {
        val task1 = taskManager.createTask("Task 1", "Description 1", Priority.HIGH)
        val task2 = taskManager.createTask("Task 2", "Description 2", Priority.MEDIUM)
        taskManager.createTask("Task 3", "Description 3", Priority.LOW)

        taskManager.updateTaskStatus(task1.id, TaskStatus.IN_PROGRESS)
        taskManager.updateTaskStatus(task2.id, TaskStatus.COMPLETED)

        val pendingTasks = taskManager.getTasksByStatus(TaskStatus.PENDING)
        val inProgressTasks = taskManager.getTasksByStatus(TaskStatus.IN_PROGRESS)
        val completedTasks = taskManager.getTasksByStatus(TaskStatus.COMPLETED)

        assertEquals(1, pendingTasks.size)
        assertEquals(1, inProgressTasks.size)
        assertEquals(1, completedTasks.size)
    }

    @Test
    @DisplayName("Should identify overdue tasks")
    fun testGetOverdueTasks() {
        taskManager.createTask(
            title = "Past Task",
            description = "Already overdue",
            priority = Priority.HIGH,
            dueDate = LocalDateTime.now().minusDays(2)
        )

        taskManager.createTask(
            title = "Future Task",
            description = "Not due yet",
            priority = Priority.MEDIUM,
            dueDate = LocalDateTime.now().plusDays(2)
        )

        val overdueTasks = taskManager.getOverdueTasks()
        assertEquals(1, overdueTasks.size)
        assertEquals("Past Task", overdueTasks[0].title)
    }

    @Test
    @DisplayName("Should calculate task statistics correctly")
    fun testTaskStatistics() {
        val task1 = taskManager.createTask("Task 1", "Desc 1", Priority.HIGH)
        val task2 = taskManager.createTask("Task 2", "Desc 2", Priority.MEDIUM)
        val task3 = taskManager.createTask("Task 3", "Desc 3", Priority.LOW)
        taskManager.createTask("Task 4", "Desc 4", Priority.HIGH)

        taskManager.updateTaskStatus(task1.id, TaskStatus.IN_PROGRESS)
        taskManager.updateTaskStatus(task2.id, TaskStatus.COMPLETED)
        taskManager.updateTaskStatus(task3.id, TaskStatus.CANCELLED)

        val stats = taskManager.getTaskStats()

        assertEquals(4, stats.total)
        assertEquals(1, stats.pending)
        assertEquals(1, stats.inProgress)
        assertEquals(1, stats.completed)
        assertEquals(1, stats.cancelled)
        assertEquals(25.0, stats.completionRate, 0.01)
    }
}

class TrieTest {

    private lateinit var trie: Trie

    @BeforeEach
    fun setup() {
        trie = Trie()
    }

    @Test
    @DisplayName("Should insert and search words")
    fun testInsertAndSearch() {
        trie.insert("hello")
        trie.insert("world")
        trie.insert("help")

        assertTrue(trie.search("hello"))
        assertTrue(trie.search("world"))
        assertTrue(trie.search("help"))
        assertFalse(trie.search("hell"))
        assertFalse(trie.search("helping"))
    }

    @Test
    @DisplayName("Should find words with prefix")
    fun testFindWordsWithPrefix() {
        trie.insert("hello")
        trie.insert("help")
        trie.insert("helpful")
        trie.insert("world")

        val wordsWithHel = trie.findWordsWithPrefix("hel")
        assertEquals(3, wordsWithHel.size)
        assertTrue(wordsWithHel.contains("hello"))
        assertTrue(wordsWithHel.contains("help"))
        assertTrue(wordsWithHel.contains("helpful"))

        val wordsWithWorld = trie.findWordsWithPrefix("world")
        assertEquals(1, wordsWithWorld.size)
        assertTrue(wordsWithWorld.contains("world"))
    }

    @Test
    @DisplayName("Should check if word starts with prefix")
    fun testStartsWith() {
        trie.insert("hello")
        trie.insert("world")

        assertTrue(trie.startsWith("hel"))
        assertTrue(trie.startsWith("hello"))
        assertTrue(trie.startsWith("wor"))
        assertFalse(trie.startsWith("abc"))
    }
}
