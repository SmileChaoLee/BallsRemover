package com.smile.ballsremover.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class Settings internal constructor(
    var hasSound : Boolean = true,
    // 0 --> easy for 5 color balls
    // 1 --> difficult for 6 color balls
    var gameLevel : Int = 0,
    var fillColumn : Boolean = true): Parcelable