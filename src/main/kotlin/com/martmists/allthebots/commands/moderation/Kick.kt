package com.martmists.allthebots.commands.moderation

import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.BotPermission
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member

class Kick : Command() {
    override val description = "Kick users"
    override val example = "kick @dickhead#1234 Dick"
    override val usage = "kick <user>[,user,...] [reason]"
    override val guildOnly = true

    init {
        userPermissions += UserPermission(Permission.KICK_MEMBERS)
        botPermissions += BotPermission(Permission.KICK_MEMBERS)

        arguments += argument<Array<Member>>("users")
        arguments += argument("reason", defaultValue = "No reason given!")
    }

    override fun run(ctx: CommandContext) {
        val members = ctx.args["users"] as Array<Member>
        val reason = ctx.args["reason"] as String

        if (members.any { !ctx.guild.selfMember.canInteract(it) || !ctx.member.canInteract(it) }) {
            val users = members.filter {
                !ctx.guild.selfMember.canInteract(it) || !ctx.member.canInteract(it)
            }.joinToString(", ") { "${it.user.name}#${it.user.discriminator}" }
            return ctx.send("Unable to kick the following users: $users")
        }

        for (user in members) {
            ctx.guild.controller.kick(user, reason).queue()
        }

        ctx.send(":ok_hand:")
    }
}