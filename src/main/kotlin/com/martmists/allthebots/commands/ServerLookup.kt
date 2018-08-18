package com.martmists.allthebots.commands

import com.martmists.allthebots.entities.mcquery.MCQuery
import com.martmists.allthebots.entities.mcquery.QueryResponse
import com.martmists.allthebots.extensions.discordEscaped
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission


class ServerLookup: Command() {
    override val description = "Look up a server"
    override val example = "serverlookup my.server.ip"
    override val usage = "serverlookup <ip> [port]"

    init {
        aliases += "lookup"

        arguments += argument<String>("ip")
        arguments += argument("port", defaultValue = 25565)
    }

    override fun run(ctx: CommandContext){
        val ip = ctx.args["ip"] as String
        val port = ctx.args["port"] as Int
        val query = MCQuery(ip, port)

        println("$ip:$port")

        val stat: QueryResponse
        try {
            stat = query.fullStat()
        } catch(e: KotlinNullPointerException){
            e.printStackTrace()
            return ctx.send("Server not online, or invalid address!")
        }

        val online = "${stat.onlinePlayers}/${stat.maxPlayers}"
        val players = stat.playerList!!.toMutableList()
        while (players.size > 20){
            players.removeAt(0)
        }

        val playerList = players.joinToString("\n") { "- ${it.discordEscaped()}" }
        if (ctx.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
            ctx.send(EmbedBuilder().apply{
                setTitle("Server Lookup Results")
                setDescription(stat.motd!!)
                addField("Online Players", online, false)
                addField("Player List", playerList, false)
            }.build())
        } else {
            ctx.send("""
                **Server Lookup Results**
                ${stat.motd}

                **Online**: $online
                **Players**
                $playerList
            """.trimIndent())
        }
        query.finalize()
    }
}