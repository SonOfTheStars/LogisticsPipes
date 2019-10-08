package logisticspipes.renderer.shaderbased

import logisticspipes.LogisticsPipes
import org.lwjgl.opengl.GL11.GL_TRUE
import org.lwjgl.opengl.GL20.*

private const val errorTitle  = "Failed to compile shader"
private const val errorBody = "You may experience graphical glitches or bad performance. More info in the log"
private const val timeout = 5000L

fun mkShader(vshs: String, fshs: String): Int {
    val vsh = glCreateShader(GL_VERTEX_SHADER)
    val fsh = glCreateShader(GL_FRAGMENT_SHADER)
    val shader = glCreateProgram()

    glShaderSource(vsh, vshs)
    glCompileShader(vsh)
    for (s in glGetShaderInfoLog(vsh, 65535).trimIndent().lineSequence()) LogisticsPipes.log.warn(s)
    if (glGetShaderi(vsh, GL_COMPILE_STATUS) != GL_TRUE) {
        for ((i, l) in vshs.lineSequence().withIndex()) LogisticsPipes.log.info("${i + 1}: $l")
        LogisticsPipes.log.fatal("Failed to compile vertex shader")
        //notifySend(title = errorTitle, body = errorBody, expireTime = timeout)
        return 0
    }

    glShaderSource(fsh, fshs)
    glCompileShader(fsh)
    for (s in glGetShaderInfoLog(fsh, 65535).trimIndent().lineSequence()) LogisticsPipes.log.warn(s)
    if (glGetShaderi(fsh, GL_COMPILE_STATUS) != GL_TRUE) {
        for ((i, l) in fshs.lineSequence().withIndex()) LogisticsPipes.log.info("${i + 1}: $l")
        LogisticsPipes.log.fatal("Failed to compile fragment shader")
        //notifySend(title = errorTitle, body = errorBody, expireTime = timeout)
        return 0
    }

    glAttachShader(shader, vsh)
    glAttachShader(shader, fsh)
    glLinkProgram(shader)

    for (s in glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) LogisticsPipes.log.warn(s)
    if (glGetProgrami(shader, GL_LINK_STATUS) != GL_TRUE) {
        LogisticsPipes.log.fatal("Failed to link program")
        //notifySend(title = errorTitle, body = errorBody, expireTime = timeout)
        return 0
    }

    glValidateProgram(shader)
    for (s in glGetProgramInfoLog(shader, 65535).trimIndent().lineSequence()) LogisticsPipes.log.warn(s)
    if (glGetProgrami(shader, GL_VALIDATE_STATUS) != GL_TRUE) {
        LogisticsPipes.log.fatal("Failed to validate program")
        //notifySend(title = errorTitle, body = errorBody, expireTime = timeout)
        return 0
    }

    glDeleteShader(fsh)
    glDeleteShader(vsh)

    return shader
}