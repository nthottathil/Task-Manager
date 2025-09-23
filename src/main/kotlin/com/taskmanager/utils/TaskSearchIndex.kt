package com.com.taskmanager.utils

import com.com.taskmanager.models.Task

class TaskSearchIndex {
    private val titleIndex = Trie()
    private val taskMap = mutableMapOf<String, MutableSet<Task>>()

    fun indexTask(task: Task) {
        // Index by title words
        val titleWords = task.title.lowercase().split(Regex("\\s+"))
        titleWords.forEach { word ->
            if (word.isNotBlank()) {
                titleIndex.insert(word)
                taskMap.getOrPut(word) { mutableSetOf() }.add(task)
            }
        }

        // Index by description words (first 10 words for performance)
        val descriptionWords = task.description.lowercase()
            .split(Regex("\\s+"))
            .take(10)
        descriptionWords.forEach { word ->
            if (word.isNotBlank()) {
                titleIndex.insert(word)
                taskMap.getOrPut(word) { mutableSetOf() }.add(task)
            }
        }

        // Index by tags
        task.tags.forEach { tag ->
            val lowercaseTag = tag.lowercase()
            titleIndex.insert(lowercaseTag)
            taskMap.getOrPut(lowercaseTag) { mutableSetOf() }.add(task)
        }
    }

    fun removeTask(task: Task) {
        // Remove from title index
        val titleWords = task.title.lowercase().split(Regex("\\s+"))
        titleWords.forEach { word ->
            if (word.isNotBlank()) {
                taskMap[word]?.remove(task)
                if (taskMap[word]?.isEmpty() == true) {
                    taskMap.remove(word)
                    // Note: We don't remove from Trie as other tasks might use this word
                }
            }
        }

        // Remove from description index
        val descriptionWords = task.description.lowercase()
            .split(Regex("\\s+"))
            .take(10)
        descriptionWords.forEach { word ->
            if (word.isNotBlank()) {
                taskMap[word]?.remove(task)
                if (taskMap[word]?.isEmpty() == true) {
                    taskMap.remove(word)
                }
            }
        }

        // Remove from tags
        task.tags.forEach { tag ->
            val lowercaseTag = tag.lowercase()
            taskMap[lowercaseTag]?.remove(task)
            if (taskMap[lowercaseTag]?.isEmpty() == true) {
                taskMap.remove(lowercaseTag)
            }
        }
    }

    fun search(query: String): List<Task> {
        if (query.isBlank()) return emptyList()

        val results = mutableSetOf<Task>()
        val words = query.lowercase().split(Regex("\\s+"))

        words.forEach { word ->
            if (word.isNotBlank()) {
                // Find exact matches
                taskMap[word]?.let { results.addAll(it) }

                // Find prefix matches
                val matchingWords = titleIndex.findWordsWithPrefix(word)
                matchingWords.forEach { matchingWord ->
                    taskMap[matchingWord]?.let { results.addAll(it) }
                }
            }
        }

        // Sort by relevance (tasks matching more query words come first)
        return results.sortedByDescending { task ->
            words.count { word ->
                task.title.lowercase().contains(word) ||
                        task.description.lowercase().contains(word) ||
                        task.tags.any { it.lowercase().contains(word) }
            }
        }
    }

    fun getAllIndexedWords(): List<String> {
        return taskMap.keys.sorted()
    }

    fun getTaskCount(): Int {
        return taskMap.values.flatMap { it }.distinct().count()
    }
}