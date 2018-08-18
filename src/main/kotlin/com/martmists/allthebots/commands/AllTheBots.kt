package com.martmists.allthebots.commands

import com.martmists.chitose.entities.Core
import com.martmists.chitose.entities.cmd.HelpFormatter
import com.martmists.chitose.entities.provided.*
import com.martmists.chitose.entities.util.TypeConverter
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent

class AllTheBots(
        prefix: Array<String>,
        owners: Array<Long>,
        commandPath: String,
        typeConverter: TypeConverter = DefaultTypeConverter,
        helpFormatter: HelpFormatter = DefaultHelpFormatter,
        totalShards: Int = 1
): Core(prefix, owners, commandPath, typeConverter, helpFormatter, totalShards){
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        if (event.guild.idLong == 254530689225981953) {
            event.guild.controller.addRolesToMember(event.member, event.guild.getRoleById(254530689225981953))
        }
    }
}