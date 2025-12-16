package ch.ergon.studentmarks.infrastructure.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun getLogger(forClass: Class<*>): Logger = LoggerFactory.getLogger(forClass)

interface Logger {
    val LOG
        get() = getLogger(this.javaClass.enclosingClass)
}
