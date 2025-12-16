package ch.ergon.tablero.infrastructure

import kotlin.random.Random

class IdGenerator(
    private val length: Int = 10,
    private val charPool: String = ALL,
) {
    companion object {
        const val DIGITS = "0123456789"
        const val SMALL_LETTERS = "abcdefghijklmnopqrstuvwxyz"
        const val BIG_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        const val ALL = DIGITS + SMALL_LETTERS + BIG_LETTERS
    }

    fun generate(): String =
        (1..length)
            .map { Random.nextInt(0, charPool.length) }
            .map(charPool::get)
            .joinToString("")
}
