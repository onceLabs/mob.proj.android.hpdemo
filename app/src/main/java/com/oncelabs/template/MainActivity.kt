package com.oncelabs.template

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.oncelabs.template.navigation.Navigation
import com.oncelabs.template.permission.PermissionType
import com.oncelabs.template.ui.theme.TemplateTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * This is a Single Activity application,
 * try and keep this file as clean as possible
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TemplateTheme {
                Navigation()
                /**TODO: Request needed permissions*/
                //RequestAllPermissions(
                //    navigateToSettingsScreen = {
                //        PermissionType.navigateToSettings(context = this)
                //    },
                //    onAllGranted = {
                //        /*TODO: */
                //    }
                //)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(allPermissionsGranted()) {
            /**TODO: */
        }
    }

    /**
     * Check if all permissions are granted
     * @return if all permissions are granted
     */
    private fun allPermissionsGranted(): Boolean {
        val permissions = mutableListOf<String>()
        for (permission in PermissionType.values()) {
            if (permission.minimumVersion == null || Build.VERSION.SDK_INT >= permission.minimumVersion) {
                permissions.add(permission.id)
            }
        }
        return permissions.all {
            ActivityCompat.checkSelfPermission(
                this,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
}

