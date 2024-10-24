package com.mato.base.hook

/**
 * @Author sunlulu.tomato
 * @Date 7/12/24
 */
sealed interface Checker {
    fun check(): Boolean
}

class FirstTimeChecker : Checker {
    private var firstTime = true

    override fun check(): Boolean {
        if (firstTime) {
            firstTime = false
            return true
        }
        return false
    }
}

class IntervalChecker(private val seconds: Int): Checker {
    private var lastTime = 0L

    override fun check(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastTime > seconds * 1000) {
            lastTime = now
            return true
        }
        return false
    }
}