package com.a10miaomiao.bilimiao.commponents.video

import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoUI
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.config.ViewStyle
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.widget.rcImageView
import splitties.dimensions.dip
import splitties.views.dsl.core.*
import splitties.views.imageResource
import splitties.views.padding

fun MiaoUI.videoItem (
    title: String = "",
    pic: String = "",
    upperName: String = "",
    playNum: String = "",
    damukuNum: String = "",
): View {
    return horizontalLayout {
        layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
        setBackgroundResource(config.selectableItemBackground)
        padding = dip(10)

        views {
            // 封面
            +rcImageView {
                radius = dip(5)
                _network(pic)
            }..lParams {
                width = dip(140)
                height = dip(85)
                rightMargin = dip(5)
            }

            +verticalLayout {

                views {
                    // 标题
                    +textView {
                        ellipsize = TextUtils.TruncateAt.END
                        maxLines = 2
                        setTextColor(config.foregroundColor)
                        _text = title
                    }..lParams(matchParent, matchParent) {
                        weight = 1f
                    }

                    // UP主
                    +horizontalLayout {
                        gravity = Gravity.CENTER_VERTICAL

                        views {
                            +imageView {
                                imageResource = R.drawable.icon_up
                                apply(ViewStyle.roundRect(dip(5)))
                            }..lParams {
                                width = dip(16)
                                rightMargin = dip(3)
                            }

                            +textView {
                                textSize = 12f
                                setTextColor(config.foregroundAlpha45Color)
                                _text = upperName
                            }
                        }
                    }

                    // 播放量，弹幕数量
                    +horizontalLayout {
                        gravity = Gravity.CENTER_VERTICAL

                        views {
                            +imageView {
                                imageResource = R.drawable.ic_play_circle_outline_black_24dp
                            }..lParams {
                                width = dip(16)
                                rightMargin = dip(3)
                            }
                            +textView {
                                textSize = 12f
                                setTextColor(config.foregroundAlpha45Color)
                                _text = NumberUtil.converString(playNum)
                            }
                            +space()..lParams(width = dip(10))
                            +imageView {
                                imageResource = R.drawable.ic_subtitles_black_24dp
                            }..lParams {
                                width = dip(16)
                                rightMargin = dip(3)
                            }
                            +textView {
                                textSize = 12f
                                setTextColor(config.foregroundAlpha45Color)
                                _text = NumberUtil.converString(damukuNum)
                            }
                        }
                    }

                }

            }..lParams(width = matchParent, height = matchParent)

        }

    }
}


