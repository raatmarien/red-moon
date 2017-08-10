/*
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 *
 * This file incorporates work covered by the following copyright and
 * permission notices:
 * 
 *    Anko <https://github.com/Kotlin/anko>
 *    Copyright 2016 JetBrains s.r.o.
 *
 *    kotlin-logging <https://github.com/MicroUtils/kotlin-logging/>
 *    Copyright 2017 oshai
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")
package com.jmstudios.redmoon.helper

import android.util.Log
import java.lang.reflect.Modifier


/**
 * Normally you should pass the logger tag to the [Log] methods, such as [Log.d] or [Log.e].
 * This can be inconvenient because you should store the tag somewhere or hardcode it,
 *   which is considered to be a bad practice.
 *
 * Instead of hardcoding tags, Logger provides a member, Log, which implements
 * the [KLog] interface. You can just add the interface to the companion object
 * of any of your classes, and then call its member functions.
 *
 *
 * @param enabled (optional) whether to print logs. If this is omitted, the
 *                             const val DEBUG will be used instead.
 *
 * @param TAG (optional) the tag to print with the logs. If this is omitted,
 *                         the javaclass.simpleName will be used by default.
 *
 * usage example:
 *
 * class ClassWithLogging {
 *   companion object: Logger() // Default
 *   fun test() {
 *     Log.i{"test ClassWithLogging"}
 *   }
 * }
 *
 * Or, the default constructor could be replaced with:
 *
 *   companion object : Logger(false) {
 *   companion object : Logger(true, "MY_TAG") {
 *   companion object : Logger(TAG = "YOUR_TAG") {
 *
 */
const val DEBUG = true
abstract class Logger(inline private val enabled: Boolean = DEBUG, TAG: String? = null) {

    /**
     * The member that performs the actual logging
     */
    val Log: KLog =
            if (TAG == null) {
                log()
            } else {
                log(TAG)
            }

    /**
     * get logger for the class
     */
    private fun log(): KLog = makeLog(this, enabled)

    /**
     * get logger by explicit name
     */
    private fun log(tag: String): KLog = makeLog(tag, enabled)


}

object KLogging {
    /**
     * This method allow defining the logger in a file in the following way:
     *
     * val logger = Logging.logger {}
     * val logger = Logging.logger("MY TAG", true)
     *
     * This method is not recommended unless you are unable to use Logger().
     * 
     */
    fun logger(func: () -> Unit, enabled: Boolean = DEBUG): KLog = makeLog(func, enabled)

    fun logger(TAG: String, enabled: Boolean = DEBUG): KLog = makeLog(TAG, enabled)
}

abstract class KLog {
    /**
     * The logger tag used in extension functions for the [KLog].
     * Note that the tag length should not be more than 23 symbols.
     */
    abstract val logTag: String
    abstract val logEnabled: Boolean

    /**
     * Send a log message with the [Log.VERBOSE] severity.
     * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
     * The default log level is [Log.INFO].
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.v].
     */
    fun v(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            log(this, message, thr, Log.VERBOSE,
                    { tag, msg -> Log.v(tag, msg) },
                    { tag, msg, thr -> Log.v(tag, msg, thr) })
        }
    }

    /**
     * Send a log message with the [Log.DEBUG] severity.
     * Note that the log message will not be written if the current log level is above [Log.DEBUG].
     * The default log level is [Log.INFO].
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.d].
     */
    fun d(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            log(this, message, thr, Log.DEBUG,
                    { tag, msg -> Log.d(tag, msg) },
                    { tag, msg, thr -> Log.d(tag, msg, thr) })
        }
    }

    /**
     * Send a log message with the [Log.INFO] severity.
     * Note that the log message will not be written if the current log level is above [Log.INFO]
     *   (it is the default level).
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.i].
     */
    fun i(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            log(this, message, thr, Log.INFO,
                    { tag, msg -> Log.i(tag, msg) },
                    { tag, msg, thr -> Log.i(tag, msg, thr) })
        }
    }

    /**
     * Send a log message with the [Log.WARN] severity.
     * Note that the log message will not be written if the current log level is above [Log.WARN].
     * The default log level is [Log.INFO].
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.w].
     */
    fun w(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            log(this, message, thr, Log.WARN,
                    { tag, msg -> Log.w(tag, msg) },
                    { tag, msg, thr -> Log.w(tag, msg, thr) })
        }
    }

    /**
     * Send a log message with the [Log.ERROR] severity.
     * Note that the log message will not be written if the current log level is above [Log.ERROR].
     * The default log level is [Log.INFO].
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.e].
     */
    fun e(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            log(this, message, thr, Log.ERROR,
                    { tag, msg -> Log.e(tag, msg) },
                    { tag, msg, thr -> Log.e(tag, msg, thr) })
        }
    }

    /**
     * Send a log message with the "What a Terrible Failure" severity.
     * Report an exception that should never happen.
     *
     * @param message the message text to log. `null` value will be represent as "null", for any other value
     *   the [Any.toString] will be invoked.
     * @param thr an exception to log (optional).
     *
     * @see [Log.wtf].
     */
    inline fun wtf(message: Any?, thr: Throwable? = null) {
        if (logEnabled) {
            if (thr != null) {
                Log.wtf(logTag, message?.toString() ?: "null", thr)
            } else {
                Log.wtf(logTag, message?.toString() ?: "null")
            }
        }
    }

    /**
     * Send a log message with the [Log.VERBOSE] severity.
     * Note that the log message will not be written if the current log level is above [Log.VERBOSE].
     * The default log level is [Log.INFO].
     *
     * @param message the function that returns message text to log.
     *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
     *
     * @see [Log.v].
     */
    inline fun v(message: () -> Any?) {
        if (logEnabled) {
            val tag = logTag
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message()?.toString() ?: "null")
            }
        }
    }

    /**
     * Send a log message with the [Log.DEBUG] severity.
     * Note that the log message will not be written if the current log level is above [Log.DEBUG].
     * The default log level is [Log.INFO].
     *
     * @param message the function that returns message text to log.
     *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
     *
     * @see [Log.d].
     */
    inline fun d(message: () -> Any?) {
        if (logEnabled) {
            val tag = logTag
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message()?.toString() ?: "null")
            }
        }
    }

    /**
     * Send a log message with the [Log.INFO] severity.
     * Note that the log message will not be written if the current log level is above [Log.INFO].
     * The default log level is [Log.INFO].
     *
     * @param message the function that returns message text to log.
     *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
     *
     * @see [Log.i].
     */
    inline fun i(message: () -> Any?) {
        if (logEnabled) {
            val tag = logTag
            if (Log.isLoggable(tag, Log.INFO)) {
                Log.i(tag, message()?.toString() ?: "null")
            }
        }
    }

    /**
     * Send a log message with the [Log.WARN] severity.
     * Note that the log message will not be written if the current log level is above [Log.WARN].
     * The default log level is [Log.INFO].
     *
     * @param message the function that returns message text to log.
     *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
     *
     * @see [Log.w].
     */
    inline fun w(message: () -> Any?) {
        if (logEnabled) {
            val tag = logTag
            if (Log.isLoggable(tag, Log.WARN)) {
                Log.w(tag, message()?.toString() ?: "null")
            }
        }
    }

    /**
     * Send a log message with the [Log.ERROR] severity.
     * Note that the log message will not be written if the current log level is above [Log.ERROR].
     * The default log level is [Log.INFO].
     *
     * @param message the function that returns message text to log.
     *   `null` value will be represent as "null", for any other value the [Any.toString] will be invoked.
     *
     * @see [Log.e].
     */
    inline fun e(message: () -> Any?) {
        if (logEnabled) {
            val tag = logTag
            if (Log.isLoggable(tag, Log.ERROR)) {
                Log.e(tag, message()?.toString() ?: "null")
            }
        }
    }
}



/**
 * Return the stack trace [String] of a throwable.
 */
inline fun Throwable.getStackTraceString(): String = Log.getStackTraceString(this)

private inline fun log(
        logger: KLog,
        message: Any?,
        thr: Throwable?,
        level: Int,
        f: (String, String) -> Unit,
        fThrowable: (String, String, Throwable) -> Unit) {
    val tag = logger.logTag
    if (Log.isLoggable(tag, level)) {
        if (thr != null) {
            fThrowable(tag, message?.toString() ?: "null", thr)
        } else {
            f(tag, message?.toString() ?: "null")
        }
    }
}

/**
 * get logger by explicit name
 */
inline private fun makeLog(tag: String, enabled: Boolean): KLog = object : KLog() {
    override val logEnabled = enabled
    override val logTag =
            if (tag.length <= 23) {
                tag
            } else {
                tag.substring(0, 23)
            }
}

/**
 * get logger for the class
 */
inline private fun makeLog(logger: Logger, enabled: Boolean): KLog =
        makeLog(resolveName(logger.javaClass), enabled)

/**
 * get logger for the method, assuming it was declared at the logger file/class
 */
inline private fun makeLog(noinline func: () -> Unit, enabled: Boolean): KLog =
        makeLog(resolveName(func), enabled)

// get logger by the object. Not sure if we will need this.
//inline internal fun makeLog(obj: Any): Log = makeLog(obj.javaClass)


/**
 * get class name for function by the package of the function
 */
inline private fun resolveName(noinline func: () -> Unit): String {
    val name = func.javaClass.simpleName
    val slicedName = when {
        name.contains("Kt$") -> name.substringBefore("Kt$")
        name.contains("$") -> name.substringBefore("$")
        else -> name
    }
    return slicedName
}

/**
 * get class name for java class (that usually represents kotlin class)
 */
inline private fun <T : Any> resolveName(forClass: Class<T>): String =
        unwrapCompanionClass(forClass).simpleName


/**
 * unwrap companion class to enclosing class given a Java Class
 */
inline private fun <T : Any> unwrapCompanionClass(clazz: Class<T>): Class<*> {
    if (clazz.enclosingClass != null) {
        try {
            val field = clazz.enclosingClass.getField(clazz.simpleName)
            if (Modifier.isStatic(field.modifiers) && field.type == clazz) {
                // && field.get(null) === obj
                // the above might be safer but problematic with initialization order
                return clazz.enclosingClass
            }
        } catch(e: Exception) {
            //ok, it is not a companion object
        }
    }
    return clazz
}
