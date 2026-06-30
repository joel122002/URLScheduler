package com.play.urlscheduler.domain.privilege

interface ShellExecutor {
    suspend fun execute(command: String): ShellResult
}

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
) {
    val isSuccess: Boolean get() = exitCode == 0
}
