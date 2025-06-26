package com.smile.ballsremover

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smile.ballsremover.constants.Constants
import com.smile.ballsremover.models.Settings
import com.smile.ballsremover.models.TopPlayer
import com.smile.ballsremover.ui.theme.ColorPrimary

object Composables {

    private const val TAG = "Composables"

    interface ButtonClickListener {
        fun buttonOkClick()
        fun buttonCancelClick() = run { }
    }

    interface ButtonClickListenerString {
        fun buttonOkClick(value: String?)
        fun buttonCancelClick(value: String?) = run { }
    }

    interface SettingClickListener {
        fun hasSoundClick(hasSound: Boolean)
        fun gameLevelClick(gameLevel: Int)
        fun isFillColumnClick(fillColumn: Boolean)
    }

    var mFontSize = 24.sp
        set(value) {
            if (field != value) {
                field = value
                menuItemFontSize = value
            }
        }

    private var menuItemFontSize = mFontSize

    @Composable
    fun textUnitToDp(sp: TextUnit): Dp {
        val dp = with(LocalDensity.current) {
            sp.toDp()
        }
        return dp
    }

    @Composable
    fun HorDivider(
        modifier: Modifier = Modifier,
        color: Color = Color.Black, thickness: Dp = 2.dp
    ) {
        HorizontalDivider(
            modifier = modifier.fillMaxWidth(),
            color = color, thickness = thickness
        )
    }

    @Composable
    fun MenuItemText(
        modifier: Modifier = Modifier,
        text: String, color: Color
    ) {
        Text(modifier = modifier,
            lineHeight = (menuItemFontSize.value + 2f).sp,
            text = text, color = color,
            fontWeight = FontWeight.Normal, fontStyle = FontStyle.Normal,
            fontSize = menuItemFontSize
        )
    }

    @Composable
    fun DropdownMenuItem(
        text: String, color: Color,
        onClick: () -> Unit,
        isDivider: Boolean = true
    ) {
        val itemHeight = textUnitToDp((menuItemFontSize.value + 12).sp)
        androidx.compose.material3.DropdownMenuItem(
            modifier = Modifier
                .height(height = itemHeight)
                .padding(all = 0.dp),
            text = { MenuItemText(text = text, color = color) },
            onClick = { onClick() })
        if (isDivider) HorDivider()
    }

    // For the Top10ComposeActivity
    @Composable
    fun Top10Composable(
        title: String, topPlayers: List<TopPlayer>,
        buttonListener: ButtonClickListener,
        oKStr: String
    ) {
        Log.d(TAG, "Top10Composable.topPlayers.size = ${topPlayers.size}")
        val imageWidth = (mFontSize.value * 3.0).dp
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color(0xff90e5c4))
        ) {
            Text(text = title, fontSize = mFontSize, color = Color.Blue)
            HorizontalDivider(
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .size(10.dp)
            )
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(topPlayers) { topPlayer ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(3f)) {
                            Text(
                                text = topPlayer.player.playerName!!,
                                color = Color.Red, fontSize = mFontSize
                            )
                            Text(
                                text = topPlayer.player.score!!.toString(),
                                color = Color.Red, fontSize = mFontSize
                            )
                        }
                        Image(
                            modifier = Modifier
                                .weight(2f)
                                .size(imageWidth),
                            painter = painterResource(id = topPlayer.medal),
                            contentDescription = "", // Accessibility text
                            contentScale = ContentScale.Fit
                        )
                    }
                    HorizontalDivider(
                        color = Color.Blue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(5.dp)
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = Color.Blue)/*.weight(10f, true)*/,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = { buttonListener.buttonOkClick() },
                    /* modifier = Modifier
                    .size(buttonWidth, height = buttonHeight)
                    .weight(weight = 1f, fill = true)
                    .align(Alignment.CenterHorizontally)
                    .background(color = Color.Green), */
                    colors = ButtonColors(
                        containerColor = Color.Cyan,
                        disabledContainerColor = Color.DarkGray,
                        contentColor = Color.Red,
                        disabledContentColor = Color.LightGray
                    )
                )
                { Text(text = oKStr, fontSize = mFontSize) }
            }
        }
    }

    @Composable
    fun SettingCompose(
        activity: Activity,
        buttonListener: ButtonClickListener,
        textListener: SettingClickListener,
        text: String, backgroundColor: Color,
        setting: Settings
    ) {
        val textColor = Color(0xffffa500)
        val spaceWeight = 1.0f
        val dividerWeight = 1.0f
        var setRowWeight = 3.0f    // for setting row
        var setColumnWeight = 10.0f     // for setting column
        var rowWeight = 5.0f
        var textWeight = 3.0f
        var buttonWeight = spaceWeight * 3.0f
        if (LocalConfiguration.current.orientation ==
            Configuration.ORIENTATION_LANDSCAPE
        ) {
            setRowWeight = 8.0f
            setColumnWeight = 6.0f
            rowWeight = 8.0f
            textWeight = 6.0f
            buttonWeight = spaceWeight * 2.0f
        }

        val onStr = activity.getString(R.string.onStr)
        val offStr = activity.getString(R.string.offStr)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
        ) { // main column
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
            Row(modifier = Modifier.weight(setRowWeight)) {    // setting row
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
                Column(
                    modifier = Modifier
                        .background(color = backgroundColor)
                        .weight(setColumnWeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { // setting column
                    Column(
                        modifier = Modifier.weight(textWeight),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = text, color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = mFontSize
                        )
                    }
                    HorDivider(
                        color = Color.White,
                        modifier = Modifier.weight(dividerWeight)
                    )
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val yesStr = activity.getString(R.string.yesStr)
                        val noStr = activity.getString(R.string.noStr)
                        var hasSound by remember { mutableStateOf(setting.hasSound) }
                        MenuItemText(
                            text = activity.getString(R.string.soundStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    hasSound = !hasSound
                                    textListener.hasSoundClick(hasSound)
                                },
                            text = if (hasSound) onStr else offStr,
                            Color.White
                        )
                        MenuItemText(
                            text = if (hasSound) yesStr else noStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val no1Str = activity.getString(R.string.no1)
                        val no2Str = activity.getString(R.string.no2)
                        val easyStr = activity.getString(R.string.easyStr)
                        val diffStr = activity.getString(R.string.difficultStr)
                        var gameLevel by remember { mutableIntStateOf(setting.gameLevel) }
                        MenuItemText(
                            text = activity.getString(R.string.playerLevelStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                                .clickable {
                                    gameLevel = if (gameLevel == Constants.EASY_LEVEL) Constants.DIFFICULT_LEVEL
                                    else Constants.EASY_LEVEL
                                    textListener.gameLevelClick(gameLevel)
                                },
                            text = if (gameLevel == Constants.EASY_LEVEL) no1Str else no2Str,
                            Color.White
                        )
                        MenuItemText(
                            text = if (gameLevel == Constants.EASY_LEVEL) easyStr else diffStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val showStr = activity.getString(R.string.showStr)
                        val noShowStr = activity.getString(R.string.noShowStr)
                        var fillColumn by remember { mutableStateOf(setting.fillColumn) }
                        MenuItemText(
                            text = activity.getString(R.string.fillColumnStr),
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                        MenuItemText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp).clickable {
                                    fillColumn = !fillColumn
                                    textListener.isFillColumnClick(fillColumn)
                                },
                            text = if (fillColumn) onStr else offStr,
                            Color.White
                        )
                        MenuItemText(
                            text = if (fillColumn) showStr else noShowStr,
                            color = textColor,
                            modifier = Modifier
                                .weight(1f)
                                .padding(all = 0.dp)
                        )
                    }
                    Row(
                        Modifier.weight(rowWeight),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { buttonListener.buttonCancelClick() },
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Red,
                                disabledContainerColor = Color.Red,
                            )
                        ) {
                            Text(
                                text = activity.getString(R.string.cancelStr),
                                fontSize = mFontSize
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(spaceWeight)
                        )
                        Button(
                            onClick = { buttonListener.buttonOkClick() },
                            modifier = Modifier.weight(buttonWeight),
                            colors = ButtonColors(
                                containerColor = Color.Yellow,
                                disabledContentColor = Color.Yellow,
                                contentColor = Color.Blue,
                                disabledContainerColor = Color.Blue
                            )
                        ) {
                            Text(
                                text = activity.getString(R.string.okStr),
                                fontSize = mFontSize
                            )
                        }
                    }
                }   // end of setting column
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(spaceWeight)
                )
            }   // end of setting row
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(spaceWeight)
            )
        }   // end of main column
    }

    @Composable
    fun DialogWithText(
        activity: Activity,
        buttonListener: ButtonClickListener,
        dialogTitle: String, dialogText: String
    ) {
        Log.d(TAG, "DialogWithText")
        val okStr = activity.getString(R.string.okStr)
        val noStr = activity.getString(R.string.noStr)
        val lightRed = Color(0xffff4444)
        AlertDialog(icon = null, title = {
            if (dialogTitle.isNotEmpty())
                Text(
                    text = dialogTitle,
                    fontWeight = FontWeight.Medium, fontSize = mFontSize
                )
        },
            text = {
                if (dialogText.isNotEmpty())
                    Text(
                        text = dialogText,
                        fontWeight = FontWeight.Medium, fontSize = mFontSize
                    )
            },
            containerColor = Color(0xffffa500),
            titleContentColor = Color.White,
            textContentColor = Color.Blue,
            onDismissRequest = { /* isOpen = false */ },
            confirmButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonOkClick()
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = Color.Yellow,
                        disabledContentColor = Color.Yellow
                    )
                ) { Text(text = okStr, fontSize = mFontSize) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonCancelClick()
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = lightRed,
                        disabledContentColor = lightRed
                    )
                ) { Text(text = noStr, fontSize = mFontSize) }
            }
        )
    }

    @Composable
    fun textFieldValue(hintText: String): String {
        var textValue by rememberSaveable { mutableStateOf("") }
        TextField(value = textValue,
            onValueChange = {
                textValue = it
                Log.d(TAG, "textFieldValue.textValue = $textValue")
            },
            textStyle = LocalTextStyle.current.copy(fontSize = mFontSize),
            placeholder = {
                Text(
                    text = hintText, color = Color.LightGray,
                    fontWeight = FontWeight.Light, fontSize = mFontSize
                )
            }
        )
        return textValue
    }

    @Composable
    fun DialogWithTextField(
        activity: Activity,
        buttonListener: ButtonClickListenerString,
        dialogTitle: String, hintText: String
    ) {
        Log.d(TAG, "DialogWithTextField")
        val lightRed = Color(0xffff4444)
        val okStr = activity.getString(R.string.okStr)
        val noStr = activity.getString(R.string.noStr)
        var returnValue = ""
        Log.d(TAG, "DialogWithTextField.AlertDialog")
        AlertDialog(onDismissRequest = { /* isOpen = false */ },
            icon = null, title = {
                if (dialogTitle.isNotEmpty())
                    Text(
                        text = dialogTitle,
                        fontWeight = FontWeight.Medium, fontSize = mFontSize
                    )
            },
            text = {
                if (hintText.isNotEmpty())
                    returnValue = textFieldValue(hintText)
            },
            containerColor = Color(0xffffa500),
            titleContentColor = Color.White,
            textContentColor = Color.Blue,
            confirmButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonOkClick(returnValue)
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = Color.Yellow,
                        disabledContentColor = Color.Yellow
                    )
                ) { Text(text = okStr, fontSize = mFontSize) }
            },
            dismissButton = {
                Button(
                    onClick = {
                        // isOpen = false
                        buttonListener.buttonCancelClick(returnValue)
                    }, colors = ButtonColors(
                        containerColor = ColorPrimary,
                        disabledContainerColor = ColorPrimary,
                        contentColor = lightRed,
                        disabledContentColor = lightRed
                    )
                ) { Text(text = noStr, fontSize = mFontSize) }
            }
        )
    }
}