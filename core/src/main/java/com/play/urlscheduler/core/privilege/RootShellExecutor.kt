package com.play.urlscheduler.core.privilege

import com.play.urlscheduler.domain.error.AppError
import com.play.urlscheduler.domain.privilege.ShellExecutor
import com.play.urlscheduler.domain.privilege.ShellResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

class RootShellExecutor @Inject constructor() : ShellExecutor {
    override suspend fun execute(command: String): ShellResult = withContext(Dispatchers.IO) {
        try {
            Timber.d("Executing shell command: $command")
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", command))
            
            val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { it.readText() }
            val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { it.readText() }
            
            val exitCode = process.waitFor()
            
            Timber.d("Shell command exited with $exitCode. stdout: $stdout, stderr: $stderr")
            ShellResult(exitCode, stdout, stderr)
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute shell command: $command")
            throw AppError.ShellCommandError("Failed to execute shell command: ${e.message}", null)
        }
    }
}
