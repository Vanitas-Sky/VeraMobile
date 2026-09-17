package com.vera.mobile.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1E293B)
) {
    val annotatedString = parseSimpleMarkdown(text, color)
    Text(
        text = annotatedString,
        modifier = modifier,
        fontSize = 13.sp,
        lineHeight = 20.sp
    )
}

fun parseSimpleMarkdown(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.split("\n")
        lines.forEachIndexed { index, line ->
            var cleanLine = line

            // Encabezados
            when {
                cleanLine.startsWith("### ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, color = defaultColor)) {
                        append(cleanLine.removePrefix("### "))
                    }
                }
                cleanLine.startsWith("## ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, color = defaultColor)) {
                        append(cleanLine.removePrefix("## "))
                    }
                }
                cleanLine.startsWith("# ") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 16.sp, color = defaultColor)) {
                        append(cleanLine.removePrefix("# "))
                    }
                }
                // Si viene tabla residual tipo | Clave | Valor |
                cleanLine.startsWith("|") && cleanLine.endsWith("|") -> {
                    val cells = cleanLine.split("|").filter { it.isNotBlank() && !it.contains("---") }
                    if (cells.isNotEmpty()) {
                        cells.forEachIndexed { i, c ->
                            append(if (i == 0) "• " else " ➔ ")
                            appendInlineMarkdown(c.trim(), defaultColor)
                        }
                    }
                }
                else -> {
                    appendInlineMarkdown(cleanLine, defaultColor)
                }
            }

            if (index < lines.size - 1) {
                append("\n")
            }
        }
    }
}

private fun AnnotatedString.Builder.appendInlineMarkdown(text: String, defaultColor: Color) {
    val parts = text.split("**")
    parts.forEachIndexed { index, part ->
        if (index % 2 == 1) {
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                append(part)
            }
        } else {
            withStyle(SpanStyle(fontWeight = FontWeight.Normal, color = defaultColor)) {
                append(part)
            }
        }
    }
}
