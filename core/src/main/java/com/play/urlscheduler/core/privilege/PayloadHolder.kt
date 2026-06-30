package com.play.urlscheduler.core.privilege

/**
 * A singleton object used to securely pass URL payloads between the SchedulerService 
 * and target Activities without relying on Shell/Intent serialization.
 * This completely bypasses the destructive quoting/parsing of the `am start` command.
 */
object PayloadHolder {
    @Volatile
    var currentUrl: String? = null
}
