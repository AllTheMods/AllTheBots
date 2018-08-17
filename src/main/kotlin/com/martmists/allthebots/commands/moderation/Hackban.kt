package com.martmists.allthebots.commands.moderation

import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.BotPermission
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission

class Hackban : Command() {
    override val description = "HackBan users"
    override val example = "hackban 838502072396 Dick"
    override val usage = "hackban <id>[,id,...] [reason]"
    override val guildOnly = true

    init {
        userPermissions += UserPermission(Permission.BAN_MEMBERS)
        botPermissions += BotPermission(Permission.BAN_MEMBERS)

        arguments += argument<Array<Int>>("users")
        arguments += argument("reason", defaultValue = "No reason given!")
    }

    override fun run(ctx: CommandContext) {
        val members = ctx.args["users"] as Array<Int>
        val reason = ctx.args["reason"] as String

        if (members.any {
                    val member = ctx.guild.getMemberById(it.toLong())
                    if (member != null)
                        !ctx.guild.selfMember.canInteract(member) || !ctx.member.canInteract(member)
                    else false
                }) {
            return ctx.send("Unable to kick some users.")
        }

        for (user in members) {
            ctx.guild.controller.ban(user.toString(), 7, reason).queue()
        }

        ctx.send(":ok_hand:")
    }
}