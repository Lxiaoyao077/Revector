package org.matrix.vector.manager.ui.screens.report
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.matrix.vector.manager.R
import org.matrix.vector.manager.data.github.GitHubRepository

/**
 * What to try before opening an issue.
 *
 * The tracker sits at the foot of the screen rather than being the whole of it, because the first
 * reply to a bug report is a checklist — try the latest canary, update your Zygisk implementation —
 * and a screen can *do* most of that instead of describing it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleshootScreen(
    onNavigateBack: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onOpenCanary: () -> Unit,
) {
    Scaffold(
        // Docked rather than last in the list. It is where the screen is heading, and a reader who
        // has decided to file anyway should not have to scroll past the advice to do it.
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    // Scaffold places its bottom slot against the bottom of the window and leaves
                    // the system bars to it, so the button says where it stands itself.
                    modifier = Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(onClick = { onOpenUrl(GitHubRepository.ISSUES_URL) }) {
                        Icon(
                            Icons.Rounded.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.report_open_tracker))
                    }
                }
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.report_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            item {
                Step(
                    icon = Icons.Rounded.Science,
                    title = stringResource(R.string.report_step_canary),
                    body = stringResource(R.string.report_step_canary_body),
                ) {
                    FilledTonalButton(onClick = onOpenCanary) {
                        Text(stringResource(R.string.home_test_canary))
                    }
                }
            }

            item {
                Step(
                    icon = Icons.AutoMirrored.Rounded.OpenInNew,
                    title = stringResource(R.string.report_step_zygisk),
                    body = stringResource(R.string.report_step_zygisk_body),
                    titleAction = {
                        OutlinedButton(onClick = { onOpenUrl(NEO_ZYGISK) }) { Text("NeoZygisk") }
                    },
                )
            }
        }
    }
}

/** One thing to try, with the control that does it. */
@Composable
private fun Step(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String,
    titleAction: (@Composable () -> Unit)? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(10.dp))
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f, fill = false),
            )
            // A step whose whole action is one link does not need a line of its own for it.
            titleAction?.let {
                Spacer(Modifier.width(8.dp))
                it()
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        action?.let {
            Spacer(Modifier.height(10.dp))
            it()
        }
    }
}

private const val NEO_ZYGISK = "https://github.com/JingMatrix/NeoZygisk"
