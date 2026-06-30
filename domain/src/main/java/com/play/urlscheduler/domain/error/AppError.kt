package com.play.urlscheduler.domain.error

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    class InvalidUrl(message: String) : AppError(message)
    class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class ShellCommandError(message: String, val exitCode: Int? = null) : AppError(message)
    class BrowserError(message: String) : AppError(message)
    class PermissionDenied(message: String) : AppError(message)
    class UnknownError(message: String, cause: Throwable? = null) : AppError(message, cause)
}
