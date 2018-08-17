package com.martmists.allthebots.commands

import com.google.gson.Gson
import com.martmists.chitose.entities.cmd.*
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission
import java.io.File

data class TagData(
        val name: String,
        val content: String
)

fun saveTags(tags: MutableList<TagData>){
    Gson().toJson(tags.toTypedArray(), File("tags.json").writer())
}

class Create(private val tags: MutableList<TagData>): SubCommand() {
    override val description = "Create a tag"
    override val usage = "tag create <tag name> <content>"
    override val example = "tag create help no help for you"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
        arguments += argument<String>("content")
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String
        val content = ctx.args["content"] as String

        if (tags.filter { it.name == name }.isEmpty()) {
            tags.add(TagData(name, content))
            saveTags(tags)
            ctx.send("Tag '$name' successfully created!")
        } else {
            ctx.send("Tag '$name' already exists!")
        }

    }
}

class Delete(private val tags: MutableList<TagData>): SubCommand() {
    override val description = "Delete a tag"
    override val usage = "tag delete <tag name>"
    override val example = "tag delete useless_tag"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String

        val filteredTags = tags.filter { it.name == name }

        if (filteredTags.isNotEmpty()) {
            tags.remove(filteredTags.first())
            saveTags(tags)
            ctx.send("Tag '$name' successfully deleted!")
        } else {
            ctx.send("Tag '$name' does not exist!")
        }

    }
}

class Edit(private val tags: MutableList<TagData>): SubCommand() {
    override val description = "Edit a tag"
    override val usage = "tag edit <tag name> <new content>"
    override val example = "tag edit my_tag this is my tag"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
        arguments += argument<String>("content")
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String
        val content = ctx.args["content"] as String

        val filteredTags = tags.filter { it.name == name }

        if (filteredTags.isEmpty()) {
            ctx.send("Tag '$name' does not exist!")
        } else {
            tags.remove(filteredTags.first())
            tags.add(TagData(name, content))
            saveTags(tags)
            ctx.send("Tag '$name' successfully edited!")
        }

    }
}

class Tag: Command() {
    override val description = "Create, edit, delete or retrieve tags"
    override val usage = "tag <tag name>"
    override val example = "tag needaserver"

    private val tags = Gson().fromJson(File("tags.json").reader(), Array<TagData>::class.java).toMutableList()

    init {
        subcommands += Create(tags)
        subcommands += Delete(tags)
        subcommands += Edit(tags)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext){
        val name = ctx.args["name"] as String

        val filteredTags = tags.filter { it.name == name }

        if (filteredTags.isNotEmpty()) {
            ctx.send(filteredTags.first().content)
        } else {
            ctx.send("Tag '$name' does not exist!")
        }

    }
}