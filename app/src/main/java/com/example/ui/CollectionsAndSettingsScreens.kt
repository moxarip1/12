package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesomeMotion
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ClipCollectionEntity
import com.example.ui.theme.CyberCyanBright
import com.example.ui.theme.ElectricIndigo
import com.example.ui.theme.ViralAmber
import java.util.Locale

@Composable
fun CollectionsAndSettingsScreens(
    uiState: AutoClipUiState,
    onCreateSmartCollection: (String, String) -> Unit,
    onDeleteCollection: (Long) -> Unit
) {
    val context = LocalContext.current
    var showCreateCollectionDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("collections_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, ElectricIndigo.copy(alpha = 0.55f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesomeMotion,
                                contentDescription = "Step 5 Smart Collections",
                                tint = CyberCyanBright
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Step 5 • Thematic Collections (`step5_clustering.py`)",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "AI-clustered multi-segment compilations & social publishing copy",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { showCreateCollectionDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_smart_collection_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyanBright,
                            contentColor = Color(0xFF032329)
                        )
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Collection", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cluster Top Clips Into New Compilation", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(uiState.allCollections, key = { it.id }) { collection ->
            CollectionItemCard(
                collection = collection,
                onCopyPublishingCopy = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                    clipboard?.setPrimaryClip(
                        ClipData.newPlainText("AutoClip Publishing Copy", collection.publishingCopy)
                    )
                },
                onDelete = { onDeleteCollection(collection.id) }
            )
        }
    }

    if (showCreateCollectionDialog) {
        var title by remember {
            mutableStateOf("${uiState.activeProject?.title?.take(28) ?: "AutoClip"} - Golden Nuggets")
        }
        var desc by remember {
            mutableStateOf("Curated compilation of the highest-scoring viral segments above threshold.")
        }

        AlertDialog(
            onDismissRequest = { showCreateCollectionDialog = false },
            title = { Text("New Step 5 Smart Collection") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Collection Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Thematic Clustering Description") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showCreateCollectionDialog = false
                        onCreateSmartCollection(title, desc)
                    }
                ) {
                    Text("Generate Compilation")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateCollectionDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CollectionItemCard(
    collection: ClipCollectionEntity,
    onCopyPublishingCopy: () -> Unit,
    onDelete: () -> Unit
) {
    val clipCount = collection.clipIdsCsv.split(",").filter { it.isNotBlank() }.size.coerceAtLeast(1)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("collection_card_${collection.id}"),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ElectricIndigo.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "$clipCount Clips • ${collection.totalDurationSec}s Total",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberCyanBright,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ViralAmber.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = String.format(Locale.US, "Avg Score %.2f", collection.avgViralScore),
                            style = MaterialTheme.typography.labelMedium,
                            color = ViralAmber,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Collection",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = collection.collectionTitle,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = collection.themeDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = "Generated Social Publishing Copy:",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyanBright
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = collection.publishingCopy,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            OutlinedButton(
                onClick = onCopyPublishingCopy,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Post Copy", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy Compilation Caption & Hashtags")
            }
        }
    }
}
