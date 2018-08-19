package com.martmists.allthebots.commands

import com.martmists.allthebots.entities.AllTheBots
import com.martmists.allthebots.entities.ars.ARSHandler
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission

class AutoResponse : Command() {
    override val description = "ARS System"
    override val example = "ars poll={reactMe: :thumbsup:}"
    override val usage = "ars <ARS>"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        aliases += "ars"

        arguments += argument<String>("ars")
    }

    override fun run(ctx: CommandContext) {
        val ars = ctx.args["ars"] as String

        val func = ARSHandler(ars).parse()
        AllTheBots.listeners += func

        ctx.send("Autoresponse ${func.name} added.")
    }
}