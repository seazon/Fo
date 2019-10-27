package com.seazon.utils.permission

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

interface StoragePermsProvider {

    fun infoStoragePerms()
    fun onStoragePermsGranted(requestCode: Int)
    fun onStoragePermsRejected()

    fun requestStoragePermissions(activity: Activity, requestCode: Int = AppPermissions.DANGEROUS_PERMISSIONS_REQUEST_CODE) {
        ActivityCompat.requestPermissions(activity
                , AppPermissions.STORAGE_PERMISSIONS
                , requestCode
        )
    }

    fun ensureStoragePermissions(activity: Activity, requestCode: Int = AppPermissions.DANGEROUS_PERMISSIONS_REQUEST_CODE) {
        if (AppPermissions.missingPermissions(activity, AppPermissions.STORAGE_PERMISSIONS)) {
            handleStoragePermissions(activity)
        } else {
            onStoragePermsGranted(requestCode)
        }
    }

    private fun handleStoragePermissions(activity: Activity, requestCode: Int = AppPermissions.DANGEROUS_PERMISSIONS_REQUEST_CODE) {
        if (AppPermissions.STORAGE_PERMISSIONS.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example, if the request has been denied previously.
            infoStoragePerms()
        } else {
            requestStoragePermissions(activity, requestCode)
        }
    }

    fun onStoragePermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.size == AppPermissions.STORAGE_PERMISSIONS.size
                && grantResults.none { it != PackageManager.PERMISSION_GRANTED }) {
            onStoragePermsGranted(requestCode)
        } else {
            onStoragePermsRejected()
        }
    }

}