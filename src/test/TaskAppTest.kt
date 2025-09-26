// FILE: src/test/kotlin/TaskAppTest.kt
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import java.time.LocalDateTime
import javax.swing.SwingUtilities
import kotlin.concurrent.thread

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TaskAppTest {

    private lateinit var manager: SimpleManager

    @BeforeEach
    fun setUp() {
        manager = SimpleManager()
    }

    @Test
    @DisplayName("Should create a task with all required fields")
    fun testCreateTask() {
        // Given
        val title = "Test Task"
        val description = "This is a test task"
        val priority = "HIGH"

        // When
        val task = manager.createTask(title, description, priority)

        // Then
        assertNotNull(task)
        assertEquals(title, task.title)
        assertEquals(description, task.description)
        assertEquals(priority, task.priority)
        assertEquals("PENDING", task.status)
        assertNotNull(task.id)
        assertNotNull(task.createdAt)
    }

    @Test
    @DisplayName("Should generate unique IDs for each task")
    fun testUniqueTaskIds() {
        // Given & When
        val task1 = manager.createTask("Task 1", "Description 1", "LOW")
        val task2 = manager.createTask("Task 2", "Description 2", "MEDIUM")
        val task3 = manager.createTask("Task 3", "Description 3", "HIGH")

        // Then
        assertNotEquals(task1.id, task2.id)
        assertNotEquals(task2.id, task3.id)
        assertNotEquals(task1.id, task3.id)
    }

    @Test
    @DisplayName("Should return all created tasks")
    fun testGetAllTasks() {
        // Given
        manager.createTask("Task 1", "Description 1", "LOW")
        manager.createTask("Task 2", "Description 2", "MEDIUM")
        manager.createTask("Task 3", "Description 3", "HIGH")

        // When
        val tasks = manager.getAllTasks()

        // Then
        assertEquals(3, tasks.size)
        assertEquals("Task 1", tasks[0].title)
        assertEquals("Task 2", tasks[1].title)
        assertEquals("Task 3", tasks[2].title)
    }

    @Test
    @DisplayName("Should update task status successfully")
    fun testUpdateTaskStatus() {
        // Given
        val task = manager.createTask("Test Task", "Description", "MEDIUM")
        val originalStatus = task.status

        // When
        manager.updateStatus(task.id, "IN_PROGRESS")
        val updatedTask = manager.getAllTasks().find { it.id == task.id }

        // Then
        assertEquals("PENDING", originalStatus)
        assertEquals("IN_PROGRESS", updatedTask?.status)
    }

    @Test
    @DisplayName("Should not update status for non-existent task")
    fun testUpdateNonExistentTaskStatus() {
        // Given
        val nonExistentId = "non-existent-id"
        val initialTaskCount = manager.getAllTasks().size

        // When
        manager.updateStatus(nonExistentId, "COMPLETED")

        // Then
        assertEquals(initialTaskCount, manager.getAllTasks().size)
    }

    @Test
    @DisplayName("Should delete task successfully")
    fun testDeleteTask() {
        // Given
        val task = manager.createTask("To Delete", "Will be deleted", "LOW")
        val initialCount = manager.getAllTasks().size

        // When
        manager.deleteTask(task.id)
        val finalCount = manager.getAllTasks().size
        val deletedTask = manager.getAllTasks().find { it.id == task.id }

        // Then
        assertEquals(initialCount - 1, finalCount)
        assertNull(deletedTask)
    }

    @Test
    @DisplayName("Should handle deletion of non-existent task")
    fun testDeleteNonExistentTask() {
        // Given
        manager.createTask("Existing Task", "Description", "HIGH")
        val initialCount = manager.getAllTasks().size

        // When
        manager.deleteTask("non-existent-id")

        // Then
        assertEquals(initialCount, manager.getAllTasks().size)
    }

    @Test
    @DisplayName("Should return correct task count")
    fun testGetTaskCount() {
        // Given & When
        assertEquals(0, manager.getTaskCount())

        manager.createTask("Task 1", "Description 1", "LOW")
        assertEquals(1, manager.getTaskCount())

        manager.createTask("Task 2", "Description 2", "MEDIUM")
        assertEquals(2, manager.getTaskCount())

        val task = manager.createTask("Task 3", "Description 3", "HIGH")
        assertEquals(3, manager.getTaskCount())

        manager.deleteTask(task.id)
        assertEquals(2, manager.getTaskCount())
    }

    @Test
    @DisplayName("Should maintain task order in getAllTasks")
    fun testTaskOrder() {
        // Given
        val task1 = manager.createTask("First", "Description", "LOW")
        val task2 = manager.createTask("Second", "Description", "MEDIUM")
        val task3 = manager.createTask("Third", "Description", "HIGH")

        // When
        val tasks = manager.getAllTasks()

        // Then
        assertEquals(task1.id, tasks[0].id)
        assertEquals(task2.id, tasks[1].id)
        assertEquals(task3.id, tasks[2].id)
    }

    @Test
    @DisplayName("Should handle multiple status updates on same task")
    fun testMultipleStatusUpdates() {
        // Given
        val task = manager.createTask("Test", "Description", "MEDIUM")

        // When & Then
        manager.updateStatus(task.id, "IN_PROGRESS")
        assertEquals("IN_PROGRESS", manager.getAllTasks().find { it.id == task.id }?.status)

        manager.updateStatus(task.id, "COMPLETED")
        assertEquals("COMPLETED", manager.getAllTasks().find { it.id == task.id }?.status)

        manager.updateStatus(task.id, "PENDING")
        assertEquals("PENDING", manager.getAllTasks().find { it.id == task.id }?.status)
    }

    @Test
    @DisplayName("Should handle empty task list operations")
    fun testEmptyListOperations() {
        // Given empty manager

        // When & Then
        assertEquals(0, manager.getTaskCount())
        assertTrue(manager.getAllTasks().isEmpty())

        // Operations on empty list should not throw exceptions
        assertDoesNotThrow {
            manager.updateStatus("any-id", "COMPLETED")
            manager.deleteTask("any-id")
        }
    }

    @Test
    @DisplayName("Should set correct timestamp on task creation")
    fun testTaskTimestamp() {
        // Given
        val beforeCreation = LocalDateTime.now()

        // When
        val task = manager.createTask("Timed Task", "Description", "LOW")
        val afterCreation = LocalDateTime.now()

        // Then
        assertTrue(task.createdAt.isAfter(beforeCreation.minusSeconds(1)))
        assertTrue(task.createdAt.isBefore(afterCreation.plusSeconds(1)))
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SimpleTaskTest {

    @Test
    @DisplayName("Should create SimpleTask with default values")
    fun testSimpleTaskDefaults() {
        // Given & When
        val task = SimpleTask(
            title = "Test",
            description = "Description",
            priority = "HIGH"
        )

        // Then
        assertNotNull(task.id)
        assertEquals("Test", task.title)
        assertEquals("Description", task.description)
        assertEquals("HIGH", task.priority)
        assertEquals("PENDING", task.status)
        assertNotNull(task.createdAt)
    }

    @Test
    @DisplayName("Should create SimpleTask with custom values")
    fun testSimpleTaskCustom() {
        // Given
        val customId = "custom-id-123"
        val customTime = LocalDateTime.now().minusDays(1)

        // When
        val task = SimpleTask(
            id = customId,
            title = "Custom Task",
            description = "Custom Description",
            priority = "LOW",
            status = "COMPLETED",
            createdAt = customTime
        )

        // Then
        assertEquals(customId, task.id)
        assertEquals("Custom Task", task.title)
        assertEquals("Custom Description", task.description)
        assertEquals("LOW", task.priority)
        assertEquals("COMPLETED", task.status)
        assertEquals(customTime, task.createdAt)
    }

    @Test
    @DisplayName("Should modify status as it's mutable")
    fun testStatusMutability() {
        // Given
        val task = SimpleTask(
            title = "Mutable Task",
            description = "Test mutability",
            priority = "MEDIUM"
        )

        // When
        task.status = "IN_PROGRESS"

        // Then
        assertEquals("IN_PROGRESS", task.status)

        // When
        task.status = "COMPLETED"

        // Then
        assertEquals("COMPLETED", task.status)
    }
}

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UIComponentTest {

    private var app: TaskApp? = null

    @BeforeAll
    fun setUpUI() {
        // Run UI setup on EDT
        SwingUtilities.invokeAndWait {
            app = TaskApp()
        }
    }

    @AfterAll
    fun tearDownUI() {
        SwingUtilities.invokeAndWait {
            app?.dispose()
        }
    }

    @Test
    @DisplayName("Should initialize TaskApp window")
    fun testTaskAppInitialization() {
        SwingUtilities.invokeAndWait {
            assertNotNull(app)
            assertEquals("Task Manager Pro", app?.title)
            assertEquals(1100, app?.width)
            assertEquals(650, app?.height)
        }
    }

    @Test
    @DisplayName("Should create ModernButton with correct properties")
    fun testModernButton() {
        SwingUtilities.invokeAndWait {
            // Given
            val color = java.awt.Color(100, 100, 200)

            // When
            val button = ModernButton("Test Button", color)

            // Then
            assertEquals("Test Button", button.text)
            assertEquals(java.awt.Color.WHITE, button.foreground)
            assertFalse(button.isFocusPainted)
            assertFalse(button.isBorderPainted)
            assertFalse(button.isContentAreaFilled)
        }
    }

    @Test
    @DisplayName("Should create GradientPanel")
    fun testGradientPanel() {
        SwingUtilities.invokeAndWait {
            // Given
            val color1 = java.awt.Color.BLUE
            val color2 = java.awt.Color.GREEN

            // When
            val panel = GradientPanel(color1, color2)

            // Then
            assertNotNull(panel)
            assertFalse(panel.isOpaque)
        }
    }
}