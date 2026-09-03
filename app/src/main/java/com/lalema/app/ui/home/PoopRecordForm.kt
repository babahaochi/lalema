package com.lalema.app.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.lalema.app.ui.theme.LocalIsDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lalema.app.data.PoopAmount
import com.lalema.app.data.PoopColor
import com.lalema.app.data.PoopConsistency
import com.lalema.app.data.PoopSmell
import com.lalema.app.data.PainLevel
import com.lalema.app.ui.theme.GlassInlineTimePicker
import com.lalema.app.ui.theme.GlassMotion
import com.lalema.app.ui.theme.LiquidGlassButton
import com.lalema.app.ui.theme.LiquidGlassCard
import com.lalema.app.ui.theme.LiquidGlassSurface
import com.lalema.app.ui.theme.LiquidGlassTextField
import com.lalema.app.ui.theme.glassContentColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoopRecordForm(
    show: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (
        timeHour: Int,
        timeMinute: Int,
        amount: String,
        consistency: String,
        color: String,
        smell: String,
        painLevel: Int,
        blood: Boolean,
        mucus: Boolean,
        notes: String
    ) -> Unit
) {
    if (!show) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = LocalIsDarkTheme.current
    var timeHour by remember { mutableIntStateOf(java.time.LocalTime.now().hour) }
    var timeMinute by remember { mutableIntStateOf(java.time.LocalTime.now().minute) }
    var selectedAmount by remember { mutableStateOf(PoopAmount.NORMAL.name) }
    var selectedConsistency by remember { mutableStateOf(PoopConsistency.NORMAL.name) }
    var selectedColor by remember { mutableStateOf(PoopColor.BROWN.name) }
    var selectedSmell by remember { mutableStateOf(PoopSmell.NORMAL.name) }
    var selectedPainLevel by remember { mutableIntStateOf(0) }
    var hasBlood by remember { mutableStateOf(false) }
    var hasMucus by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    val sheetContainerColor = if (isDark) Color(0xFF1A1C30) else Color(0xFFF0F0FA)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = sheetContainerColor,
        scrimColor = Color.Black.copy(alpha = 0.3f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (isDark) Color.White.copy(alpha = 0.20f) else Color.Black.copy(alpha = 0.15f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
        ) {
            Text(
                text = "记录排便",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(20.dp))

            GlassTimeCard(
                timeHour = timeHour,
                timeMinute = timeMinute,
                onClick = { showTimePicker = !showTimePicker }
            )

            AnimatedVisibility(
                visible = showTimePicker,
                enter = expandVertically(animationSpec = tween(GlassMotion.DURATION_MEDIUM)) + fadeIn(animationSpec = tween(GlassMotion.DURATION_FAST)),
                exit = shrinkVertically(animationSpec = tween(GlassMotion.DURATION_FAST)) + fadeOut(animationSpec = tween(GlassMotion.DURATION_MICRO))
            ) {
                Spacer(modifier = Modifier.height(12.dp))
                GlassInlineTimePicker(
                    hour = timeHour,
                    minute = timeMinute,
                    onHourChange = { timeHour = it },
                    onMinuteChange = { timeMinute = it }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "量多量少",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PoopAmount.entries.forEach { amount ->
                    ChoiceChip(
                        label = amount.displayName,
                        selected = selectedAmount == amount.name,
                        onClick = { selectedAmount = amount.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "干稀程度",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PoopConsistency.entries.forEach { consistency ->
                    ChoiceChip(
                        label = consistency.displayName,
                        selected = selectedConsistency == consistency.name,
                        onClick = { selectedConsistency = consistency.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "颜色",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PoopColor.entries.take(4).forEach { color ->
                    ColorChip(
                        colorHex = color.colorHex,
                        displayName = color.displayName,
                        selected = selectedColor == color.name,
                        onClick = { selectedColor = color.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                PoopColor.entries.drop(4).forEach { color ->
                    ColorChip(
                        colorHex = color.colorHex,
                        displayName = color.displayName,
                        selected = selectedColor == color.name,
                        onClick = { selectedColor = color.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "气味",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PoopSmell.entries.forEach { smell ->
                    ChoiceChip(
                        label = smell.displayName,
                        selected = selectedSmell == smell.name,
                        onClick = { selectedSmell = smell.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "疼痛程度",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                PainLevel.entries.forEachIndexed { index, level ->
                    ChoiceChip(
                        label = level.displayName,
                        selected = selectedPainLevel == index,
                        onClick = { selectedPainLevel = index },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LiquidGlassSurface(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { hasBlood = !hasBlood },
                    cornerRadius = 6.dp,
                    tint = if (hasBlood) MaterialTheme.colorScheme.error else null,
                    contentPadding = 0.dp,
                    contentAlignment = Alignment.Center
                ) {
                    if (hasBlood) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = glassContentColor(MaterialTheme.colorScheme.error),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "有血", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(24.dp))
                LiquidGlassSurface(
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { hasMucus = !hasMucus },
                    cornerRadius = 6.dp,
                    tint = if (hasMucus) MaterialTheme.colorScheme.error else null,
                    contentPadding = 0.dp,
                    contentAlignment = Alignment.Center
                ) {
                    if (hasMucus) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = glassContentColor(MaterialTheme.colorScheme.error),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "有粘液", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LiquidGlassTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "备注（可选）",
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 16.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            LiquidGlassButton(
                text = "确认记录",
                onClick = {
                    onSubmit(
                        timeHour,
                        timeMinute,
                        selectedAmount,
                        selectedConsistency,
                        selectedColor,
                        selectedSmell,
                        selectedPainLevel,
                        hasBlood,
                        hasMucus,
                        notes
                    )
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun GlassTimeCard(
    timeHour: Int,
    timeMinute: Int,
    onClick: () -> Unit
) {
    LiquidGlassCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "时间: ${String.format("%02d:%02d", timeHour, timeMinute)}",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) {
            glassContentColor(MaterialTheme.colorScheme.primary)
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(GlassMotion.DURATION_CONTROL),
        label = "chipText"
    )

    LiquidGlassSurface(
        modifier = modifier.padding(2.dp),
        cornerRadius = 10.dp,
        tint = if (selected) MaterialTheme.colorScheme.primary else null,
        contentPadding = 0.dp,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ColorChip(
    colorHex: String,
    displayName: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.3f),
        animationSpec = tween(GlassMotion.DURATION_CONTROL),
        label = "colorChipBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) glassContentColor(MaterialTheme.colorScheme.primary) else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(GlassMotion.DURATION_CONTROL),
        label = "colorChipText"
    )

    LiquidGlassSurface(
        modifier = modifier,
        cornerRadius = 12.dp,
        tint = if (selected) MaterialTheme.colorScheme.primary else null,
        contentPadding = 2.dp,
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.clickable { onClick() },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(android.graphics.Color.parseColor(colorHex).let { Color(it) })
                    .border(
                        width = if (selected) 2.5.dp else 1.dp,
                        color = borderColor,
                        shape = CircleShape
                    )
            )
            Text(
                text = displayName,
                fontSize = 10.sp,
                color = textColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
