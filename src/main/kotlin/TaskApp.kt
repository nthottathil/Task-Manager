// FILE: src/main/kotlin/TaskApp.kt
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.GridLayout
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.swing.*
import javax.swing.table.DefaultTableModel

// Simple Task class
data class SimpleTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val priority: String,
    var status: String = "PENDING",
    val createdAt: LocalDateTime = LocalDateTime.now()
)

// Simple Manager class
class SimpleManager {
    private val tasks = mutableListOf<SimpleTask>()

    fun createTask(title: String, description: String, priority: String): SimpleTask {
        val task = SimpleTask(
            id = UUID.randomUUID().toString(),
            title = title,
            description = description,
            priority = priority,
            status = "PENDING",
            createdAt = LocalDateTime.now()
        )
        tasks.add(task)
        return task
    }

    fun getAllTasks(): List<SimpleTask> = tasks.toList()

    fun updateStatus(id: String, newStatus: String) {
        tasks.find { it.id == id }?.status = newStatus
    }

    fun deleteTask(id: String) {
        tasks.removeIf { it.id == id }
    }
}

// UI Application
class TaskApp : JFrame("Task Manager Application") {
    private val manager = SimpleManager()
    private val tableModel = DefaultTableModel()
    private val table = JTable()
    private val formatter = DateTimeFormatter.ofPattern("MMM dd HH:mm")

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(900, 500)
        setLocationRelativeTo(null)
        setupUI()
        addSampleData()
        refreshTable()
    }

    private fun setupUI() {
        layout = BorderLayout()

        // Header
        val header = JPanel().apply {
            background = Color(50, 100, 200)
            add(JLabel("Task Manager").apply {
                foreground = Color.WHITE
                font = Font("Arial", Font.BOLD, 20)
            })
        }
        add(header, BorderLayout.NORTH)

        // Table
        tableModel.setColumnIdentifiers(arrayOf("Title", "Description", "Priority", "Status", "Created"))
        table.model = tableModel
        table.selectionModel.selectionMode = 0  // 0 = SINGLE_SELECTION
        add(JScrollPane(table), BorderLayout.CENTER)

        // Buttons
        val buttonPanel = JPanel(GridLayout(5, 1, 5, 5)).apply {
            preferredSize = Dimension(120, 0)

            add(JButton("Add").apply {
                addActionListener { addTask() }
            })

            add(JButton("Update").apply {
                addActionListener { updateTask() }
            })

            add(JButton("Delete").apply {
                addActionListener { deleteTask() }
            })

            add(JButton("Refresh").apply {
                addActionListener { refreshTable() }
            })

            add(JButton("Exit").apply {
                addActionListener { dispose() }
            })
        }
        add(buttonPanel, BorderLayout.EAST)
    }

    private fun addTask() {
        val title = JOptionPane.showInputDialog(this, "Enter title:")
        val desc = JOptionPane.showInputDialog(this, "Enter description:")
        val priorities = arrayOf("LOW", "MEDIUM", "HIGH")
        val priority = JOptionPane.showInputDialog(
            this, "Select priority:", "Priority",
            JOptionPane.QUESTION_MESSAGE, null,
            priorities, priorities[0]
        ) as? String

        if (!title.isNullOrBlank() && !desc.isNullOrBlank() && priority != null) {
            manager.createTask(title, desc, priority)
            refreshTable()
            JOptionPane.showMessageDialog(this, "Task added!")
        }
    }

    private fun updateTask() {
        val row = table.selectedRow
        if (row >= 0) {
            val statuses = arrayOf("PENDING", "IN_PROGRESS", "COMPLETED")
            val newStatus = JOptionPane.showInputDialog(
                this, "Select status:", "Update",
                JOptionPane.QUESTION_MESSAGE, null,
                statuses, statuses[0]
            ) as? String

            if (newStatus != null) {
                val tasks = manager.getAllTasks()
                if (row < tasks.size) {
                    manager.updateStatus(tasks[row].id, newStatus)
                    refreshTable()
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a task first")
        }
    }

    private fun deleteTask() {
        val row = table.selectedRow
        if (row >= 0) {
            val tasks = manager.getAllTasks()
            if (row < tasks.size) {
                manager.deleteTask(tasks[row].id)
                refreshTable()
                JOptionPane.showMessageDialog(this, "Task deleted")
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a task first")
        }
    }

    private fun refreshTable() {
        tableModel.rowCount = 0
        manager.getAllTasks().forEach { task ->
            tableModel.addRow(arrayOf(
                task.title,
                task.description,
                task.priority,
                task.status,
                task.createdAt.format(formatter)
            ))
        }
    }

    private fun addSampleData() {
        manager.createTask("Setup Project", "Initialize the repository", "HIGH")
        manager.createTask("Write Code", "Implement features", "MEDIUM")
        manager.createTask("Test", "Run unit tests", "LOW")
    }
}

fun main() {
    SwingUtilities.invokeLater {
        TaskApp().isVisible = true
    }
}