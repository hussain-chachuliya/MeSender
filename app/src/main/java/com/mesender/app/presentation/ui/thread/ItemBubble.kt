package com.mesender.app.presentation.ui.thread

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import java.io.File

@Composable
fun ItemBubble(
    item: Item,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(Modifier.padding(12.dp)) {
            when (item.type) {
                ItemType.Text -> Text(item.textContent.orEmpty(), style = MaterialTheme.typography.bodyLarge)
                ItemType.Link -> LinkContent(item)
                ItemType.Photo -> AsyncImage(
                    model = File(item.mediaPath.orEmpty()),
                    contentDescription = item.title ?: "Photo",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                ItemType.Video -> AsyncImage(
                    model = File(item.mediaPath.orEmpty()),
                    contentDescription = item.title ?: "Video",
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.isRetryableMedia) {
                    Text(
                        "Media failed to save — re-attach it above.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun LinkContent(item: Item) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                item.title ?: "Link",
                style = MaterialTheme.typography.titleSmall,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1
            )
        }
        Text(
            item.textContent.orEmpty(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2
        )
    }
}