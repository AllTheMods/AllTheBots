package com.martmists.allthebots.entities.ars

import org.parboiled.Node
import org.parboiled.Parboiled
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance


class ARSHandler(private val input: String) {
    companion object {
        val classes = Token::class.nestedClasses.filter { !it.isCompanion && !it.isAbstract }
    }

    fun parse(): Token.Set {
        val parser = Parboiled.createParser(ARSParser::class.java)
        val result = ReportingParseRunner<Any>(parser.Set()).run(input)
        if (result.parseErrors.isNotEmpty()) {
            throw Exception(ErrorUtils.printParseError(result.parseErrors[0]))
        } else {
            fun convert(x: Node<Any>): Any? {
                return when (x.label) {
                    "ID" -> result.inputBuffer.extract(x.startIndex, x.endIndex).removeSuffix(" ")
                    "Whitespace" -> null
                    "EOI" -> null
                    "ARS" -> x.children.map { convert(it) }.first()
                    "Argument" -> x.children.map { convert(it) }.first { it != null }
                    "Arguments" -> x.children.map { convert(it) }
                    "Expressions" -> x.children.map { convert(it) }
                    "Expression" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        val clazz = classes.firstOrNull {
                            it as KClass<Token>
                            val companion = it.companionObjectInstance as Token.Factory
                            companion.inits.contains(args[0])
                        } as KClass<Token>?

                        if (clazz != null) {
                            val companion = clazz.companionObjectInstance as Token.Factory
                            val arguments = (args[1] as List<Token>?) ?: listOf()
                            companion.init(args[0] as String, arguments)
                        } else {
                            null
                        }
                    }
                    "Word" -> Token.StringToken(result.inputBuffer.extract(x.startIndex, x.endIndex).removeSuffix(" "))
                    "Set" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        val value = args[1] as List<Token>
                        Token.Set(args[0] as String, value)
                    }
                    "FirstOf" -> x.children.map { convert(it) }.first { it != null }
                    "OneOrMore" -> x.children.map { convert(it) }
                    "Sequence" -> x.children.map { println(it); convert(it) }
                    else -> null
                }
            }

            return convert(result.parseTreeRoot) as Token.Set
        }
    }
}

fun main(args: Array<String>) {
    // Test function
    val code = """
    x = {
    if: {message.contains: atm1}
    {message: a}
    elseif {message.contains: atm2}
    {message: b
        {delete: 10}
    }
    elseif {message.contains: atm3}
    {message: c
        {react: {emote: 51095}}
    }
    else
    {message: d}
    }
    """.trimIndent()
    println(ARSHandler(code).parse())
}
