package com.martmists.allthebots.commands

import com.google.gson.Gson
import com.martmists.allthebots.entities.AllTheBots
import com.martmists.allthebots.entities.ars.ARSHandler
import com.martmists.allthebots.entities.ars.Token
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.SubCommand
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission
import java.io.File

fun saveARS(arsTable: MutableMap<String, Pair<String, Token.Set>>) {
    val writer = File("data/ARS.json").writer()
    Gson().toJson(arsTable.map { it.value.first }.toTypedArray(), writer)
    writer.close()
}

class RemoveARS(val ars: MutableMap<String, Pair<String, Token.Set>>) : SubCommand() {
    override val name = "remove"
    override val description = "Remove from ARS System"
    override val example = "ars remove myars"
    override val usage = "ars remove <name>"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext) {
        val name = ctx.args["name"] as String

        if (ars.containsKey(name)) {
            ars.remove(name)
            AllTheBots.listeners.removeIf { it.name == name }
            saveARS(ars)
            ctx.send("Autoresponse '$name' removed.")
        } else {
            ctx.send("Autoresponse '$name' does not exist.")
        }


    }
}

class EditARS(val arsTable: MutableMap<String, Pair<String, Token.Set>>) : SubCommand() {
    override val name = "edit"
    override val description = "edit ARS System"
    override val example = "ars edit myars={...}"
    override val usage = "ars edit <ars>"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("ars")
    }

    override fun run(ctx: CommandContext) {
        val ars = ctx.args["ars"] as String

        val func = ARSHandler(ars).parse()

        if (arsTable.containsKey(func.name)) {
            val old = arsTable[func.name]!!
            arsTable.remove(func.name)
            arsTable[name] = Pair(ars, func)
            AllTheBots.listeners.remove(old.second)
            AllTheBots.listeners.add(func)
            saveARS(arsTable)
            ctx.send("Autoresponse '${func.name}' edited.")
        } else {
            ctx.send("Autoresponse '${func.name}' does not exist.")
        }
    }
}


class InspectARS(val arsTable: MutableMap<String, Pair<String, Token.Set>>) : SubCommand() {
    override val name = "inspect"
    override val description = "inspect ARS System"
    override val example = "ars inspect poll"
    override val usage = "ars inspect <name>"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext) {
        val name = ctx.args["name"] as String


        if (arsTable.containsKey(name)) {
            val content = arsTable[name]!!.first
            ctx.send("```ruby\n$content```")
        } else {
            ctx.send("Autoresponse '${name}' does not exist.")
        }
    }
}


class HelpARS : SubCommand() {
    override val name = "help"
    override val description = "Guide for ARS System"
    override val example = "ars help"
    override val usage = "ars help"

    override fun run(ctx: CommandContext) {
        val guide = File("data/ARSGuide.md").readText()
        ctx.send(guide)
    }
}


class AutoResponse : Command() {
    override val description = "ARS System (see `!ars help` for more info)"
    override val example = "ars poll={reactMe: :thumbsup:}"
    override val usage = "ars <ARS>"

    private val arsTable = mutableMapOf<String, Pair<String, Token.Set>>()

    init {
        val reader = File("data/ARS.json").reader()
        val arsList = Gson().fromJson(reader, Array<String>::class.java).toMutableList()
        reader.close()

        for (entry in arsList) {
            val func = ARSHandler(entry).parse()
            AllTheBots.listeners += func
            arsTable[func.name] = Pair(entry, func)
        }

        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        aliases += "ars"

        subcommands += EditARS(arsTable)
        subcommands += RemoveARS(arsTable)
        subcommands += InspectARS(arsTable)
        subcommands += HelpARS()

        arguments += argument<String>("ars")
    }

    override fun run(ctx: CommandContext) {
        val ars = ctx.args["ars"] as String

        val func = ARSHandler(ars).parse()

        if (arsTable.containsKey(func.name)) {
            ctx.send("Autoresponse '${func.name}' already exists.")
        } else {
            arsTable[func.name] = Pair(ars, func)
            AllTheBots.listeners += func
            saveARS(arsTable)
            ctx.send("Autoresponse ${func.name} added.")
        }
    }
}