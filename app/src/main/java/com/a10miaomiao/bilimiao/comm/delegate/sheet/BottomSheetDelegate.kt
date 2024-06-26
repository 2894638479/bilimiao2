package com.a10miaomiao.bilimiao.comm.delegate.sheet

import android.os.Bundle
import android.view.View
import android.view.accessibility.AccessibilityEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentOnAttachListener
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import cn.a10miaomiao.bilimiao.compose.ComposeFragment
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPageConfigInfo
import com.google.android.material.bottomsheet.BottomSheetBehavior


class BottomSheetDelegate(
    private val activity: AppCompatActivity,
    private val ui: BottomSheetUi,
): NavController.OnDestinationChangedListener, FragmentOnAttachListener {

    private val TAG = BottomSheetDelegate::class.simpleName

    private lateinit var navBottomSheetFragment: NavHostFragment
    private lateinit var navBottomSheetController: NavController

    fun onCreate(savedInstanceState: Bundle?) {
        initBottomSheet()
    }

    private fun initBottomSheet() {
        navBottomSheetFragment = activity.supportFragmentManager
            .findFragmentById(ui.bottomSheetView.id) as NavHostFragment
        navBottomSheetController = navBottomSheetFragment.navController
        MainNavGraph.createGraph(navBottomSheetController, MainNavGraph.dest.template)
        navBottomSheetController.addOnDestinationChangedListener(this)
        navBottomSheetFragment.childFragmentManager.addFragmentOnAttachListener(this)

        ui.bottomSheetBehavior?.let { behavior ->
            behavior.isHideable = true
            behavior.isFitToContents = false
            behavior.state = BottomSheetBehavior.STATE_HIDDEN
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {
                    if (p1 < 0) {
                        ui.bottomSheetMaskView.alpha = (p1 + 1) * 0.6f
                    } else {
                        ui.bottomSheetMaskView.alpha = 0.6f
                    }
                }

                override fun onStateChanged(p0: View, p1: Int) {
                    if (p1 == BottomSheetBehavior.STATE_HIDDEN) {
                        if (navBottomSheetController.currentDestination?.id != MainNavGraph.dest.template) {
                            navBottomSheetController.popBackStack(MainNavGraph.dest.template, false)
                        }
                        ui.bottomSheetMaskView.visibility = View.GONE
                    } else {
                        ui.bottomSheetMaskView.visibility = View.VISIBLE
                    }
                }
            })
        }

        ui.bottomSheetMaskView.setOnClickListener {
            ui.bottomSheetBehavior?.state = BottomSheetBehavior.STATE_HIDDEN
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        ui.bottomSheetTitleView.text = ""
        val behavior = ui.bottomSheetBehavior
        if (destination.id == MainNavGraph.dest.template) {
            if (behavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
                behavior?.state = BottomSheetBehavior.STATE_HIDDEN
            }
        } else {
            if (behavior?.state != BottomSheetBehavior.STATE_EXPANDED) {
                behavior?.state = BottomSheetBehavior.STATE_EXPANDED //设置为展开状态
                behavior?.skipCollapsed = true
                behavior?.state = BottomSheetBehavior.STATE_EXPANDED
                requestFocus()
            }
        }
    }

    private fun requestFocus() {
        val bottomSheetView = ui.bottomSheetView
        bottomSheetView.isFocusable = true
        bottomSheetView.post {
            bottomSheetView.requestFocus()
            bottomSheetView.sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED)
        }
    }

    override fun onAttachFragment(fragmentManager: FragmentManager, fragment: Fragment) {
        if (fragment is MyPage) {
            val config = fragment.pageConfig
            config.setConfig = this::notifyConfigChanged
        }
    }
    private fun notifyConfigChanged(){
        navBottomSheetFragment.childFragmentManager.fragments.lastOrNull().let {
            if(it is MyPage){
                setMyPageConfig(it.pageConfig.configInfo)
            }
        }
    }

    private fun setMyPageConfig(config: MyPageConfigInfo) {
        val title = config.title.replace("\n", " ")
        ui.bottomSheetTitleView.text = title
        ui.bottomSheetView.contentDescription = "${title}，弹出式页面"
    }


    fun onBackPressed(): Boolean {
        // 上滑菜单未关闭则先关闭上滑菜单
        val behavior = ui.bottomSheetBehavior
        if (behavior?.state != BottomSheetBehavior.STATE_HIDDEN) {
            val currentDestinationId = navBottomSheetController.currentDestination?.id
            if (currentDestinationId == MainNavGraph.dest.template) {
                behavior?.state =  BottomSheetBehavior.STATE_HIDDEN
            } else if (currentDestinationId == MainNavGraph.dest.compose) {
                (navBottomSheetFragment.childFragmentManager.fragments.last()
                        as? ComposeFragment)?.onBackPressed()
            } else {
                navBottomSheetController.popBackStack()
            }
            return true
        }
        return false
    }

    fun onDestroy () {
        navBottomSheetController.removeOnDestinationChangedListener(this)
        navBottomSheetFragment.childFragmentManager.removeFragmentOnAttachListener(this)
    }

}