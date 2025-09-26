// FILE: src/main/kotlin/TaskApp.kt
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.swing.*
import javax.swing.border.EmptyBorder
import javax.swing.table.DefaultTableCellRenderer
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

    fun getTaskCount(): Int = tasks.size
}

// Custom button with modern styling
class ModernButton(text: String, private val bgColor: Color = Color(99, 102, 241)) : JButton(text) {
    init {
        isFocusPainted = false
        isBorderPainted = false  // Fixed: using property instead of borderPainted
        isContentAreaFilled = false
        foreground = Color.WHITE
        font = Font("Arial", Font.BOLD, 13)  // Changed from Segoe UI to Arial
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)

        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent) {
                background = bgColor.brighter()
            }
            override fun mouseExited(e: MouseEvent) {
                background = bgColor
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Draw rounded rectangle background
        g2.color = if (model.isPressed) bgColor.darker() else background ?: bgColor
        g2.fillRoundRect(0, 0, width - 1, height - 1, 15, 15)

        // Draw text
        val fm = g2.fontMetrics  // Fixed: using property syntax
        val stringWidth = fm.stringWidth(text)
        val stringHeight = fm.ascent
        g2.color = foreground
        g2.drawString(text, (width - stringWidth) / 2, (height + stringHeight) / 2 - 2)

        g2.dispose()
    }
}

// Modern styled panel with gradient
class GradientPanel(private val color1: Color, private val color2: Color) : JPanel() {
    init {
        isOpaque = false
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        val gp = GradientPaint(0f, 0f, color1, 0f, height.toFloat(), color2)
        g2.paint = gp
        g2.fillRect(0, 0, width, height)
        g2.dispose()
        super.paintComponent(g)
    }
}

// UI Application
class TaskApp : JFrame("Task Manager Pro") {
    private val manager = SimpleManager()
    private val tableModel = DefaultTableModel()
    private val table = JTable()
    private val formatter = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
    private var statsLabel: JLabel? = null

    // Modern color scheme
    private val primaryColor = Color(99, 102, 241)   // Indigo
    private val secondaryColor = Color(139, 92, 246)  // Purple
    private val successColor = Color(34, 197, 94)     // Green
    private val dangerColor = Color(239, 68, 68)      // Red
    private val bgColor = Color(248, 250, 252)        // Light gray
    private val cardColor = Color.WHITE

    init {
        defaultCloseOperation = EXIT_ON_CLOSE
        setSize(1100, 650)
        setLocationRelativeTo(null)

        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setupUI()
        addSampleData()
        refreshTable()
    }

    private fun setupUI() {
        contentPane.background = bgColor
        layout = BorderLayout()

        // Top Panel
        add(createHeaderPanel(), BorderLayout.NORTH)

        // Main content
        val mainPanel = JPanel(BorderLayout(20, 20)).apply {
            background = bgColor
            border = EmptyBorder(20, 20, 20, 20)

            add(createTablePanel(), BorderLayout.CENTER)
            add(createActionsPanel(), BorderLayout.EAST)
        }
        add(mainPanel, BorderLayout.CENTER)

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH)
    }

    private fun createHeaderPanel(): JPanel {
        return GradientPanel(primaryColor, secondaryColor).apply {
            preferredSize = Dimension(width, 80)
            layout = BorderLayout()
            border = EmptyBorder(20, 30, 20, 30)

            val textPanel = JPanel(GridLayout(2, 1)).apply {
                isOpaque = false

                add(JLabel("Task Manager Pro").apply {
                    foreground = Color.WHITE
                    font = Font("Arial", Font.BOLD, 28)
                })

                add(JLabel("Organize • Track • Complete").apply {
                    foreground = Color(255, 255, 255, 200)
                    font = Font("Arial", Font.PLAIN, 14)
                })
            }
            add(textPanel, BorderLayout.WEST)

            statsLabel = JLabel("Total Tasks: 0").apply {
                foreground = Color.WHITE
                font = Font("Arial", Font.PLAIN, 16)
            }
            add(statsLabel as Component, BorderLayout.EAST)  // Fixed: cast to Component
        }
    }

    private fun createTablePanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            background = cardColor
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color(229, 231, 235), 1),
                EmptyBorder(20, 20, 20, 20)
            )

            // Table setup
            tableModel.setColumnIdentifiers(arrayOf("Title", "Description", "Priority", "Status", "Created"))
            table.model = tableModel
            table.setShowGrid(false)
            table.setIntercellSpacing(Dimension(0, 5))
            table.rowHeight = 45
            table.selectionModel.selectionMode = 0
            table.font = Font("Arial", Font.PLAIN, 13)
            table.background = cardColor
            table.selectionBackground = Color(243, 244, 246)

            // Table header styling
            table.tableHeader.apply {
                font = Font("Arial", Font.BOLD, 13)
                background = Color(249, 250, 251)
                foreground = Color(107, 114, 128)
                preferredSize = Dimension(width, 40)
            }

            // Custom renderers
            setupTableRenderers()

            val scrollPane = JScrollPane(table).apply {
                border = BorderFactory.createEmptyBorder()
                viewport.background = cardColor
            }
            add(scrollPane, BorderLayout.CENTER)
        }
    }

    private fun setupTableRenderers() {
        // Priority column
        table.columnModel.getColumn(2).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean,
                hasFocus: Boolean, row: Int, column: Int
            ): Component {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

                text = when (value?.toString()) {
                    "HIGH" -> "● HIGH"
                    "MEDIUM" -> "● MEDIUM"
                    "LOW" -> "● LOW"
                    else -> value?.toString()
                }

                foreground = when (value?.toString()) {
                    "HIGH" -> Color(239, 68, 68)
                    "MEDIUM" -> Color(251, 146, 60)
                    "LOW" -> Color(34, 197, 94)
                    else -> Color.BLACK
                }

                font = Font("Arial", Font.BOLD, 12)
                return this
            }
        }

        // Status column
        table.columnModel.getColumn(3).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable?, value: Any?, isSelected: Boolean,
                hasFocus: Boolean, row: Int, column: Int
            ): Component {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)

                when (value?.toString()) {
                    "COMPLETED" -> {
                        text = "✓ Completed"
                        foreground = Color(34, 197, 94)
                    }
                    "IN_PROGRESS" -> {
                        text = "◉ In Progress"
                        foreground = Color(99, 102, 241)
                    }
                    "PENDING" -> {
                        text = "○ Pending"
                        foreground = Color(156, 163, 175)
                    }
                    else -> {
                        text = value?.toString()
                        foreground = Color.BLACK
                    }
                }
                return this
            }
        }
    }

    private fun createActionsPanel(): JPanel {
        return JPanel().apply {
            background = cardColor
            preferredSize = Dimension(200, 0)
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color(229, 231, 235), 1),
                EmptyBorder(20, 15, 20, 15)
            )

            add(JLabel("Quick Actions").apply {
                font = Font("Arial", Font.BOLD, 16)
                foreground = Color(31, 41, 55)
                alignmentX = CENTER_ALIGNMENT  // Fixed: removed Component. qualifier
            })

            add(Box.createRigidArea(Dimension(0, 20)))

            val buttons = listOf(
                "+ Add Task" to successColor,
                "✎ Update" to primaryColor,
                "× Delete" to dangerColor,
                "↻ Refresh" to Color(107, 114, 128)
            )

            buttons.forEach { (text, color) ->
                add(ModernButton(text, color).apply {
                    alignmentX = CENTER_ALIGNMENT  // Fixed: removed Component. qualifier
                    maximumSize = Dimension(170, 38)
                    preferredSize = Dimension(170, 38)

                    addActionListener {
                        when {
                            text.contains("Add") -> addTask()
                            text.contains("Update") -> updateTask()
                            text.contains("Delete") -> deleteTask()
                            text.contains("Refresh") -> refreshTable()
                        }
                    }
                })
                add(Box.createRigidArea(Dimension(0, 10)))
            }
        }
    }

    private fun createStatusBar(): JPanel {
        return JPanel(FlowLayout(FlowLayout.LEFT)).apply {
            background = Color(31, 41, 55)
            preferredSize = Dimension(width, 30)

            add(JLabel("Ready").apply {
                foreground = Color.WHITE
                font = Font("Arial", Font.PLAIN, 12)
            })
        }
    }

    private fun addTask() {
        val panel = JPanel(GridLayout(3, 2, 10, 10)).apply {
            border = EmptyBorder(10, 10, 10, 10)

            add(JLabel("Title:"))
            val titleField = JTextField()
            add(titleField)

            add(JLabel("Description:"))
            val descField = JTextField()
            add(descField)

            add(JLabel("Priority:"))
            val priorityCombo = JComboBox(arrayOf("LOW", "MEDIUM", "HIGH"))
            add(priorityCombo)
        }

        val result = JOptionPane.showConfirmDialog(
            this, panel, "Add New Task",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        )

        if (result == JOptionPane.OK_OPTION) {
            val title = (panel.getComponent(1) as JTextField).text
            val desc = (panel.getComponent(3) as JTextField).text
            val priority = (panel.getComponent(5) as JComboBox<*>).selectedItem as String

            if (title.isNotBlank() && desc.isNotBlank()) {
                manager.createTask(title, desc, priority)
                refreshTable()
                JOptionPane.showMessageDialog(this, "Task added successfully!")
            }
        }
    }

    private fun updateTask() {
        val row = table.selectedRow
        if (row >= 0) {
            val tasks = manager.getAllTasks()
            if (row < tasks.size) {
                val statuses = arrayOf("PENDING", "IN_PROGRESS", "COMPLETED")
                val newStatus = JOptionPane.showInputDialog(
                    this, "Select new status:", "Update Status",
                    JOptionPane.QUESTION_MESSAGE, null, statuses, tasks[row].status
                ) as? String

                if (newStatus != null) {
                    manager.updateStatus(tasks[row].id, newStatus)
                    refreshTable()
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task first")
        }
    }

    private fun deleteTask() {
        val row = table.selectedRow
        if (row >= 0) {
            val tasks = manager.getAllTasks()
            if (row < tasks.size) {
                val confirm = JOptionPane.showConfirmDialog(
                    this, "Delete this task?", "Confirm",
                    JOptionPane.YES_NO_OPTION
                )
                if (confirm == JOptionPane.YES_OPTION) {
                    manager.deleteTask(tasks[row].id)
                    refreshTable()
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task first")
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
        statsLabel?.text = "Total Tasks: ${manager.getTaskCount()}"
    }

    private fun addSampleData() {
        manager.createTask("Design new UI", "Create modern interface mockups", "HIGH")
        manager.createTask("Code review", "Review pull requests from team", "MEDIUM")
        manager.createTask("Update docs", "Update API documentation", "LOW")
        manager.createTask("Team meeting", "Weekly sync with development team", "HIGH")
    }
}

fun main() {
    SwingUtilities.invokeLater {
        TaskApp().isVisible = true
    }
}