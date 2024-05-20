package com.a10miaomiao.bilimiao.page.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import bilibili.app.card.v1.SmallCoverV5
import bilibili.app.show.v1.EntranceShow
import cn.a10miaomiao.miao.binding.android.view._bottomMargin
import cn.a10miaomiao.miao.binding.android.widget._text
import com.a10miaomiao.bilimiao.comm.BiliNavigation
import com.a10miaomiao.bilimiao.comm._isRefreshing
import com.a10miaomiao.bilimiao.comm._network
import com.a10miaomiao.bilimiao.comm.diViewModel
import com.a10miaomiao.bilimiao.comm.lazyUiDi
import com.a10miaomiao.bilimiao.comm.miaoBindingUi
import com.a10miaomiao.bilimiao.comm.miaoStore
import com.a10miaomiao.bilimiao.comm.navigation.currentOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.pointerOrSelf
import com.a10miaomiao.bilimiao.comm.navigation.stopSameIdAndArgs
import com.a10miaomiao.bilimiao.comm.recycler.GridAutofitLayoutManager
import com.a10miaomiao.bilimiao.comm.recycler.RecyclerViewFragment
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.footerViews
import com.a10miaomiao.bilimiao.comm.recycler.headerViews
import com.a10miaomiao.bilimiao.comm.recycler.lParams
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.views
import com.a10miaomiao.bilimiao.comm.wrapInSwipeRefreshLayout
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.commponents.video.videoItem
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.page.video.VideoInfoFragment
import com.a10miaomiao.bilimiao.page.web.WebFragment
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.recyclerviewAtViewPager2
import com.chad.library.adapter.base.listener.OnItemClickListener
import org.kodein.di.DI
import org.kodein.di.DIAware
import splitties.dimensions.dip
import splitties.views.backgroundColor
import splitties.views.dsl.core.imageView
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView
import splitties.views.gravityCenter
import splitties.views.verticalPadding

class PopularFragment: RecyclerViewFragment(), DIAware {

    companion object {
        fun newFragmentInstance(): PopularFragment {
            val fragment = PopularFragment()
            val bundle = Bundle()
            fragment.arguments = bundle
            return fragment
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<PopularViewModel>(di)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        ui.parentView = container
        return ui.root
    }

    override fun refreshList() {
        if (!viewModel.list.loading) {
            viewModel.refreshList()
        }
    }

    private val handleTopEntranceItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.topEntranceList[position]
        val url = item.uri
        if (url.indexOf("bilibili://") == 0) {
            BiliNavigation.navigationTo(view, url)
        } else {
            val args = WebFragment.createArguments(url)
            findNavController().currentOrSelf()
                .navigate(
                    WebFragment.actionId,
                    args,
                )
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = viewModel.list.data[position]
        val args = VideoInfoFragment.createArguments(item.base!!.param)
        Navigation.findNavController(view).pointerOrSelf()
            .stopSameIdAndArgs(VideoInfoFragment.id, args)
            ?.navigate(VideoInfoFragment.actionId, args)
    }

    val topEntranceItemUi = miaoBindingItemUi<EntranceShow> { item, index ->
        verticalLayout {
            layoutParams = ViewGroup.MarginLayoutParams(
                dip(100), wrapContent
            )
            verticalPadding = config.pagePadding
            gravity = gravityCenter
            setBackgroundResource(config.selectableItemBackground)
            views {
                +imageView {
                    _network(item.icon)
                }..lParams {
                    width = dip(40)
                    height = dip(40)
                    bottomMargin = config.dividerSize
                }
                +textView {
                    _text = item.title
                    gravity = gravityCenter
                }
            }
        }
    }

    val itemUi = miaoBindingItemUi<SmallCoverV5> { item, index ->
        videoItem (
            title = item.base?.title,
            pic =item.base?.cover,
            upperName = item.rightDesc1,
            remark = item.rightDesc2,
            duration = item.coverRightText1,
        )
    }

    val ui = miaoBindingUi {
        val windowStore = miaoStore<WindowStore>(viewLifecycleOwner, di)
        val contentInsets = windowStore.getContentInsets(parentView)

        verticalLayout {
//            _leftPadding = contentInsets.left
//            _rightPadding = contentInsets.right
//            _topPadding = config.pagePadding
//            _bottomPadding = contentInsets.bottom

            views {
                +recyclerviewAtViewPager2 {
                    backgroundColor = config.windowBackgroundColor
                    scrollBarSize = 0
                    mLayoutManager = _miaoLayoutManage(
                        GridAutofitLayoutManager(requireContext(), requireContext().dip(300))
                    )

                    val mAdapter = _miaoAdapter(
                        items = viewModel.list.data,
                        itemUi = itemUi,
                    ) {
                        stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
                        setOnItemClickListener(handleItemClick)
                        loadMoreModule.setOnLoadMoreListener {
                            viewModel.loadMode()
                        }
                    }
                    headerViews(mAdapter) {
                        +recyclerView {
                            layoutManager = LinearLayoutManager(requireContext()).apply {
                                orientation = LinearLayoutManager.HORIZONTAL
                            }
                            scrollBarSize = 0
                            _miaoAdapter(
                                items = viewModel.topEntranceList,
                                itemUi = topEntranceItemUi,
                            ) {
                                setOnItemClickListener(handleTopEntranceItemClick)
                            }
                        }..lParams(matchParent, wrapContent)
                    }
                    footerViews(mAdapter) {
                        +listStateView(
                            when {
                                viewModel.triggered -> ListState.NORMAL
                                viewModel.list.loading -> ListState.LOADING
                                viewModel.list.fail -> ListState.FAIL
                                viewModel.list.finished -> ListState.NOMORE
                                else -> ListState.NORMAL
                            },
                            viewModel::tryAgainLoadData
                        )..lParams(matchParent, wrapContent) {
                            _bottomMargin = contentInsets.bottom
                        }
                    }
                }.wrapInSwipeRefreshLayout {
                    setColorSchemeResources(config.themeColorResource)
                    setOnRefreshListener(handleRefresh)
                    _isRefreshing = viewModel.triggered
                }..lParams(matchParent, matchParent)
            }
        }
    }

}