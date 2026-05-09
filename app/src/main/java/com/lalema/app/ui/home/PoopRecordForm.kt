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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.draw.drawBehind
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
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
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
                enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(200)),
                exit = shrinkVertically(animationSpec = tween(200)) + fadeOut(animationSpec = tween(150))
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
                val checkboxShape = RoundedCornerShape(6.dp)
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(checkboxShape)
                        .background(
                            if (hasBlood) MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                            else if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f)
                        )
                        .border(
                            1.dp,
                            if (hasBlood) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            else if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.45f),
                            checkboxShape
                        )
                        .clickable { hasBlood = !hasBlood },
                    contentAlignment = Alignment.Center
                ) {
                    if (hasBlood) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "有血", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(24.dp))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(checkboxShape)
                        .background(
                            if (hasMucus) MaterialTheme.colorScheme.error.copy(alpha = 0.25f)
                            else if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.35f)
                        )
                        .border(
                            1.dp,
                            if (hasMucus) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            else if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.45f),
                            checkboxShape
                        )
                        .clickable { hasMucus = !hasMucus },
                    contentAlignment = Alignment.Center
                ) {
                    if (hasMucus) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "有粘液", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text(text = "备注（可选）") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.50f),
                    unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.40f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            val primaryColor = MaterialTheme.colorScheme.primary
            val submitInteractionSource = remember { MutableInteractionSource() }
            val isSubmitPressed by submitInteractionSource.collectIsPressedAsState()
            val submitScale by animateFloatAsState(
                targetValue = if (isSubmitPressed) 0.96f else 1f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = 600f),
                label = "submitScale"
            )

            Button(
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
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(submitScale),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor.copy(alpha = if (isDark) 0.80f else 0.90f)
                ),
                interactionSource = submitInteractionSource
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "确认记录", fontSize = 16.sp, fontWeight = FontWeight.Medium)
            }

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
    val isDark = LocalIsDarkTheme.current
    val shape = RoundedCornerShape(16.dp)

    val glassBg = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.10f),
                Color.White.copy(alpha = 0.04f)
            )
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.60f),
                Color.White.copy(alpha = 0.35f)
            )
        )
    }

    val borderColor = if (isDark) Color.White.copy(alpha = 0.12f) else Color.White.copy(alpha = 0.50f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = glassBg)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .drawBehind {
                val h = size.height
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = if (isDark) 0.05f else 0.30f),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = h * 0.35f
                    )
                )
            }
            .clickable(onClick = onClick)
            .padding(16.dp)
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
    val isDark = LocalIsDarkTheme.current
    val shape = RoundedCornerShape(10.dp)

    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.30f else 0.20f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.35f)
        },
        animationSpec = tween(250),
        label = "chipBg"
    )

    val borderColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        } else {
            if (isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.40f)
        },
        animationSpec = tween(250),
        label = "chipBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = tween(250),
        label = "chipText"
    )

    Box(
        modifier = modifier
            .padding(2.dp)
            .clip(shape)
            .background(bgColor)
            .border(width = 1.dp, color = borderColor, shape = shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = textColor,
            textAlign = TextAlign.Center
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
        animationSpec = tween(250),
        label = "colorChipBorder"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(250),
        label = "colorChipText"
    )

    Column(
        modifier = modifier
            .padding(2.dp)
            .clickable { onClick() },
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
