package com.martmists.allthebots.entities.ars

import org.parboiled.Node
import org.parboiled.Parboiled
import org.parboiled.errors.ErrorUtils
import org.parboiled.parserunners.ReportingParseRunner
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance


class ARSHandler(private val input: String) {
    companion object {
        val classes = Token::class.nestedClasses.filter { !it.isCompanion }
    }

    fun parse(): Token.Set {
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
                    "ARS" -> x.children.map { convert(it) }.first()
                    "Expressions" -> x.children.map { convert(it) }
                    "ComplexExpression" -> x.children.map { convert(it) }
                    "Expression" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        val clazz = classes.firstOrNull { it as KClass<Token>
                            val companion = it.companionObjectInstance as Token.Factory
                            companion.inits.contains(args[0])
                        } as KClass<Token>?

                        return if (clazz != null) {
                            val companion = clazz.companionObjectInstance as Token.Factory
                            companion.init(args[0] as String, args as List<Any>)
                        } else {
                            null
                        }
                    }
                    "Word" -> result.inputBuffer.extract(x.startIndex, x.endIndex)
                    "Set" -> {
                        val args = x.children.map { convert(it) }.toMutableList()
                        args.removeIf { it == null }
                        val value = args[1] as List<Token>
                        Token.Set(args[0] as String, value.toTypedArray())
                    }
                    "FirstOf" -> x.children.map { convert(it) }.first()
                    else -> null
                }
            }

            return convert(result.parseTreeRoot) as Token.Set
        }
    }
}
