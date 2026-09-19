package com.mesender.app.presentation.ui.thread

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val BubbleShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 8.dp, bottomEnd = 2.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ItemBubble(
    item: Item,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeText = formatTime(item.createdAt)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelection()
                },
                onLongClick = onToggleSelection
            )
            .padding(horizontal = 16.dp, vertical = 4.dp),
        contentAlignment = Alignment.CenterEnd
    ) {
        Surface(
            modifier = Modifier,
//                .widthIn(max = 300.dp),
            shape = BubbleShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 0.5.dp
        ) {
            Column {
                when (item.type) {
                    ItemType.Text -> Text(
                        formatWhatsAppText(item.textContent.orEmpty()),
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp),
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 15.sp)
                    )
                    ItemType.Link -> LinkContent(
                        item,
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp)
                    )
                    ItemType.Photo -> AsyncImage(
                        model = File(item.mediaPath.orEmpty()),
                        contentDescription = item.title ?: "Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        contentScale = ContentScale.FillWidth,
                        placeholder = ColorPainter(Color.LightGray),
                        error = ColorPainter(Color(0xFFFFCDD2))
                    )
                    ItemType.Video -> AsyncImage(
                        model = File(item.mediaPath.orEmpty()),
                        contentDescription = item.title ?: "Video",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                        contentScale = ContentScale.FillWidth,
                        placeholder = ColorPainter(Color.LightGray),
                        error = ColorPainter(Color(0xFFFFCDD2))
                    )
                }
                Row(
                    modifier = Modifier.padding(start = 8.dp, end = 6.dp, bottom = 4.dp, top = 2.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                    if (item.isRetryableMedia) {
                        Text(
                            "Failed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    if (item.isEdited) {
                        Text(
                            "Edited",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        timeText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkContent(item: Item, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Link,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                item.title ?: "Link",
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        Text(
            formatWhatsAppText(item.textContent.orEmpty()),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
