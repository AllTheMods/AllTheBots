package com.martmists.allthebots.entities.ars

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

fun String.replaceEventVars(event: MessageReceivedEvent): String {
    var new = this
    for (entry in mapOf<String, String>(
            "%.channelDescription" to event.textChannel.topic,
            "%.userColor" to event.member.color.rgb.toString(16),
            "%.userNickname" to event.member.effectiveName,
            "%.topRole" to event.member.roles[0].name,

            "%.server" to event.guild.name,
            "%.channel" to event.channel.name,
            "%.user" to event.author.name
    )) {
        new = new.replace(entry.key, entry.value)
    }
    return new
}


abstract class Token {
    abstract fun run(event: MessageReceivedEvent)
}

class NOP : Token() {
    override fun run(event: MessageReceivedEvent) {}
}

data class Set(val name: String, val actions: Array<Token>) : Token() {
    override fun toString(): String {
        return "Set($name, [${actions.joinToString(", ") { it.toString() }}])"
    }

    override fun run(event: MessageReceivedEvent) {
        if (event.message.contentRaw.toLowerCase().startsWith("!${name.toLowerCase()}")) {
            for (action in actions) {
                action.run(event)
            }
        }
    }
}

data class Embed(val children: Array<Token>) : Token() {
    override fun toString(): String {
        return "Embed([${children.joinToString(", ") { it.toString() }}])"
    }

    override fun run(event: MessageReceivedEvent) {
        val embed = EmbedBuilder().apply {
            for (child in children) {
                child.run(event)

                child as EmbedProperty
                when {
                    child.property == "title" -> {
                        setTitle(child.value)
                    }
                    child.property == "description" -> {
                        setDescription(child.value)
                    }
                    child.property.startsWith("field") -> {
                        val inline = (child.property == "field[1]")
                        val props = child.value.split("|")
                        addField(props[0], props[1], inline)
                    }
                    child.property == "color" -> {
                        println(child.value)
                        setColor(child.value.toInt(16))
                    }
                }
            }
        }.build()

        event.channel.sendMessage(embed).queue()
    }
}

data class EmbedProperty(val property: String, var value: String) : Token() {
    override fun toString(): String {
        return "Property($property, $value)"
    }

    override fun run(event: MessageReceivedEvent) {
        value = value.replaceEventVars(event)
    }
}

data class Message(val content: String) : Token() {
    override fun toString(): String {
        return "Message($content)"
    }

    override fun run(event: MessageReceivedEvent) {
        event.channel.sendMessage(content.replaceEventVars(event)).queue()
    }
}

data class React(val emote: String) : Token() {
    override fun toString(): String {
        return "React($emote)"
    }

    override fun run(event: MessageReceivedEvent) {
        event.message.addReaction(emote).queue()
    }
}
