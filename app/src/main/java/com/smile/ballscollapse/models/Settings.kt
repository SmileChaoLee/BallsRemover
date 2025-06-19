package com.smile.ballscollapse.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Settings internal constructor(
    var hasSound : Boolean = true,
    var easyLevel : Boolean = true,
    var hasNextBall : Boolean = true): Parcelable