package com.chiko0085.testgym

import platform.UIKit.UIDevice
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun openWebLink(url: String) {

}

actual fun getCurrentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

actual fun formatEpochToDate(millis: Long): String = ""

actual fun parseDateToMillis(dateStr: String): Long? = null

actual fun initFirebase() { }