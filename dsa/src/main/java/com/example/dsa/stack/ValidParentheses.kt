package com.example.dsa.stack


fun isValid(s: String): Boolean {
    val matchMap = mapOf(')' to '(', ']' to '[', '}' to '{')
    val stack = ArrayDeque<Char>()
    for(ch in s) {
        if(ch in matchMap.values) {
            stack.addLast(element = ch)
        } else {
            if (stack.isEmpty() || (stack.last() == matchMap[ch]).not()) return false
            stack.removeLast()
        }
    }
    return stack.isEmpty()
}