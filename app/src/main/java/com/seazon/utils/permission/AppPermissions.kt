package com.seazon.utils.permission

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

object AppPermissions {

    @JvmStatic
    val DANGEROUS_PERMISSIONS_REQUEST_CODE = 0

    @JvmField
    val STORAGE_PERMISSIONS = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @JvmStatic
    fun missingPermissions(activity: Activity, permissions: Array<String>) = permissions.any {
        ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
    }
}
