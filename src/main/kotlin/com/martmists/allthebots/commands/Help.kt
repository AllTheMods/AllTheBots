package com.martmists.allthebots.commands

import com.martmists.chitose.entities.Core
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.MessageEmbed

class Help : Command() {
    override val description = "get help"
    override val example = "help tag"
    override val usage = "help [command]"

    init {
        arguments += argument("command", "")
    }

    override fun run(ctx: CommandContext) {
        val command = ctx.args["command"] as String

        if (command.isEmpty()) {
            val helpText = Core.handler.commands.filter {
                it.value.userPermissions.any { !ctx.member.hasPermission(it.perm) }
            }.map {
                val result = Core.handler.getHelp(it.value, ctx, false) as String
                result
            }.joinToString("\n")
            ctx.send("```\n$helpText```")
        } else {
            val args = command.split(" ").toMutableList()
            val arg = args.removeAt(0)
            val cmd = Core.handler.commands.values.firstOrNull { it.effectiveName == arg || it.aliases.contains(arg) }
                    ?: return ctx.send("Command '$arg' not found")

            val targetCmd = cmd.getSubCommand(args)

            if (targetCmd.second.isNotEmpty() || targetCmd.first.userPermissions.any { !ctx.member.hasPermission(it.perm) }) {
                ctx.send("(Sub)Command not found.")
            } else {
                val helpText = Core.handler.getHelp(targetCmd.first, ctx, true) as Pair<String, MessageEmbed>
                if (ctx.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
                    ctx.send(helpText.second)
                } else {
                    ctx.send(helpText.first)
                }
            }
        }
    }
}