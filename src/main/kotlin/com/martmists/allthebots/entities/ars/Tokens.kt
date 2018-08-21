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
    open fun run(any: Any) { }

    // ABCs

    abstract class Factory {
        abstract val inits: Array<String>
        abstract fun init(name: String, args: List<Any>): Token
    }

    abstract class BooleanToken: Token() {
        override fun run(event: MessageReceivedEvent) {}
        abstract fun getValue(event: MessageReceivedEvent): Boolean
        open fun getValue(any: Any): Boolean = false
    }

    // Unused classes

    class NOP: Token() {
        override fun toString(): String {
            return "NOP()"
        }
        override fun run(event: MessageReceivedEvent) { }
        companion object Factory: Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Any>): NOP {
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

        companion object Factory: Token.Factory() {
            override val inits = arrayOf<String>()
            override fun init(name: String, args: List<Any>): Token {
                return NOP()
            }
        }
    }

    // Variable getters

    class GetUser(val id: Long, val actions: List<Token>): Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("user")
            override fun init(name: String, args: List<Any>): Token {
                val id = args[0] as String
                return GetUser(id.toLong(), args.subList(1, args.size) as List<Token>)
            }
        }

        override fun toString(): String {
            return "GetUser($id)"
        }

        fun run(guild: Guild) {
            val member = guild.getMemberById(id)
            with (member){
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.guild)
        }
    }

    class GetChannel(val id: Long, val actions: List<Token>): Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("channel")
            override fun init(name: String, args: List<Any>): Token {
                val id = args[0] as String
                return GetChannel(id.toLong(), args.subList(1, args.size) as List<Token>)
            }
        }

        override fun toString(): String {
            return "GetChannel($id)"
        }

        fun run(guild: Guild) {
            val channel = guild.getTextChannelById(id)
            with (channel){
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.guild)
        }
    }

    class GetRole(val id: Long, val actions: List<Token>): Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("role")
            override fun init(name: String, args: List<Any>): Token {
                val id = args[0] as String
                return GetRole(id.toLong(), args.subList(1, args.size) as List<Token>)
            }
        }

        override fun toString(): String {
            return "GetRole($id)"
        }

        fun run(guild: Guild) {
            val role = guild.getRoleById(id)
            with (role){
                actions.forEach {
                    it.run(this@with)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.guild)
        }
    }

    // Functions

    class Embed(val children: Array<EmbedProperty>, val actions: List<Token> = listOf()) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("embed")

            override fun init(name: String, args: List<Any>): Embed {
                val props = mutableListOf<EmbedProperty>()
                val actions: MutableList<Token> = mutableListOf()
                for (arg in args){
                    if (arg is EmbedProperty){
                        props += arg
                    } else if (arg is Token){
                        actions += arg
                    }
                }

                val actionsArray= if (actions.isEmpty()){
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
                            setColor(child.value.toInt(16))
                        }
                    }
                }
            }.build()

            event.channel.sendMessage(embed).queue{
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }
    }

    class MessageCreate(val content: String, val actions: List<Token> = listOf()) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("message")
            override fun init(name: String, args: List<Any>): MessageCreate {
                val content = args[0] as String
                return if (args.size > 1){
                    MessageCreate(content, args.subList(1, args.size) as List<Token>)
                } else {
                    MessageCreate(content)
                }
            }
        }

        override fun toString(): String {
            return "MessageCreate($content, [${actions.joinToString(",")}])"
        }

        fun run(channel: TextChannel){
            channel.sendMessage(content).queue {
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            event.channel.sendMessage(content.replaceEventVars(event)).queue {
                actions.forEach { action ->
                    action.run(it)
                }
            }
        }
    }

    class DM(val content: String, val actions: List<Token> = listOf()): Token(){
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("dm", "pm")
            override fun init(name: String, args: List<Any>): DM {
                val content = args[0] as String
                return if (args.size > 1){
                    DM(content, args.subList(1, args.size) as List<Token>)
                } else {
                    DM(content)
                }
            }
        }

        override fun toString(): String {
            return "DM($content)"
        }

        fun run(member: Member){
            member.user.openPrivateChannel().queue {
                it.sendMessage(content).queue{
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }

        override fun run(event: MessageReceivedEvent) {
            event.author.openPrivateChannel().queue {
                it.sendMessage(content.replaceEventVars(event)).queue{
                    actions.forEach { action ->
                        action.run(it)
                    }
                }
            }
        }
    }

    class React(val emote: String) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("react")
            override fun init(name: String, args: List<Any>): React {
                val emote = args[0] as String
                return React(emote)
            }
        }

        override fun toString(): String {
            return "React($emote)"
        }

        fun run(message: Message){
            message.addReaction(emote).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    class Delete(val time: Long = 0) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("delete")
            override fun init(name: String, args: List<Any>): Delete {
                return if (args.isNotEmpty()) {
                    val time = args[0] as String
                    Delete(time.toLong())
                } else {
                    Delete()
                }
            }
        }

        override fun toString(): String {
            return "Delete($time)"
        }

        fun run(message: Message){
            message.delete().queueAfter(time, TimeUnit.SECONDS)
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    class RoleAdd(val role: String) :Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("role.add")
            override fun init(name: String, args: List<Any>): Token {
                val roleName = args[0] as String
                return RoleAdd(roleName)
            }
        }

        override fun toString(): String {
            return "RoleAdd($role)"
        }

        fun run(member: Member){
            val role = member.guild.getRolesByName(role, true).first()
            member.guild.controller.addRolesToMember(member, role).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.member)
        }
    }

    class RoleTake(val role: String) :Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("role.remove")
            override fun init(name: String, args: List<Any>): Token {
                val roleName = args[0] as String
                return RoleTake(roleName)
            }
        }

        override fun toString(): String {
            return "RoleAdd($role)"
        }

        fun run(member: Member){
            val role = member.guild.getRolesByName(role, true).first()
            member.guild.controller.removeSingleRoleFromMember(member, role).queue()
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.member)
        }
    }

    class Pin: Token(){
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("pin")
            override fun init(name: String, args: List<Any>): Token {
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

    class EmbedProperty(val property: String, var value: String) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("title", "description", "color", "field[0]", "field[1]")
            override fun init(name: String, args: List<Any>): Token {
                return when (name) {
                    "title" -> {
                        EmbedProperty(name, args[0] as String)
                    }
                    "description" -> {
                        EmbedProperty(name, args[0] as String)
                    }
                    "color" -> {
                        EmbedProperty(name, args[0] as String)
                    }
                    "field[0]" -> {
                        EmbedProperty(name, args[0] as String)
                    }
                    "field[1]" -> {
                        EmbedProperty(name, args[0] as String)
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

    class Edit(val content: String, val after: Long = 0L) : Token() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("attachment", "file")
            override fun init(name: String, args: List<Any>): Token {
                val content = args[0] as String
                return if (args.size > 1){
                    Edit(content, args[1] as Long)
                } else {
                    Edit(content)
                }
            }
        }

        override fun toString(): String {
            return "Edit($content, $after)"
        }

        fun run(message: Message){
            message.editMessage(content).queueAfter(after, TimeUnit.SECONDS)
        }

        override fun run(event: MessageReceivedEvent) {
            run(event.message)
        }
    }

    // Conditions

    class IfStatement(val ifTokens: Map<BooleanToken, List<Token>>, val ifFalse: List<Token>): Token() {
        companion object Factory: Token.Factory(){
            override val inits = arrayOf("if")
            override fun init(name: String, args: List<Any>): Token {
                val arguments = args.toMutableList()

                val map = mutableMapOf<BooleanToken, List<Token>>()

                var currentTokens = mutableListOf<Token>()
                val elseTokens = mutableListOf<Token>()

                var currentBool = arguments.removeAt(0) as BooleanToken
                var bool = true

                for (arg in arguments){
                    println(arg)
                    if (arg is BooleanToken){
                        currentBool = arg
                    } else if (arg is Token) {
                        if (bool){
                            currentTokens.add(arg)
                        } else {
                            elseTokens.add(arg)
                        }
                    } else {
                        when (arg){
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

                if (currentTokens.isNotEmpty()){
                    map[currentBool] = currentTokens.toList()
                }

                return IfStatement(map.toMap(), elseTokens.toList())
            }
        }

        override fun toString(): String {
            return "If(${ifTokens.map { "[${it.key}, [${it.value.joinToString(", ")}]]" }.joinToString(", ")}, $ifFalse)"
        }

        override fun run(event: MessageReceivedEvent) {
            for (entry in ifTokens){
                if (entry.key.getValue(event)){
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

    class MessageContains(val arg: String, val inverse: Boolean): BooleanToken() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("message.contains", "!message.contains")

            override fun init(name: String, args: List<Any>): Token {
                return when (name){
                    "!message.contains" -> MessageContains(args[0] as String, true)
                    "message.contains" -> MessageContains(args[0] as String, false)
                    else -> NOP()
                }
            }
        }

        fun getValue(message: Message): Boolean {
            val bool = message.contentRaw.contains(arg)
            return if (inverse) !bool else bool
        }

        override fun getValue(event: MessageReceivedEvent): Boolean {
            val bool = event.message.contentRaw.contains(arg.replaceEventVars(event))
            return if (inverse) !bool else bool
        }

        override fun toString(): String {
            return "MessageContains($arg, $inverse)"
        }
    }

    class NicknameContains(val arg: String, val inverse: Boolean): BooleanToken() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf("nickname.contains", "!nickname.contains")

            override fun init(name: String, args: List<Any>): Token {
                return when (name){
                    "!nickname.contains" -> NicknameContains(args[0] as String, true)
                    "nickname.contains" -> NicknameContains(args[0] as String, false)
                    else -> NOP()
                }
            }
        }

        fun getValue(member: Member): Boolean {
            val bool = member.effectiveName.contains(arg)
            return if (inverse) !bool else bool
        }

        override fun getValue(event: MessageReceivedEvent): Boolean {
            val bool = event.member.effectiveName.contains(arg.replaceEventVars(event))
            return if (inverse) !bool else bool
        }

        override fun toString(): String {
            return "NicknameContains($arg, $inverse)"
        }
    }

    class Parameter(val arg: String, val inverse: Boolean): BooleanToken() {
        companion object Factory: Token.Factory() {
            override val inits = arrayOf(
                    "parameter", "argument", "param", "arg",
                    "!parameter", "!argument", "!param", "!arg"
            )
            override fun init(name: String, args: List<Any>): Token {
                val match = args[0] as String
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

        override fun getValue(event: MessageReceivedEvent): Boolean {
            val split = event.message.contentRaw.split(" ")
            val params = split.subList(1, split.size).joinToString(" ")
            val bool = params == arg.replaceEventVars(event)
            return if (inverse) !bool else bool
        }
    }
}


