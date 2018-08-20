package com.martmists.allthebots.entities.ars

import org.parboiled.Node
import org.parboiled.Parboiled
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.ReportingParseRunner


class ARSHandler(private val input: String) {

    fun parse(): Set {
        val parser = Parboiled.createParser(ARSParser::class.java)
        val result = ReportingParseRunner<Any>(parser.Set()).run(input)
        if (result.parseErrors.isNotEmpty()) {
            throw Exception(ErrorUtils.printParseError(result.parseErrors[0]))
        } else {
            fun convert(x: Node<Any>): Any? {
                return when (x.label) {
                    "ID" -> result.inputBuffer.extract(x.startIndex, x.endIndex)
                    "Whitespace" -> null
                    "EOI" -> null
                    "ARS" -> x.children.map { convert(it) }
                    "Expressions" -> x.children.map { convert(it) }
                    "ComplexExpression" -> x.children.map { convert(it) }
                    "Expression" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        when (args[0]) {
                            "react" -> {
                                val emote = args[1] as List<String>
                                React(emote[0])
                            }
                            "title" -> {
                                val value = args[1] as List<String>
                                EmbedProperty(args[0].toString(), value[0])
                            }
                            "description" -> {
                                val value = args[1] as List<String>
                                EmbedProperty(args[0].toString(), value[0])
                            }
                            "color" -> {
                                val value = args[1] as List<String>
                                EmbedProperty(args[0].toString(), value[0])
                            }
                            "field[0]" -> {
                                val value = args[1] as List<String>
                                EmbedProperty(args[0].toString(), value[0])
                            }
                            "field[1]" -> {
                                val value = args[1] as List<String>
                                EmbedProperty(args[0].toString(), value[0])
                            }
                            "embed" -> {
                                val value = args[1] as List<Any?>
                                val newArgs = value[0] as List<Token>
                                val props = mutableListOf<EmbedProperty>()
                                val actions: MutableList<MessageAction> = mutableListOf()
                                for (arg in newArgs){
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
                                Embed(props.toTypedArray(), actionsArray)
                            }
                            "message" -> {
                                val newArgs = args[1] as List<Any>
                                try {
                                    val finalArgs = (newArgs[0] as List<Any?>).toMutableList()
                                    finalArgs.removeIf { it == null }
                                    val content = finalArgs[0] as String
                                    MessageCreate(content, (finalArgs[1] as List<MessageAction>).toTypedArray())
                                } catch(e: Throwable) {
                                    val content = newArgs[0] as String
                                    MessageCreate(content)
                                }

                            }
                            "delete" -> {
                                Delete()
                            }
                            "message.react" -> {
                                val emote = args[1] as List<String>
                                MessageAction("react", emote[0])
                            }
                            else -> null
                        }
                    }
                    "Word" -> result.inputBuffer.extract(x.startIndex, x.endIndex)
                    "Set" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        val value = args[1] as List<Any>
                        Set(args[0] as String, (value[0] as List<Token>).toTypedArray())
                    }
                    "FirstOf" -> x.children.map { convert(it) }
                    else -> null
                }
            }

            return convert(result.parseTreeRoot) as Set
        }
    }
}
