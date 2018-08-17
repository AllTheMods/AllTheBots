package com.martmists.allthebots.commands

import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class Ping: Command() {
    override val description = "ping"
    override val example = "ping"
    override val usage = "ping"

    override fun run(ctx: CommandContext) {
        val now = OffsetDateTime.now()
        ctx.send("Pong!") {
            it.editMessage("Pong! (ping: ${now.until(it.creationTime, ChronoUnit.MILLIS)}ms | ws: ${ctx.jda.ping}ms)").queue()
        }
    }
}