package com.martmists.allthebots.commands

import com.martmists.chitose.entities.cmd.Command
import com.martmists.chitose.entities.cmd.CommandContext
import com.martmists.chitose.entities.cmd.argument
import com.martmists.chitose.entities.permissions.UserPermission
import jdk.nashorn.api.scripting.ClassFilter
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import net.dv8tion.jda.core.Permission
import javax.script.ScriptContext
import javax.script.SimpleScriptContext

class JavaScript: Command() {
    override val description = "Run JavaScript code"
    override val example = "javascript print('abc');"
    override val usage = "javascript <code>"

    init {
        aliases += "js"

        userPermissions += UserPermission(Permission.MANAGE_SERVER)

        arguments += argument<String>("code")
    }

    class CF: ClassFilter {
        override fun exposeToScripts(p0: String?): Boolean {
            println(p0)
            return false
        }
    }

    override fun run(ctx: CommandContext) {
        val code = ctx.args["code"] as String

        val engine = NashornScriptEngineFactory().getScriptEngine(CF())
        val context = SimpleScriptContext().apply {
            setAttribute("ctx", ctx, ScriptContext.ENGINE_SCOPE)
            setAttribute("jda", ctx.jda, ScriptContext.ENGINE_SCOPE)
        }

        try {
            val result = engine.eval(code, context)
            if (result != null)
                ctx.send(result.toString())
        } catch (e: Exception) {
            ctx.sendException(e)
        }
    }
}