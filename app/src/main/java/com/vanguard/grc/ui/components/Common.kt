package com.vanguard.grc.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.vanguard.grc.data.content.Difficulty
import com.vanguard.grc.ui.theme.Gold400
import com.vanguard.grc.ui.theme.Graphite500
import com.vanguard.grc.ui.theme.RiskCritical
import com.vanguard.grc.ui.theme.RiskHigh
import com.vanguard.grc.ui.theme.RiskInformational
import com.vanguard.grc.ui.theme.RiskLow
import com.vanguard.grc.ui.theme.RiskMedium
import com.vanguard.grc.ui.theme.StatusIneffective
import com.vanguard.grc.ui.theme.StatusNa
import com.vanguard.grc.ui.theme.StatusNotTested
import com.vanguard.grc.ui.theme.StatusPartial
import com.vanguard.grc.ui.theme.StatusEffective

/** A titled card used throughout the platform. */
@Composable
fun VanguardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    accent: Color? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = accent?.let {
            androidx.compose.foundation.BorderStroke(1.dp, it.copy(alpha = 0.55f))
        }
    ) {
        Column(Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        trailing?.invoke()
    }
}

/** Small uppercase label used to open a block of content. */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier, color: Color = Gold400) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = color,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
}

@Composable
fun StatTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    caption: String? = null
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            color = accent,
            fontWeight = FontWeight.SemiBold
        )
        if (!caption.isNullOrBlank()) {
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun ChipPill(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    filled: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (filled) color else color.copy(alpha = 0.14f))
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = if (filled) Color(0xFF06101C) else color,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
fun RiskBadge(rating: String, modifier: Modifier = Modifier) {
    ChipPill(text = rating, modifier = modifier, color = riskColor(rating))
}

fun riskColor(rating: String): Color = when (rating.lowercase()) {
    "low" -> RiskLow
    "moderate" -> RiskMedium
    "high" -> RiskHigh
    "very high" -> RiskHigh
    "extreme", "critical" -> RiskCritical
    "informational" -> RiskInformational
    else -> StatusNotTested
}

fun controlStatusColor(status: String): Color = when (status.lowercase()) {
    "effective" -> StatusEffective
    "partially effective", "partial" -> StatusPartial
    "ineffective" -> StatusIneffective
    "not applicable", "na" -> StatusNa
    else -> StatusNotTested
}

@Composable
fun DifficultyChip(difficulty: Difficulty, modifier: Modifier = Modifier) {
    val (label, color) = when (difficulty) {
        Difficulty.BEGINNER -> "Beginner" to RiskLow
        Difficulty.INTERMEDIATE -> "Intermediate" to RiskInformational
        Difficulty.ADVANCED -> "Advanced" to RiskHigh
        Difficulty.EXPERT -> "Expert" to RiskCritical
    }
    ChipPill(text = label, modifier = modifier, color = color)
}

@Composable
fun LabeledValue(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BulletList(items: List<String>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { item ->
            Row {
                Box(
                    Modifier
                        .padding(top = 7.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(Gold400)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun InfoCallout(
    text: String,
    modifier: Modifier = Modifier,
    kind: CalloutKind = CalloutKind.INFO
) {
    val (icon, color) = when (kind) {
        CalloutKind.INFO -> Icons.Outlined.Info to MaterialTheme.colorScheme.primary
        CalloutKind.WARNING -> Icons.Outlined.WarningAmber to RiskHigh
        CalloutKind.CAUTION -> Icons.Outlined.WarningAmber to RiskCritical
        CalloutKind.TIP -> Icons.Outlined.Info to RiskLow
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(color.copy(alpha = 0.10f))
            .border(1.dp, color.copy(alpha = 0.30f), MaterialTheme.shapes.small)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

enum class CalloutKind { INFO, WARNING, CAUTION, TIP }

@Composable
fun ProgressLine(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = Graphite500.copy(alpha = 0.35f)
) {
    LinearProgressIndicator(
        progress = progress.coerceIn(0f, 1f),
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(50)),
        color = color,
        trackColor = trackColor
    )
}

@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(38.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        if (action != null) {
            Spacer(Modifier.height(16.dp))
            action()
        }
    }
}

@Composable
fun ListRowCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
    trailingText: String? = null,
    onClick: (() -> Unit)? = null
) {
    VanguardCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (!trailingText.isNullOrBlank()) {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = trailingText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(placeholder, style = MaterialTheme.typography.bodyMedium)
        },
        singleLine = true,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.fillMaxWidth()
    )
}
