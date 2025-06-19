package com.smile.ballsremover.models

import com.smile.smilelibraries.player_record_rest.models.Player

class TopPlayer(
    val player: Player = Player(),
    val medal: Int = 0)