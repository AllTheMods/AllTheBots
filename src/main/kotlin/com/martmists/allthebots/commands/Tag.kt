package com.martmists.allthebots.commands

import com.google.gson.Gson
import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.SubCommand
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.UserPermission
import net.dv8tion.jda.core.Permission
import java.io.File

data class TagData(
        val name: String,
        val content: String
)

fun saveTags(tags: MutableList<TagData>) {
    val writer = File("data/tags.json").writer()
    Gson().toJson(tags.toTypedArray(), writer)
    writer.close()
}

class CreateTag(private val tags: MutableList<TagData>) : SubCommand() {
    override val name = "create"
    override val description = "Create a tag"
    override val usage = "tag create <tag name> <content>"
    override val example = "tag create help no help for you"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
        arguments += argument<String>("content")
    }

    override fun run(ctx: CommandContext) {
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

class DeleteTag(private val tags: MutableList<TagData>) : SubCommand() {
    override val name = "delete"
    override val description = "Delete a tag"
    override val usage = "tag delete <tag name>"
    override val example = "tag delete useless_tag"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext) {
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

class EditTag(private val tags: MutableList<TagData>) : SubCommand() {
    override val name = "edit"
    override val description = "Edit a tag"
    override val usage = "tag edit <tag name> <new content>"
    override val example = "tag edit my_tag this is my tag"

    init {
        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("name")
        arguments += argument<String>("content")
    }

    override fun run(ctx: CommandContext) {
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

class Tag : Command() {
    override val description = "Create, edit, delete or retrieve tags"
    override val usage = "tag <tag name>"
    override val example = "tag needaserver"

    private val tags: MutableList<TagData>

    init {
        val reader = File("data/tags.json").reader()
        tags = Gson().fromJson(reader, Array<TagData>::class.java).toMutableList()
        reader.close()

        subcommands += CreateTag(tags)
        subcommands += DeleteTag(tags)
        subcommands += EditTag(tags)

        arguments += argument<String>("name")
    }

    override fun run(ctx: CommandContext) {
        val name = ctx.args["name"] as String

        val filteredTags = tags.filter { it.name == name }

        if (filteredTags.isNotEmpty()) {
            ctx.send(filteredTags.first().content)
        } else {
            ctx.send("Tag '$name' does not exist!")
        }

    }
}