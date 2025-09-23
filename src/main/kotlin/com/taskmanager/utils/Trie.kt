package com.com.taskmanager.utils

class TrieNode {
    val children = mutableMapOf<Char, TrieNode>()
    var isEndOfWord = false
}

class Trie {
    private val root = TrieNode()

    fun insert(word: String) {
        var current = root
        word.lowercase().forEach { char ->
            current = current.children.getOrPut(char) { TrieNode() }
        }
        current.isEndOfWord = true
    }

    fun search(word: String): Boolean {
        var current = root
        word.lowercase().forEach { char ->
            current = current.children[char] ?: return false
        }
        return current.isEndOfWord
    }

    fun startsWith(prefix: String): Boolean {
        var current = root
        prefix.lowercase().forEach { char ->
            current = current.children[char] ?: return false
        }
        return true
    }

    fun findWordsWithPrefix(prefix: String): List<String> {
        var current = root
        val lowercasePrefix = prefix.lowercase()

        lowercasePrefix.forEach { char ->
            current = current.children[char] ?: return emptyList()
        }

        val words = mutableListOf<String>()
        dfs(current, lowercasePrefix, words)
        return words
    }

    private fun dfs(node: TrieNode, prefix: String, words: MutableList<String>) {
        if (node.isEndOfWord) {
            words.add(prefix)
        }

        node.children.forEach { (char, childNode) ->
            dfs(childNode, prefix + char, words)
        }
    }

    fun delete(word: String): Boolean {
        return deleteHelper(root, word.lowercase(), 0)
    }

    private fun deleteHelper(node: TrieNode, word: String, index: Int): Boolean {
        if (index == word.length) {
            if (!node.isEndOfWord) {
                return false
            }
            node.isEndOfWord = false
            return node.children.isEmpty()
        }

        val char = word[index]
        val childNode = node.children[char] ?: return false

        val shouldDeleteChild = deleteHelper(childNode, word, index + 1)

        if (shouldDeleteChild) {
            node.children.remove(char)
            return !node.isEndOfWord && node.children.isEmpty()
        }

        return false
    }
}