package com.example.dsa.designpatterns

/**
 * Basic Implementation not thread safe
 */
class Singleton private constructor() {
    companion object {
        private var instance: Singleton? = null

        fun getInstance(): Singleton {
            if (instance == null) {
                instance = Singleton()
            }
            return instance!!
        }
    }

}

object SingletonObject {
    const val CONFIG: String = "production"

    fun doWork(): String {
        return "Working with $CONFIG"
    }
}

class HeavySingleton private constructor(val date: String) {
    companion object {
        @Volatile
        private var instance: HeavySingleton? = null

        fun getInstance(): HeavySingleton { // Double Check Lock but still not safe CPU reordering defeats this.
            return instance ?: synchronized(this) {
                instance ?: HeavySingleton("init").also { instance = it }
            }
        }
    }
}

class ExpensiveSingleton private constructor() {
    companion object {
        val instance by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            ExpensiveSingleton()
        }
    }
}

fun main() {
    println(SingletonObject.doWork())
    println(SingletonObject == SingletonObject)
}