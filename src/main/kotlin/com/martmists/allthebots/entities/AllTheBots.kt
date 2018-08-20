package com.martmists.allthebots.entities

import com.martmists.allthebots.entities.ars.Set
import com.martmists.chitose.entities.Core
import com.martmists.chitose.entities.cmd.HelpFormatter
import com.martmists.chitose.entities.provided.DefaultHelpFormatter
import com.martmists.chitose.entities.provided.DefaultTypeConverter
import com.martmists.chitose.entities.util.TypeConverter
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

class AllTheBots(
        prefix: Array<String>,
        owners: Array<Long>,
        commandPath: String,
        typeConverter: TypeConverter = DefaultTypeConverter,
        helpFormatter: HelpFormatter = DefaultHelpFormatter,
        totalShards: Int = 1
) : Core(prefix, owners, commandPath, typeConverter, helpFormatter, totalShards) {
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.idLong == 254530689225981953) {
            event.guild.controller.addRolesToMember(event.member, event.guild.getRoleById(297602126853570560)).queue()
        }
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        super.onMessageReceived(event)
        listeners.forEach {
            it.run(event)
        }
    }

    companion object {
        val listeners = mutableListOf<Set>()
    }
}