package com.martmists.allthebots.entities.ars

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit

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

    abstract class Factory {
        abstract val inits: Array<String>
        abstract fun init(name: String, args: List<Any?>): Token
    }

    class NOP: Token() {
        override fun run(event: MessageReceivedEvent) { }
        companion object Factory: Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Any?>): NOP {
                return NOP()
            }
        }
    }

    class Set(val name: String, val actions: Array<Token>) : Token() {
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

        companion object Factory: Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Any?>): Token {
                return NOP()
            }
        }
    }

    class Embed(val children: Array<EmbedProperty>, val actions: Array<MessageAction>? = null) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("embed")

            override fun init(name: String, args: List<Any?>): Embed {
                val both = args[1] as List<Any?>
                val props = mutableListOf<EmbedProperty>()
                val actions: MutableList<MessageAction> = mutableListOf()
                for (arg in both){
                    if (arg is EmbedProperty){
                        props += arg
                    } else if (arg is MessageAction){
                        actions += arg
                    }
                }

                val actionsArray= if (actions.isEmpty()){
                    null
                } else {
                    actions.toTypedArray()
                }

                return Embed(props.toTypedArray(), actionsArray)
            }
        }

        override fun toString(): String {
            return "Embed([${children.joinToString(", ") { it.toString() }}], [${if (actions != null) actions.joinToString(", ") { it.toString() } else ""}])"
        }

        override fun run(event: MessageReceivedEvent) {
            val embed = EmbedBuilder().apply {
                for (child in children) {
                    child.run(event)

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

            event.channel.sendMessage(embed).queue{
                if (actions != null) {
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }
    }

    class EmbedProperty(val property: String, var value: String) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("title", "description", "color", "field[0]", "field[1]")
            override fun init(name: String, args: List<Any?>): Token {
                return when (name) {
                    "title" -> {
                        val value = args[1] as String
                        EmbedProperty(name, value)
                    }
                    "description" -> {
                        val value = args[1] as String
                        EmbedProperty(name, value)
                    }
                    "color" -> {
                        val value = args[1] as String
                        EmbedProperty(name, value)
                    }
                    "field[0]" -> {
                        val value = args[1] as String
                        EmbedProperty(name, value)
                    }
                    "field[1]" -> {
                        val value = args[1] as String
                        EmbedProperty(name, value)
                    }

                    else -> NOP()
                }
            }
        }

        override fun toString(): String {
            return "Property($property, $value)"
        }

        override fun run(event: MessageReceivedEvent) {
            value = value.replaceEventVars(event)
        }
    }

    class MessageCreate(val content: String, val actions: Array<MessageAction>? = null) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("message")
            override fun init(name: String, args: List<Any?>): MessageCreate {
                val newArgs = args[1] as List<Any?>
                return try {
                    val finalArgs = newArgs.toMutableList()
                    finalArgs.removeIf { it == null }
                    val content = finalArgs[0] as String
                    MessageCreate(content, (finalArgs[1] as List<MessageAction>).toTypedArray())
                } catch(e: Throwable) {
                    val content = newArgs[0] as String
                    MessageCreate(content)
                }
            }
        }

        override fun toString(): String {
            return "MessageCreate($content)"
        }

        override fun run(event: MessageReceivedEvent) {
            event.channel.sendMessage(content.replaceEventVars(event)).queue {
                if (actions != null) {
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }
    }

    class DM(val content: String): Token(){
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("dm", "pm")
            override fun init(name: String, args: List<Any?>): DM {
                val content = args[1] as String
                return DM(content)
            }
        }

        override fun toString(): String {
            return "DM($content)"
        }

        override fun run(event: MessageReceivedEvent) {
            event.author.openPrivateChannel().queue {
                it.sendMessage(content).queue()
            }
        }
    }

    class React(val emote: String) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("react")
            override fun init(name: String, args: List<Any?>): React {
                val emote = args[1] as String
                return React(emote)
            }
        }

        override fun toString(): String {
            return "React($emote)"
        }

        override fun run(event: MessageReceivedEvent) {
            event.message.addReaction(emote.removeSuffix(" ")).queue()
        }
    }

    class Delete(val time: Long = 0) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("delete")
            override fun init(name: String, args: List<Any?>): Delete {
                return if (args.size > 1) {
                    val time = args[1] as String
                    Delete(time.toLong())
                } else {
                    Delete()
                }
            }
        }

        override fun toString(): String {
            return "Delete()"
        }

        override fun run(event: MessageReceivedEvent) {
            event.message.delete().queueAfter(time, TimeUnit.SECONDS)
        }
    }

    class MessageAction(val action: String, val data: Any) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("message.react", "message.delete")

            override fun init(name: String, args: List<Any?>): Token {
                return when (name) {
                    "message.react" -> {
                        val emote = args[1] as List<String>
                        MessageAction("react", emote[0])
                    }
                    "message.delete" -> {
                        if (args.size > 1) {
                            val time = args[1] as String
                            MessageAction("delete", time.toLong())
                        } else {
                            MessageAction("delete", 0L)
                        }
                    }
                    else -> NOP()
                }
            }
        }

        override fun toString(): String {
            return "MessageAction($action, $data)"
        }

        override fun run(event: MessageReceivedEvent) {}

        fun run(message: Message) {
            when (action) {
                "react" -> {
                    data as String
                    message.addReaction(data.removeSuffix(" ")).queue()
                }
                "delete" -> {
                    data as Long
                    message.delete().queueAfter(data, TimeUnit.SECONDS)
                }
            }
        }
    }
}


