package com.example.myapplication.util

class HeavyWorkUtil {

    fun doHeavyWork() {
        primesUpTo(1_00_000)
    }

    // Intentionally not optimal
    private fun primesUpTo(n: Int): List<Int> {
        val primes = mutableListOf<Int>()
        for (i in 2..n) {
            var isPrime = true
            for (j in 2 until i) {
                if (i % j == 0) {
                    isPrime = false
                    break
                }
            }
            if (isPrime) {
                primes.add(i)
            }
        }
        return primes
    }
}
