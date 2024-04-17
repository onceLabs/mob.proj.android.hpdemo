package com.oncelabs.template.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun PermissionModal(
    shouldShow: Boolean,
    title: String,
    body: String,
    confirmText: String,
    denyText: String,
    onConfirm: () -> Unit,
    onDeny: () -> Unit,
) {
    if(shouldShow) {
        Dialog(
            onDismissRequest = {  },
            DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .background(
                        MaterialTheme.colors.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Header
                    Text(
                        title,
                        style = MaterialTheme.typography.subtitle1,
                        color = MaterialTheme.colors.onBackground,
                        textAlign = TextAlign.Center,
                    )

                    // Body
                    Text(
                        text = body,
                        color = MaterialTheme.colors.onBackground,
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                onConfirm()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(confirmText)
                        }

                        Button(
                            onClick = {
                                onDeny()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = MaterialTheme.colors.secondaryVariant
                            )
                        ) {
                            Text(
                                denyText,
                                color = MaterialTheme.colors.onBackground
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewPermissionModal() {
    PermissionModal(
        shouldShow = true,
        title = "Title",
        body = "Some long body",
        confirmText = "Ok",
        denyText = "Later",
        onConfirm = {},
        onDeny = {}
    )
}
