package cn.a10miaomiao.bilimiao.compose

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalView
import androidx.fragment.app.Fragment
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.Navigation
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navArgument
import cn.a10miaomiao.bilimiao.compose.comm.LocalFragment
import cn.a10miaomiao.bilimiao.compose.comm.LocalNavController
import cn.a10miaomiao.bilimiao.compose.comm.mypage.LocalPageConfigInfo
import cn.a10miaomiao.bilimiao.compose.comm.mypage.PageConfigInfo
import cn.a10miaomiao.bilimiao.compose.pages.BlankPage
import cn.a10miaomiao.bilimiao.compose.pages.TestPage
import cn.a10miaomiao.bilimiao.compose.pages.user.UserFollowPage
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.android.subDI
import org.kodein.di.android.x.closestDI
import org.kodein.di.bindSingleton
import org.kodein.di.compose.withDI

class ComposeFragment : Fragment(), MyPage, DIAware {

    override val di: DI = subDI(closestDI()) {
        bindSingleton { this@ComposeFragment }
        bindSingleton { this@ComposeFragment.requireArguments() }
    }

    private val pageConfigInfo = PageConfigInfo(this)

    override val pageConfig = myPageConfig {
        title = pageConfigInfo.title
        menus = pageConfigInfo.menus
    }

    private val url by lazy {
        requireArguments().getString("url", "")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val nav = findNavController()
        return ComposeView(requireContext()).apply {
            setContent {
                CompositionLocalProvider(
                    LocalFragment provides this@ComposeFragment,
                    LocalNavController provides nav,
                    LocalPageConfigInfo provides pageConfigInfo,
                ) {
                    withDI(di = di) {
                        BilimiaoTheme {
                            MyNavHost(url)
                        }
                    }
                }
            }
        }

    }
}

@Composable
fun MyNavHost(startRoute: String) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = PageRoute.start.route,
        builder = PageRoute::builder
    )
    LaunchedEffect(startRoute) {
        navController.navigate(startRoute) {
            popUpTo(0) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }
}