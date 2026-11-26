package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SolutionBreakdown
import com.example.ui.theme.LocalMangaPalette
import com.example.ui.theme.PlusJakartaFontFamily

sealed class MathNode {
    data class Row(val items: List<MathNode>) : MathNode()
    data class TextRun(val text: String, val isBold: Boolean = false, val isItalic: Boolean = false) : MathNode()
    data class Fraction(val num: MathNode, val den: MathNode) : MathNode()
    data class Script(val base: MathNode, val sup: MathNode?, val sub: MathNode?) : MathNode()
    data class Parens(val inner: MathNode) : MathNode()
    data class Underline(val inner: MathNode, val isDouble: Boolean = false) : MathNode()
}

object LatexParser {
    private val symbolMap = mapOf(
        "times" to "×",
        "cdot" to "·",
        "circ" to "∘",
        "pi" to "π",
        "theta" to "θ",
        "neq" to "≠",
        "Rightarrow" to "⇒",
        "iff" to "⇔",
        "to" to "→",
        "le" to "≤",
        "ge" to "≥",
        "div" to "÷"
    )

    fun parse(input: String): MathNode {
        val state = ParserState(input.trim())
        return state.parseSequence(stopAt = null)
    }

    fun containsBlockLayout(latex: String): Boolean {
        return latex.contains("\\frac") || latex.contains("\\underline")
    }

    fun toInlineAnnotated(
        latex: String,
        fontSize: TextUnit,
        bold: Boolean = false
    ): AnnotatedString {
        val node = parse(latex)
        return buildAnnotatedString {
            appendNodeInline(node, fontSize, bold)
        }
    }

    private fun AnnotatedString.Builder.appendNodeInline(
        node: MathNode,
        fontSize: TextUnit,
        forceBold: Boolean
    ) {
        when (node) {
            is MathNode.Row -> node.items.forEach { appendNodeInline(it, fontSize, forceBold) }
            is MathNode.TextRun -> {
                val style = SpanStyle(
                    fontWeight = if (forceBold || node.isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontStyle = if (node.isItalic) FontStyle.Italic else FontStyle.Normal
                )
                withStyle(style) { append(node.text) }
            }
            is MathNode.Parens -> {
                append("(")
                appendNodeInline(node.inner, fontSize, forceBold)
                append(")")
            }
            is MathNode.Script -> {
                appendNodeInline(node.base, fontSize, forceBold)
                if (node.sup != null) {
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = fontSize * 0.72f
                        )
                    ) {
                        appendNodeInline(node.sup, fontSize * 0.72f, forceBold)
                    }
                }
                if (node.sub != null) {
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Subscript,
                            fontSize = fontSize * 0.72f
                        )
                    ) {
                        appendNodeInline(node.sub, fontSize * 0.72f, forceBold)
                    }
                }
            }
            is MathNode.Fraction -> {
                append("(")
                appendNodeInline(node.num, fontSize, forceBold)
                append(")/(")
                appendNodeInline(node.den, fontSize, forceBold)
                append(")")
            }
            is MathNode.Underline -> appendNodeInline(node.inner, fontSize, forceBold)
        }
    }

    private class ParserState(private val src: String) {
        private var pos = 0

        fun parseSequence(stopAt: String?): MathNode {
            val items = mutableListOf<MathNode>()
            while (pos < src.length) {
                if (stopAt != null && src.startsWith(stopAt, pos)) {
                    pos += stopAt.length
                    break
                }
                if (stopAt == "}" && src[pos] == '}') {
                    pos++
                    break
                }
                val atom = parseAtom()
                if (atom != null) {
                    items.add(atom)
                }
            }
            return if (items.size == 1) items[0] else MathNode.Row(items)
        }

        private fun parseAtom(): MathNode? {
            skipSpaces()
            if (pos >= src.length) return null
            val ch = src[pos]
            if (ch == '}') return null

            var base: MathNode? = when {
                ch == '{' -> {
                    pos++
                    parseSequence(stopAt = "}")
                }
                ch == '\\' -> parseCommand()
                ch.isLetter() -> {
                    val sb = StringBuilder()
                    // Group single letter variables vs keep single letter italic
                    sb.append(src[pos++])
                    MathNode.TextRun(sb.toString(), isItalic = true)
                }
                ch.isDigit() -> {
                    val sb = StringBuilder()
                    while (pos < src.length && (src[pos].isDigit() || src[pos] == '.')) {
                        sb.append(src[pos++])
                    }
                    MathNode.TextRun(sb.toString(), isItalic = false)
                }
                ch == '-' -> {
                    pos++
                    MathNode.TextRun("−", isItalic = false)
                }
                ch == '+' || ch == '=' || ch == ',' || ch == ':' -> {
                    pos++
                    val display = if (ch == '=') " = " else if (ch == ',') ", " else ch.toString()
                    MathNode.TextRun(display, isItalic = false)
                }
                else -> {
                    pos++
                    MathNode.TextRun(ch.toString(), isItalic = false)
                }
            }

            if (base == null) return null

            // Parse optional ^ and _ scripts
            var sup: MathNode? = null
            var sub: MathNode? = null
            while (pos < src.length) {
                skipSpaces()
                if (pos < src.length && src[pos] == '^') {
                    pos++
                    sup = parseScriptArg(isSuper = true)
                } else if (pos < src.length && src[pos] == '_') {
                    pos++
                    sub = parseScriptArg(isSuper = false)
                } else {
                    break
                }
            }
            if (sup != null || sub != null) {
                base = MathNode.Script(base, sup, sub)
            }
            return base
        }

        private fun parseScriptArg(isSuper: Boolean): MathNode {
            skipSpaces()
            if (pos >= src.length) return MathNode.TextRun("")
            return when (src[pos]) {
                '{' -> {
                    pos++
                    val inner = parseSequence("}")
                    // Replace \circ inside superscript with degree symbol
                    if (isSuper) replaceCircWithDegree(inner) else inner
                }
                '\\' -> {
                    val cmd = parseCommand() ?: MathNode.TextRun("")
                    if (isSuper) replaceCircWithDegree(cmd) else cmd
                }
                else -> MathNode.TextRun(src[pos++].toString())
            }
        }

        private fun replaceCircWithDegree(node: MathNode): MathNode = when (node) {
            is MathNode.TextRun -> if (node.text == "∘") node.copy(text = "°") else node
            is MathNode.Row -> MathNode.Row(node.items.map(::replaceCircWithDegree))
            else -> node
        }

        private fun parseRequiredArg(): MathNode {
            skipSpaces()
            if (pos >= src.length) return MathNode.TextRun("")
            return when (src[pos]) {
                '{' -> {
                    pos++
                    parseSequence("}")
                }
                '\\' -> parseCommand() ?: MathNode.TextRun("")
                else -> {
                    val ch = src[pos++]
                    MathNode.TextRun(ch.toString(), isItalic = ch.isLetter())
                }
            }
        }

        private fun readBraceText(): String {
            skipSpaces()
            if (pos >= src.length || src[pos] != '{') return ""
            pos++
            val sb = StringBuilder()
            var depth = 1
            while (pos < src.length && depth > 0) {
                val c = src[pos++]
                if (c == '{') {
                    depth++
                    sb.append(c)
                } else if (c == '}') {
                    depth--
                    if (depth > 0) sb.append(c)
                } else {
                    sb.append(c)
                }
            }
            return sb.toString()
        }

        private fun parseCommand(): MathNode? {
            pos++ // skip '\'
            if (pos >= src.length) return null
            val first = src[pos]
            if (!first.isLetter()) {
                pos++
                return when (first) {
                    ';', ',', ' ' -> MathNode.TextRun(" ")
                    else -> MathNode.TextRun(first.toString())
                }
            }
            val cmdName = StringBuilder()
            while (pos < src.length && src[pos].isLetter()) {
                cmdName.append(src[pos++])
            }
            return when (val cmd = cmdName.toString()) {
                "frac" -> {
                    val num = parseRequiredArg()
                    val den = parseRequiredArg()
                    MathNode.Fraction(num, den)
                }
                "text" -> {
                    val txt = readBraceText().replace("^2", "²").replace("^3", "³")
                    MathNode.TextRun(txt, isBold = false, isItalic = false)
                }
                "mathbf" -> {
                    val txt = readBraceText().replace("^2", "²").replace("^3", "³")
                    MathNode.TextRun(txt, isBold = true, isItalic = false)
                }
                "underline" -> {
                    val inner = parseRequiredArg()
                    if (inner is MathNode.Underline) {
                        MathNode.Underline(inner.inner, isDouble = true)
                    } else {
                        MathNode.Underline(inner, isDouble = false)
                    }
                }
                "left" -> {
                    skipSpaces()
                    if (pos < src.length && src[pos] == '(') pos++
                    val inner = parseSequence(stopAt = "\\right)")
                    MathNode.Parens(inner)
                }
                "right" -> {
                    if (pos < src.length) pos++
                    null
                }
                else -> {
                    val sym = symbolMap[cmd] ?: cmd
                    val padded = when (cmd) {
                        "times", "cdot", "neq", "Rightarrow", "iff", "to", "le", "ge", "div" -> " $sym "
                        else -> sym
                    }
                    MathNode.TextRun(padded, isItalic = false)
                }
            }
        }

        private fun skipSpaces() {
            while (pos < src.length && src[pos] == ' ') pos++
        }
    }
}

private sealed class RichSegment {
    data class Plain(val text: String) : RichSegment()
    data class InlineMath(val latex: String) : RichSegment()
    data class DisplayMath(val latex: String) : RichSegment()
}

private fun splitRichText(text: String): List<RichSegment> {
    val result = mutableListOf<RichSegment>()
    var i = 0
    val plain = StringBuilder()
    while (i < text.length) {
        if (text.startsWith("$$", i)) {
            if (plain.isNotEmpty()) {
                result.add(RichSegment.Plain(plain.toString()))
                plain.clear()
            }
            val end = text.indexOf("$$", i + 2)
            if (end != -1) {
                result.add(RichSegment.DisplayMath(text.substring(i + 2, end)))
                i = end + 2
            } else {
                plain.append(text.substring(i))
                break
            }
        } else if (text[i] == '$') {
            if (plain.isNotEmpty()) {
                result.add(RichSegment.Plain(plain.toString()))
                plain.clear()
            }
            val end = text.indexOf('$', i + 1)
            if (end != -1) {
                result.add(RichSegment.InlineMath(text.substring(i + 1, end)))
                i = end + 1
            } else {
                plain.append(text.substring(i))
                break
            }
        } else {
            plain.append(text[i])
            i++
        }
    }
    if (plain.isNotEmpty()) {
        result.add(RichSegment.Plain(plain.toString()))
    }
    return result
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MathRichText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalMangaPalette.current.ink,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.SemiBold
) {
    val segments = remember(text) { splitRichText(text) }
    val hasBlockElement = remember(segments) {
        segments.any {
            it is RichSegment.DisplayMath ||
                (it is RichSegment.InlineMath && LatexParser.containsBlockLayout(it.latex))
        }
    }

    if (!hasBlockElement) {
        val annotated = remember(segments, fontSize) {
            buildAnnotatedString {
                for (seg in segments) {
                    when (seg) {
                        is RichSegment.Plain -> append(seg.text)
                        is RichSegment.InlineMath -> append(
                            LatexParser.toInlineAnnotated(seg.latex, fontSize)
                        )
                        is RichSegment.DisplayMath -> append(
                            LatexParser.toInlineAnnotated(seg.latex, fontSize)
                        )
                    }
                }
            }
        }
        Text(
            text = annotated,
            modifier = modifier,
            color = color,
            style = TextStyle(
                fontFamily = PlusJakartaFontFamily,
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontWeight = fontWeight
            )
        )
        return
    }

    // Mixed layout containing stacked fractions or display blocks
    val blocks = remember(segments) {
        val out = mutableListOf<List<RichSegment>>()
        var current = mutableListOf<RichSegment>()
        for (seg in segments) {
            if (seg is RichSegment.DisplayMath) {
                if (current.isNotEmpty()) {
                    out.add(current)
                    current = mutableListOf()
                }
                out.add(listOf(seg))
            } else {
                current.add(seg)
            }
        }
        if (current.isNotEmpty()) out.add(current)
        out
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (block in blocks) {
            if (block.size == 1 && block[0] is RichSegment.DisplayMath) {
                val latex = (block[0] as RichSegment.DisplayMath).latex
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    contentAlignment = Alignment.CenterStart
                ) {
                    MathExpressionView(
                        latex = latex,
                        color = color,
                        fontSize = fontSize * 1.06f
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    for (seg in block) {
                        when (seg) {
                            is RichSegment.Plain -> {
                                val tokens = seg.text.split(Regex("(?<=\\s)"))
                                for (tok in tokens) {
                                    if (tok.isNotEmpty()) {
                                        Text(
                                            text = tok,
                                            modifier = Modifier.align(Alignment.CenterVertically),
                                            color = color,
                                            style = TextStyle(
                                                fontFamily = PlusJakartaFontFamily,
                                                fontSize = fontSize,
                                                lineHeight = lineHeight,
                                                fontWeight = fontWeight
                                            )
                                        )
                                    }
                                }
                            }
                            is RichSegment.InlineMath -> {
                                if (LatexParser.containsBlockLayout(seg.latex)) {
                                    MathExpressionView(
                                        latex = seg.latex,
                                        modifier = Modifier
                                            .align(Alignment.CenterVertically)
                                            .padding(horizontal = 2.dp),
                                        color = color,
                                        fontSize = fontSize
                                    )
                                } else {
                                    Text(
                                        text = LatexParser.toInlineAnnotated(seg.latex, fontSize),
                                        modifier = Modifier.align(Alignment.CenterVertically),
                                        color = color,
                                        style = TextStyle(
                                            fontFamily = PlusJakartaFontFamily,
                                            fontSize = fontSize,
                                            lineHeight = lineHeight,
                                            fontWeight = fontWeight
                                        )
                                    )
                                }
                            }
                            is RichSegment.DisplayMath -> Unit
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MathExpressionView(
    latex: String,
    modifier: Modifier = Modifier,
    color: Color = LocalMangaPalette.current.ink,
    fontSize: TextUnit = 16.sp
) {
    val node = remember(latex) { LatexParser.parse(latex) }
    MathNodeView(
        node = node,
        modifier = modifier,
        color = color,
        fontSize = fontSize
    )
}

@Composable
private fun MathNodeView(
    node: MathNode,
    modifier: Modifier = Modifier,
    color: Color,
    fontSize: TextUnit
) {
    when (node) {
        is MathNode.Row -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (item in node.items) {
                    MathNodeView(
                        node = item,
                        color = color,
                        fontSize = fontSize
                    )
                }
            }
        }
        is MathNode.TextRun -> {
            Text(
                text = node.text,
                modifier = modifier,
                color = color,
                style = TextStyle(
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = fontSize,
                    fontWeight = if (node.isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontStyle = if (node.isItalic) FontStyle.Italic else FontStyle.Normal
                )
            )
        }
        is MathNode.Fraction -> {
            Column(
                modifier = modifier
                    .width(IntrinsicSize.Max)
                    .padding(horizontal = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MathNodeView(
                        node = node.num,
                        color = color,
                        fontSize = fontSize * 0.88f
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.8.dp)
                        .background(color)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    MathNodeView(
                        node = node.den,
                        color = color,
                        fontSize = fontSize * 0.88f
                    )
                }
            }
        }
        is MathNode.Script -> {
            val text = remember(node, fontSize) {
                if (node.base !is MathNode.Fraction && node.base !is MathNode.Parens) {
                    buildAnnotatedString {
                        append(
                            LatexParser.toInlineAnnotatedNode(node, fontSize)
                        )
                    }
                } else null
            }
            if (text != null) {
                Text(
                    text = text,
                    modifier = modifier,
                    color = color,
                    style = TextStyle(
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            } else {
                Row(modifier = modifier, verticalAlignment = Alignment.Top) {
                    MathNodeView(node = node.base, color = color, fontSize = fontSize)
                    if (node.sup != null) {
                        Box(modifier = Modifier.padding(top = 1.dp)) {
                            MathNodeView(node = node.sup, color = color, fontSize = fontSize * 0.7f)
                        }
                    }
                }
            }
        }
        is MathNode.Parens -> {
            Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "(",
                    color = color,
                    style = LocalTextStyle.current.copy(
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = fontSize * 1.35f,
                        fontWeight = FontWeight.Medium
                    )
                )
                MathNodeView(node = node.inner, color = color, fontSize = fontSize)
                Text(
                    text = ")",
                    color = color,
                    style = LocalTextStyle.current.copy(
                        fontFamily = PlusJakartaFontFamily,
                        fontSize = fontSize * 1.35f,
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }
        is MathNode.Underline -> {
            Column(
                modifier = modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MathNodeView(node = node.inner, color = color, fontSize = fontSize)
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.5.dp)
                        .background(color)
                )
                if (node.isDouble) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.5.dp)
                            .background(color)
                    )
                }
            }
        }
    }
}

private fun LatexParser.toInlineAnnotatedNode(node: MathNode, fontSize: TextUnit): AnnotatedString {
    return buildAnnotatedString {
        when (node) {
            is MathNode.Script -> {
                append(toInlineAnnotatedNode(node.base, fontSize))
                if (node.sup != null) {
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Superscript,
                            fontSize = fontSize * 0.72f
                        )
                    ) {
                        append(toInlineAnnotatedNode(node.sup, fontSize * 0.72f))
                    }
                }
                if (node.sub != null) {
                    withStyle(
                        SpanStyle(
                            baselineShift = BaselineShift.Subscript,
                            fontSize = fontSize * 0.72f
                        )
                    ) {
                        append(toInlineAnnotatedNode(node.sub, fontSize * 0.72f))
                    }
                }
            }
            is MathNode.Row -> node.items.forEach { append(toInlineAnnotatedNode(it, fontSize)) }
            is MathNode.TextRun -> {
                withStyle(
                    SpanStyle(
                        fontWeight = if (node.isBold) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontStyle = if (node.isItalic) FontStyle.Italic else FontStyle.Normal
                    )
                ) { append(node.text) }
            }
            is MathNode.Parens -> {
                append("(")
                append(toInlineAnnotatedNode(node.inner, fontSize))
                append(")")
            }
            is MathNode.Fraction -> {
                append("(")
                append(toInlineAnnotatedNode(node.num, fontSize))
                append(")/(")
                append(toInlineAnnotatedNode(node.den, fontSize))
                append(")")
            }
            is MathNode.Underline -> append(toInlineAnnotatedNode(node.inner, fontSize))
        }
    }
}

@Composable
fun SolutionNotebookBox(
    breakdown: SolutionBreakdown,
    modifier: Modifier = Modifier
) {
    val palette = LocalMangaPalette.current

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(palette.notebookPaper)
                .border(2.dp, palette.ink)
                .drawBehind {
                    // Red left accent bar (6.dp)
                    drawRect(
                        color = palette.crimson,
                        topLeft = Offset.Zero,
                        size = androidx.compose.ui.geometry.Size(6.dp.toPx(), size.height)
                    )
                    // Ruled blue notebook lines
                    val lineGap = 30.dp.toPx()
                    var y = lineGap
                    while (y < size.height) {
                        drawLine(
                            color = palette.notebookLine.copy(alpha = 0.75f),
                            start = Offset(6.dp.toPx(), y),
                            end = Offset(size.width, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        y += lineGap
                    }
                }
                .padding(start = 14.dp, end = 10.dp, top = 10.dp, bottom = 10.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (chain in breakdown.chains) {
                    if (chain.isRaw) {
                        MathExpressionView(
                            latex = chain.left,
                            color = palette.ink,
                            fontSize = 15.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            chain.rows.forEachIndexed { rowIdx, rhs ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (rowIdx == 0) {
                                        MathExpressionView(
                                            latex = chain.left,
                                            color = palette.ink,
                                            fontSize = 15.sp
                                        )
                                    } else {
                                        // Invisible spacer matching first row's LHS for aligned '=' column
                                        Box(modifier = Modifier.padding(end = 0.dp)) {
                                            MathExpressionView(
                                                latex = chain.left,
                                                color = Color.Transparent,
                                                fontSize = 15.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = " = ",
                                        color = palette.ink,
                                        style = TextStyle(
                                            fontFamily = PlusJakartaFontFamily,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    MathExpressionView(
                                        latex = rhs,
                                        color = palette.ink,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (breakdown.explanations.isNotEmpty()) {
            Text(
                text = "💬 Penjelasan:",
                color = palette.ink,
                style = TextStyle(
                    fontFamily = PlusJakartaFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                modifier = Modifier.padding(top = 4.dp)
            )
            for (exp in breakdown.explanations) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.yellowHighlight)
                        .border(2.dp, palette.ink)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    MathRichText(
                        text = "▸ $exp",
                        color = palette.ink,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
