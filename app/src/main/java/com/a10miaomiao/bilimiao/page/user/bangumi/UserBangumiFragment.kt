package com.a10miaomiao.bilimiao.page.user.bangumi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavType
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.bilimiao.compose.pages.bangumi.BangumiDetailPage
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.comm.NavHosts
import com.a10miaomiao.bilimiao.comm._isRefreshing
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.navigation.FragmentNavigatorBuilder
import com.a10miaomiao.bilimiao.comm.navigation.MainNavArgs
import com.a10miaomiao.bilimiao.comm.navigation.navigateToCompose
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.wrapInSwipeRefreshLayout
import com.a10miaomiao.bilimiao.commponents.bangumi.bangumiItem
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class UserBangumiFragment : Fragment(), DIAware, MyPage {

    companion object : FragmentNavigatorBuilder() {
        override val name = "user.bangumi"
        override fun FragmentNavigatorDestinationBuilder.init() {
            argument(MainNavArgs.id) {
                type = NavType.StringType
                nullable = false
            }
            argument(MainNavArgs.name) {
                type = NavType.StringType
                nullable = false
            }
        }
        fun createArguments(
            id: String,
            name: String
        ): Bundle {
            return bundleOf(
                MainNavArgs.id to id,
                MainNavArgs.name to name,
            )
        }
    }

    override val pageConfig = myPageConfig {
        title = "Ta的追番"
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<UserBangumiViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        NavHosts.pointerNavController
            .navigateToCompose(BangumiDetailPage()) {
                id set item.param
            }
    }

    val itemUi = miaoBindingItemUi<UserBangumiViewModel.ItemInfo> { item, index ->
        bangumiItem (
            title = item.title,
            cover = item.cover,
            statusText = if (item.is_started == 1) {
                if (item.finish == 1){
                    "已完结"
                }else{
                    "已更新到${item.newest_ep_index}话"
                }
            } else {
                "即将开播"
            },
        )
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            backgroundColor = config.windowBackgroundColor
            _miaoLayoutManage(
                GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
            )

            val headerView = frameLayout {
                _topPadding = contentInsets.top
            }
            val footerView = listStateView(
                when {
                    viewModel.triggered -> ListState.NORMAL
                    viewModel.list.loading -> ListState.LOADING
                    viewModel.list.fail -> ListState.FAIL
                    viewModel.list.finished -> ListState.NOMORE
                    else -> ListState.NORMAL
                }
            ) {
                _bottomPadding = contentInsets.bottom
            }
            footerView.layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)

            _miaoAdapter(
                items = viewModel.list.data,
                itemUi = itemUi,
            ) {
                stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                setOnItemClickListener(handleItemClick)
                loadMoreModule.setOnLoadMoreListener {
                    viewModel.loadMode()
                }
                addHeaderView(headerView)
                addFooterView(footerView)
            }
        }.wrapInSwipeRefreshLayout {
            setColorSchemeResources(config.themeColorResource)
            setOnRefreshListener(handleRefresh)
            _isRefreshing = viewModel.triggered
        }
    }

}