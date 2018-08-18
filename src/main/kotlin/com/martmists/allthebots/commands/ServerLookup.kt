package com.martmists.allthebots.commands

import com.google.gson.Gson
import com.martmists.allthebots.entities.mcquery.MCQuery
import com.martmists.allthebots.entities.mcquery.QueryResponse
import com.martmists.allthebots.extensions.discordEscaped
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.SubCommand
import com.martmists.chitose.entities.cmd.argument
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.io.File


data class ServerEntry(
        val name: String,
        val ip: String,
        val port: Int,
        val addedBy: Long
)


fun saveServers(servers: MutableList<ServerEntry>){
    val writer = File("servers.json").writer()
    Gson().toJson(servers.toTypedArray(), writer)
    writer.close()
}

class RegisterServer(private val servers: MutableList<ServerEntry>): SubCommand() {
    override val name = "register"
    override val description = "Register a server"
    override val usage = "serverlookup register <name> <ip> [port]"
    override val example = "serverlookup register MyServer my.ip.here 25565"

    init {
        arguments += argument<String>("name")
        arguments += argument<String>("ip")
        arguments += argument("port", defaultValue = 25565)
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String
        val ip = ctx.args["ip"] as String
        val port = ctx.args["port"] as Int
        val owner = ctx.author.idLong

        val server = servers.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }

        if (server == null) {
            val query = MCQuery(ip, port)
            try {
                query.fullStat()
            } catch(e: KotlinNullPointerException){
                return ctx.send("Server not online, invalid address or query not enabled!!")
            }

            servers.add(ServerEntry(name, ip, port, owner))
            saveServers(servers)
            ctx.send("Server '$name' successfully registered!")
        } else {
            ctx.send("Server '$name' is already registered!")
        }

    }
}


class RemoveServer(private val servers: MutableList<ServerEntry>): SubCommand() {
    override val name = "remove"
    override val description = "Remove a server"
    override val usage = "serverlookup remove <name>"
    override val example = "serverlookup remove MyServer"

    init {
        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String
        val owner = ctx.author.idLong

        val server = servers.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }

        if (server == null) {
            ctx.send("Server '$name' is not registered!")
        } else {
            if (server.addedBy != owner){
                return ctx.send("You don't own this server!")
            }
            servers.remove(server)
            saveServers(servers)
            ctx.send("Server '$name' successfully removed registered!")
        }

    }
}


class EditServer(private val servers: MutableList<ServerEntry>): SubCommand() {
    override val name = "edit"
    override val description = "Edit a server"
    override val usage = "serverlookup edit <name> <ip> [port]"
    override val example = "serverlookup edit MyServer my.ip.here 25565"

    init {
        arguments += argument<String>("name")
        arguments += argument<String>("ip")
        arguments += argument("port", defaultValue = 25565)
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String
        val ip = ctx.args["ip"] as String
        val port = ctx.args["port"] as Int
        val owner = ctx.author.idLong

        val server = servers.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }

        if (server == null) {
            ctx.send("Server '$name' is not registered!")
        } else {
            val query = MCQuery(ip, port)
            try {
                query.fullStat()
            } catch(e: KotlinNullPointerException){
                return ctx.send("Server not online, invalid address or query not enabled!!")
            }

            if (server.addedBy != owner){
                return ctx.send("You don't own this server!")
            }
            servers.remove(server)
            servers.add(ServerEntry(name, ip, port, owner))
            saveServers(servers)
            ctx.send("Server '$name' successfully updated!")
        }

    }
}


class ServerLookup: Command() {
    override val description = "Look up a server"
    override val example = "serverlookup my.server.ip"
    override val usage = "serverlookup <ip> [port]"

    private val servers: MutableList<ServerEntry>

    init {
        val reader = File("servers.json").reader()
        servers = Gson().fromJson(reader, Array<ServerEntry>::class.java).toMutableList()
        reader.close()

        aliases += "lookup"

        arguments += argument<String>("ip")
        arguments += argument("port", defaultValue = 25565)
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["ip"] as String
        val server = servers.firstOrNull { it.name.toLowerCase() == name.toLowerCase() }
        val ip = if (server == null) name else server.ip
        val port = if (server == null) ctx.args["port"] as Int else server.port
        val query = MCQuery(ip, port)

        val stat: QueryResponse
        try {
            stat = query.fullStat()
        } catch(e: KotlinNullPointerException){
            // e.printStackTrace()
            return ctx.send("Server not online, invalid address or query not enabled!!")
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