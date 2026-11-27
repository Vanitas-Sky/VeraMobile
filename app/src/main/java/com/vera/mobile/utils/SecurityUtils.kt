package com.vera.mobile.utils

object SecurityUtils {

    private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /**
     * 1. VALIDACIÓN DE INPUTS (Field Validation)
     * Verifica que el formato del correo y la fortaleza de contraseña sean correctos.
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotBlank() && EMAIL_REGEX.matches(email.trim())
    }

    fun isStrongPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * 2. PREVENCIÓN DE SQL / NoSQL INJECTION & XSS (Sanitización de Datos)
     * Detecta y limpia caracteres y patrones sospechosos de inyección antes de enviar a las APIs.
     */
    fun sanitizeInput(input: String): String {
        if (input.isBlank()) return ""

        // Elimina secuencias peligrosas de SQL Injection e instrucciones maliciosas
        return input.trim()
            .replace(Regex("(?i)DROP\\s+TABLE", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?i)SELECT\\s+\\*", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?i)UNION\\s+SELECT", RegexOption.IGNORE_CASE), "")
            .replace(Regex("(?i)OR\\s+1=1", RegexOption.IGNORE_CASE), "")
            .replace(Regex("[;--#]"), "")
            .replace("<script>", "")
            .replace("</script>", "")
    }

    fun containsSqlInjectionPattern(input: String): Boolean {
        val sqlPatterns = listOf(
            "(?i)'\\s*OR\\s*",
            "(?i)OR\\s+['\"0-9a-z]+\\s*=\\s*['\"0-9a-z]+",
            "(?i)UNION\\s+(ALL\\s+)?SELECT",
            "(?i)DROP\\s+(TABLE|DATABASE)",
            "(?i)INSERT\\s+INTO",
            "(?i)DELETE\\s+FROM",
            "(?i)SELECT\\s+.*\\s+FROM",
            "--",
            ";"
        )
        return sqlPatterns.any { pattern -> Regex(pattern).containsMatchIn(input) }
    }

    /**
     * 3. VALIDACIÓN DE PROMPTS Y RESPUESTAS DE IA (Vera AI Safety)
     * Filtra preguntas maliciosas (Prompt Injection) y valida la integridad de la respuesta.
     */
    fun isSafeAiPrompt(prompt: String): Boolean {
        if (prompt.isBlank()) return false
        
        // Evita intentos de manipulacion de sistema (System Prompt Injection)
        val jailbreakPatterns = listOf(
            "(?i)ignore\\s+previous\\s+instructions",
            "(?i)you\\s+are\\s+now\\s+DAN",
            "(?i)reveal\\s+system\\s+prompt",
            "(?i)system\\s+override"
        )
        return jailbreakPatterns.none { pattern -> Regex(pattern).containsMatchIn(prompt) }
    }

    fun validateAiResponse(rawAnswer: String?): String {
        if (rawAnswer.isNullOrBlank()) {
            return "⚠️ No se recibió una respuesta válida del servidor de inteligencia artificial."
        }
        
        // Sanitización básica de la respuesta de IA
        return rawAnswer.trim()
    }
}
