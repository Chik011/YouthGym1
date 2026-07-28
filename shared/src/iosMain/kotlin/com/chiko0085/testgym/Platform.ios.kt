package com.chiko0085.testgym

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import dev.gitlive.firebase.storage.Data
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.create
import kotlinx.cinterop.BetaInteropApi

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun openWebLink(url: String) {

}

actual fun openEmailClient(recipient: String, subject: String, body: String) {

}

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatEpochToDate(millis: Long): String = ""

actual fun parseDateToMillis(dateStr: String): Long? = null

actual fun initFirebase() { }

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun createStorageData(bytes: ByteArray): Data {
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    return Data(nsData)
}

actual fun isStorageSupported(): Boolean = true

actual fun saveSetting(key: String, value: String) {
    platform.Foundation.NSUserDefaults.standardUserDefaults.setObject(value, forKey = key)
}

actual fun getSetting(key: String): String? {
    return platform.Foundation.NSUserDefaults.standardUserDefaults.stringForKey(key)
}

actual fun clearSetting(key: String) {
    platform.Foundation.NSUserDefaults.standardUserDefaults.removeObjectForKey(key)
}
