package com.martmists.allthebots.entities.ars

import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import java.util.concurrent.TimeUnit

fun String.replaceEventVars(event: MessageReceivedEvent): String {
    var new = this
    for (entry in mapOf<String, String>(
            "%.channelDescription" to event.textChannel.topic,
            "%.channelMention" to event.textChannel.asMention,
            "%.userColor" to event.member.color.rgb.toString(16),
            "%.userNickname" to event.member.effectiveName,
            "%.userMention" to event.member.asMention,
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
    open fun run(any: Any) {}
    open fun getValue(guild: Guild): Any {
        return NOP()
    }

    open fun getValue(any: Any?): Any {
        return NOP()
    }

    // ABCs

    abstract class Factory {
        abstract val inits: Array<String>
        abstract fun init(name: String, args: List<Token>): Token
    }

    abstract class BooleanToken : Token() {
        override fun run(event: MessageReceivedEvent) {}
        open fun getValue(any: Any): Boolean = false
    }

    // Unused classes

    class NOP : Token() {
        override fun toString(): String {
            return "NOP()"
        }

        override fun run(event: MessageReceivedEvent) {}

        companion object Factory : Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Token>): NOP {
                return NOP()
            }
        }
    }

    class Set(val name: String, val actions: List<Token>) : Token() {
        override fun toString(): String {
            return "Set($name, [${actions.joinToString(", ") { it.toString() }}])"
        }

        override fun run(event: MessageReceivedEvent) {
            if (event.message.contentRaw.toLowerCase().startsWith(name.toLowerCase())) {
                for (action in actions) {
                    action.run(event)
                }
            }
        }

        companion object Factory : Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Token>): Token {
                return NOP()
            }
        }
    }

    // Variables

    class StringToken(val value: String) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Token>): Token {
                return NOP()
            }
        }

        override fun toString(): String {
            return value
        }

        override fun run(event: MessageReceivedEvent) {}

        override fun getValue(any: Any?): String {
            return value
        }

        override fun getValue(guild: Guild): String {
            return value
        }
    }

    // Variable getters

    class GetUser(val id: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("user")
            override fun init(name: String, args: List<Token>): Token {
                return if (args.size == 1) {
                    GetUser(args[0])
                } else {
                    GetUser(args[0], args.subList(1, args.size))
                }
            }
        }

        override fun toString(): String {
            return "GetUser($id)"
        }

        override fun getValue(guild: Guild): Member {
            val id = id.getValue(guild) as String
            return guild.getMemberById(id.toLong())
        }

        fun run(guild: Guild) {
            val member = getValue(guild)
            with(member) {
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.guild)
        }
    }

    class GetChannel(val id: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("channel")
            override fun init(name: String, args: List<Token>): Token {
                return if (args.size == 1) {
                    GetChannel(args[0])
                } else {
                    GetChannel(args[0], args.subList(1, args.size))
                }
            }
        }

        override fun toString(): String {
            return "GetChannel($id)"
        }

        override fun getValue(guild: Guild): Channel {
            val id = id.getValue(guild) as String
            return guild.getTextChannelById(id.toLong())
        }

        fun run(guild: Guild) {
            val channel = getValue(guild)
            with(channel) {
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) = run(event.guild)
    }

    class GetRole(val id: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("role")
            override fun init(name: String, args: List<Token>): Token {
                return if (args.size == 1) {
                    GetRole(args[0])
                } else {
                    GetRole(args[0], args.subList(1, args.size))
                }
            }
        }

        override fun toString(): String {
            return "GetRole($id)"
        }

        override fun getValue(guild: Guild): Role {
            val id = id.getValue(guild) as String
            return guild.getRoleById(id.toLong())
        }

        fun run(guild: Guild) {
            val role = getValue(guild)
            with(role) {
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.guild)
        }
    }

    class GetEmote(val content: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("emote")
            override fun init(name: String, args: List<Token>): Token {
                return if (args.size == 1) {
                    GetEmote(args[0])
                } else {
                    GetEmote(args[0], args.subList(1, args.size))
                }
            }
        }

        override fun toString(): String {
            return "GetEmote($content)"
        }

        override fun getValue(guild: Guild): Any {
            return guild.getEmoteById(content.getValue(guild) as String) ?: content
        }

        override fun run(event: MessageReceivedEvent) {
            with(getValue(event.guild)) {
                actions.forEach {
                    it.run(this@with)
                }
            }
        }
    }

    class GetMessage(val id: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("message.get")

            override fun init(name: String, args: List<Token>): Token {
                return if (args.size == 1) {
                    GetMessage(args[0])
                } else {
                    GetMessage(args[0], args.subList(1, args.size))
                }
            }
        }

        fun getValue(channel: TextChannel): Message {
            return channel.getMessageById((id.getValue(channel.guild) as String).toLong()).complete()
        }

        fun run(channel: TextChannel) {
            with(getValue(channel)) {
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.channel)
        }
    }

    // Functions

    class Embed(val children: Array<EmbedProperty>, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("embed")

            override fun init(name: String, args: List<Token>): Embed {
                val props = mutableListOf<EmbedProperty>()
                val actions: MutableList<Token> = mutableListOf()
                for (arg in args) {
                    if (arg is EmbedProperty) {
                        props += arg
                    } else {
                        actions += arg
                    }
                }

                val actionsArray = if (actions.isEmpty()) {
                    listOf()
                } else {
                    actions.toList()
                }

                return Embed(props.toTypedArray(), actionsArray)
            }
        }

        override fun toString(): String {
            return "Embed([${children.joinToString(", ") { it.toString() }}], [${actions.joinToString(", ") { it.toString() }}])"
        }

        override fun run(event: MessageReceivedEvent) {
            val embed = EmbedBuilder().apply {
                for (child in children) {
                    child.run(event)

                    when {
                        child.property == "title" -> {
                            setTitle(child.value.getValue(event.guild) as String)
                        }
                        child.property == "description" -> {
                            setDescription(child.value.getValue(event.guild) as String)
                        }
                        child.property.startsWith("field") -> {
                            val inline = (child.property == "field[1]")
                            val props = (child.value.getValue(event.guild) as String).split("|")
                            addField(props[0], props[1], inline)
                        }
                        child.property == "color" -> {
                            setColor((child.value.getValue(event.guild) as String).toInt(16))
                        }
                        child.property == "image" -> {
                            setImage(child.value.getValue(event.guild) as String)
                        }
                    }
                }
            }.build()

            event.channel.sendMessage(embed).queue {
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }
    }

    class MessageCreate(val content: Token, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("message")
            override fun init(name: String, args: List<Token>): MessageCreate {
                return if (args.size > 1) {
                    MessageCreate(args[0], args.subList(1, args.size))
                } else {
                    MessageCreate(args[0])
                }
            }
        }

        override fun toString(): String {
            return "MessageCreate($content, [${actions.joinToString(",")}])"
        }

        fun run(channel: TextChannel) {
            val content = content.getValue(channel.guild) as String
            channel.sendMessage(content).queue {
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            val content = content.getValue(event.guild) as String
            event.channel.sendMessage(content.replaceEventVars(event)).queue {
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }
    }

    class DM(val content: String, val actions: List<Token> = listOf()) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("dm", "pm")
            override fun init(name: String, args: List<Token>): DM {
                val content = args[0].getValue("") as String
                return if (args.size > 1) {
                    DM(content, args.subList(1, args.size))
                } else {
                    DM(content)
                }
            }
        }

        override fun toString(): String {
            return "DM($content)"
        }

        fun run(member: Member) {
            member.user.openPrivateChannel().queue {
                it.sendMessage(content).queue {
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            event.author.openPrivateChannel().queue {
                it.sendMessage(content.replaceEventVars(event)).queue {
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }
    }

    class React(val emote: Token) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("react")
            override fun init(name: String, args: List<Token>): React {
                return React(args[0])
            }
        }

        override fun toString(): String {
            return "React($emote)"
        }

        fun run(message: Message) {
            val emote = emote.getValue(message.guild) as String
            message.addReaction(emote).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    class Delete(val time: Long = 0) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("delete")
            override fun init(name: String, args: List<Token>): Delete {
                return if (args.isNotEmpty()) {
                    val time = args[0].getValue("") as String
                    Delete(time.toLong())
                } else {
                    Delete()
                }
            }
        }

        override fun toString(): String {
            return "Delete($time)"
        }

        fun run(message: Message) {
            message.delete().queueAfter(time, TimeUnit.SECONDS)
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    class RoleAdd(val role: Token) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("role.add")
            override fun init(name: String, args: List<Token>): Token {
                return RoleAdd(args[0])
            }
        }

        override fun toString(): String {
            return "RoleAdd($role)"
        }

        fun run(member: Member) {
            val roleName = role.getValue(member.guild) as String
            val role = member.guild.getRolesByName(roleName, true).first()
            member.guild.controller.addRolesToMember(member, role).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.member)
        }
    }

    class RoleTake(val role: Token) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("role.remove")
            override fun init(name: String, args: List<Token>): Token {
                return RoleTake(args[0])
            }
        }

        override fun toString(): String {
            return "RoleAdd($role)"
        }

        fun run(member: Member) {
            val roleName = role.getValue(member.guild) as String
            val role = member.guild.getRolesByName(roleName, true).first()
            member.guild.controller.removeSingleRoleFromMember(member, role).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.member)
        }
    }

    class Pin : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("pin")
            override fun init(name: String, args: List<Token>): Token {
                return Pin()
            }
        }

        override fun toString(): String {
            return "Pin()"
        }

        fun run(message: Message) {
            message.pin().queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    // SubFunctions

    class EmbedProperty(val property: String, var value: Token) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("title", "description", "color", "field[0]", "field[1]", "image")
            override fun init(name: String, args: List<Token>): Token {
                return EmbedProperty(name, args[0])
            }
        }

        override fun toString(): String {
            return "Property($property, $value)"
        }

        override fun getValue(guild: Guild): Any {
            return value.getValue(guild)
        }

        override fun run(event: MessageReceivedEvent) {
            value = StringToken((value.getValue(event.guild) as String).replaceEventVars(event))
        }
    }

    class Edit(val content: Token, val after: Token = StringToken("0")) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("attachment", "file")
            override fun init(name: String, args: List<Token>): Token {
                return if (args.size > 1) {
                    Edit(args[0], args[1])
                } else {
                    Edit(args[0])
                }
            }
        }

        override fun toString(): String {
            return "Edit($content, $after)"
        }

        fun run(message: Message) {
            message.editMessage(content.getValue(message.guild) as String).queueAfter((after.getValue(message.guild) as String).toLong(), TimeUnit.SECONDS)
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    // Conditions

    class IfStatement(val ifTokens: Map<BooleanToken, List<Token>>, val ifFalse: List<Token>) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("if")
            override fun init(name: String, args: List<Token>): Token {
                val arguments = args.toMutableList()

                val map = mutableMapOf<BooleanToken, List<Token>>()

                var currentTokens = mutableListOf<Token>()
                val elseTokens = mutableListOf<Token>()

                var currentBool = arguments.removeAt(0) as BooleanToken
                var bool = true

                for (arg in arguments) {
                    println(arg)
                    if (arg is BooleanToken) {
                        currentBool = arg
                    } else if (arg !is StringToken) {
                        if (bool) {
                            currentTokens.add(arg)
                        } else {
                            elseTokens.add(arg)
                        }
                    } else {
                        when (arg.getValue("")) {
                            "else" -> {
                                bool = false
                                map[currentBool] = currentTokens.toList()
                            }
                            "elseif" -> {
                                map[currentBool] = currentTokens.toList()
                                currentTokens = mutableListOf()
                            }
                        }
                    }
                }

                if (currentTokens.isNotEmpty()) {
                    map[currentBool] = currentTokens.toList()
                }

                return IfStatement(map.toMap(), elseTokens.toList())
            }
        }

        override fun toString(): String {
            return "If(${ifTokens.map { "[${it.key}, [${it.value.joinToString(", ")}]]" }.joinToString(", ")}, $ifFalse)"
        }

        override fun getValue(any: Any?): Any {
            for (entry in ifTokens) {
                if (entry.key.getValue(any!!)) {
                    for (token in entry.value) {
                        return token.getValue(any)
                    }
                }
            }
            for (token in ifFalse) {
                return token.getValue(any)
            }

            return 1 // Ideally never happens
        }

        override fun run(event: MessageReceivedEvent) {
            for (entry in ifTokens) {
                if (entry.key.getValue(event)) {
                    for (token in entry.value) {
                        token.run(event)
                    }
                    return
                }
            }
            for (token in ifFalse) {
                token.run(event)
            }
        }
    }

    class MessageContains(val arg: Token, val inverse: Boolean) : BooleanToken() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("message.contains", "!message.contains")

            override fun init(name: String, args: List<Token>): Token {
                return when (name) {
                    "!message.contains" -> MessageContains(args[0], true)
                    "message.contains" -> MessageContains(args[0], false)
                    else -> NOP()
                }
            }
        }

        fun getValue(message: Message): Boolean {
            val bool = message.contentRaw.contains(arg.getValue(message.guild) as String)
            return if (inverse) !bool else bool
        }

        fun getValue(event: MessageReceivedEvent): Boolean {
            val bool = event.message.contentRaw.contains((arg.getValue(event.guild) as String).replaceEventVars(event))
            return if (inverse) !bool else bool
        }

        override fun toString(): String {
            return "MessageContains($arg, $inverse)"
        }
    }

    class NicknameContains(val arg: Token, val inverse: Boolean) : BooleanToken() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("nickname.contains", "!nickname.contains")

            override fun init(name: String, args: List<Token>): Token {
                return when (name) {
                    "!nickname.contains" -> NicknameContains(args[0], true)
                    "nickname.contains" -> NicknameContains(args[0], false)
                    else -> NOP()
                }
            }
        }

        fun getValue(member: Member): Boolean {
            val bool = member.effectiveName.contains(arg.getValue(member.guild) as String)
            return if (inverse) !bool else bool
        }

        fun getValue(event: MessageReceivedEvent): Boolean {
            val bool = event.member.effectiveName.contains((arg.getValue(event.guild) as String).replaceEventVars(event))
            return if (inverse) !bool else bool
        }

        override fun toString(): String {
            return "NicknameContains($arg, $inverse)"
        }
    }

    class Parameter(val arg: String, val inverse: Boolean) : BooleanToken() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf(
                    "parameter", "argument", "param", "arg",
                    "!parameter", "!argument", "!param", "!arg"
            )

            override fun init(name: String, args: List<Token>): Token {
                val match = args[0].getValue("") as String
                val bool = name.startsWith("!")
                return Parameter(match, bool)
            }
        }

        fun getValue(message: Message): Boolean {
            val split = message.contentRaw.split(" ")
            val params = split.subList(1, split.size).joinToString(" ")
            val bool = params == arg
            return if (inverse) !bool else bool
        }

        fun getValue(event: MessageReceivedEvent): Boolean {
            val split = event.message.contentRaw.split(" ")
            val params = split.subList(1, split.size).joinToString(" ")
            val bool = params == arg.replaceEventVars(event)
            return if (inverse) !bool else bool
        }
    }

    class Bool(val op: Token, val left: Token, val right: Token) : BooleanToken() {
        private val operators = mapOf(
                "==" to { a: Any, b: Any -> a == b },
                "!=" to { a: Any, b: Any -> a != b }
        )


        companion object Factory : Token.Factory() {
            override val inits = arrayOf("bool")
            override fun init(name: String, args: List<Token>): Token {
                return Bool(args[0], args[1], args[2])
            }
        }

        override fun getValue(guild: Guild): Any {
            val operator = operators[op.getValue(guild)]!!

            return operator(left.getValue(guild), right.getValue(guild))
        }

        fun getValue(event: MessageReceivedEvent): Any {
            val operator = operators[op.getValue(event.guild)]!!

            return operator(left.getValue(event.guild), right.getValue(event.guild))
        }
    }

    // Values

    class Base(val origin: Token, val radix: Token) : Token() {
        companion object Factory : Token.Factory() {
            override val inits = arrayOf("base")
            override fun init(name: String, args: List<Token>): Token {
                return Base(args[0], args[1])
            }
        }

        override fun getValue(guild: Guild): Any {
            val orig = origin.getValue(guild) as String
            val rad = radix.getValue(guild) as Int

            return orig.toInt(rad)
        }

        override fun run(event: MessageReceivedEvent) {}
    }
}


