package com.monasoftware.pascher.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monasoftware.pascher.domain.model.WatchSession

@Composable
fun WatchSessionBanner(
    session: WatchSession,
    onLeave: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Watching Together (ID: ${session.sessionId})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${session.participants.size} participants • ${session.movieTitle}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            TextButton(onClick = onLeave) {
                Text("Leave")
            }
        }
    }
}
