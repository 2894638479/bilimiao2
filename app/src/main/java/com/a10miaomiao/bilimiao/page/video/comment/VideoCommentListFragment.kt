package com.a10miaomiao.bilimiao.page.video.comment

import android.os.Bundle
import android.os.Debug
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.coroutineScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import cn.a10miaomiao.miao.binding.android.view._bottomPadding
import cn.a10miaomiao.miao.binding.android.view._leftPadding
import cn.a10miaomiao.miao.binding.android.view._rightPadding
import cn.a10miaomiao.miao.binding.android.view._topPadding
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.*
import com.a10miaomiao.bilimiao.comm.entity.video.VideoCommentReplyInfo
import com.a10miaomiao.bilimiao.comm.mypage.MyPage
import com.a10miaomiao.bilimiao.comm.mypage.myMenuItem
import com.a10miaomiao.bilimiao.comm.mypage.myPageConfig
import com.a10miaomiao.bilimiao.comm.mypage.MenuItemPropInfo
import com.a10miaomiao.bilimiao.comm.recycler._miaoAdapter
import com.a10miaomiao.bilimiao.comm.recycler._miaoLayoutManage
import com.a10miaomiao.bilimiao.comm.recycler.miaoBindingItemUi
import com.a10miaomiao.bilimiao.comm.utils.BiliUrlMatcher
import com.a10miaomiao.bilimiao.comm.utils.DebugMiao
import com.a10miaomiao.bilimiao.comm.utils.NumberUtil
import com.a10miaomiao.bilimiao.commponents.comment.videoCommentView
import com.a10miaomiao.bilimiao.commponents.loading.ListState
import com.a10miaomiao.bilimiao.commponents.loading.listStateView
import com.a10miaomiao.bilimiao.config.config
import com.a10miaomiao.bilimiao.store.WindowStore
import com.a10miaomiao.bilimiao.widget.expandabletext.ExpandableTextView
import com.a10miaomiao.bilimiao.widget.expandabletext.app.LinkType
import com.chad.library.adapter.base.listener.OnItemClickListener
import kotlinx.coroutines.launch
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast
import splitties.views.backgroundColor
import splitties.views.dsl.core.frameLayout
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.wrapContent
import splitties.views.dsl.recyclerview.recyclerView

class VideoCommentListFragment : Fragment(), DIAware, MyPage {

    override val pageConfig = myPageConfig {
        title = "评论列表"
        menus = listOf(
            myMenuItem {
                key = 0
                iconResource = R.drawable.ic_baseline_filter_list_grey_24
                title = SortOrderPopupMenu.getText(viewModel.sortOrder)
            }
        )
    }

    override fun onMenuItemClick(view: View, menuItem: MenuItemPropInfo) {
        super.onMenuItemClick(view, menuItem)
        when (menuItem.key) {
            0 -> {
                val pm = SortOrderPopupMenu(
                    activity = requireActivity(),
                    anchor = view,
                    checkedValue = viewModel.sortOrder
                )
                pm.setOnMenuItemClickListener(handleMenuItemClickListener)
                pm.show()
            }
        }
    }

    override val di: DI by lazyUiDi(ui = { ui })

    private val viewModel by diViewModel<VideoCommentListViewModel>(di)

    private val windowStore by instance<WindowStore>()

    private val handleMenuItemClickListener = PopupMenu.OnMenuItemClickListener {
        it.isChecked = true
        viewModel.sortOrder = it.itemId
        pageConfig.notifyConfigChanged()
        viewModel.refreshList()
        false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ui.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        lifecycle.coroutineScope.launch {
            windowStore.connectUi(ui)
        }
    }

    private fun toSelfLink (view: View, url: String) {
        val urlInfo = BiliUrlMatcher.findIDByUrl(url)
        val urlType = urlInfo[0]
        var urlId = urlInfo[1]
        if (urlType == "BV") {
            urlId = "BV$urlId"
        }
        val args = bundleOf(
            MainNavGraph.args.id to urlId
        )
        when(urlType){
            "AV", "BV" -> {
                args.putString(MainNavGraph.args.type, urlType)
                Navigation.findNavController(view)
                    .navigate(MainNavGraph.action.videoCommentList_to_videoInfo, args)
            }
        }
    }

    private val handleUserClick = View.OnClickListener {
        val id = it.tag
        if (id != null) {
            val args = bundleOf(
                MainNavGraph.args.id to id
            )
            Navigation.findNavController(it)
                .navigate(MainNavGraph.action.videoCommentList_to_user, args)
        }
    }

    private val handleRefresh = SwipeRefreshLayout.OnRefreshListener {
        viewModel.refreshList()
    }

    private val handleItemClick = OnItemClickListener { adapter, view, position ->
        val item = adapter.getItem(position) as VideoCommentReplyInfo
        val reply = VideoCommentDetailArg(
            oid = item.oid,
            rpid = item.rpid,
            rpid_str = item.rpid_str,
            mid = item.member.mid,
            uname = item.member.uname,
            avatar = item.member.avatar,
            ctime = item.ctime,
            floor = item.floor,
            location = item.reply_control.location ?: "",
            content = item.content,
            like = item.like,
            count = item.count,
        )
        val args = bundleOf(
            MainNavGraph.args.reply to reply
        )
        DebugMiao.log(reply)

        Navigation.findNavController(view)
            .navigate(MainNavGraph.action.videoCommentList_to_videoCommentDetail, args)
    }

    private val handleLinkClickListener = ExpandableTextView.OnLinkClickListener { view, linkType, content, selfContent -> //根据类型去判断
        when (linkType) {
            LinkType.LINK_TYPE -> {
                val url = content
                val re = BiliNavigation.navigationTo(view, url)
                if (!re) {
                    if (url.indexOf("bilibili://") == 0) {
                        toast("不支持打开的链接：$url")
                    } else {
                        BiliUrlMatcher.toUrlLink(view, url)
                    }
                }
            }
            LinkType.MENTION_TYPE -> {
//                toast("你点击了@用户 内容是：$content")
            }
            LinkType.SELF -> {
                toSelfLink(view, selfContent)
            }
        }
    }

    val itemUi = miaoBindingItemUi<VideoCommentReplyInfo> { item, index ->
        DebugMiao.log(
            item.mid,
            item.member.uname,
            item.member.avatar,
            NumberUtil.converCTime(item.ctime),
             item.reply_control,
            item.floor,
//            item.content,
             item.like,
             item.count,
        )
        videoCommentView(
            mid = item.mid,
            uname = item.member.uname,
            avatar = item.member.avatar,
            time = NumberUtil.converCTime(item.ctime),
            location = item.reply_control.location ?: "",
            floor = item.floor,
            content =item.content,
            like = item.like,
            count = item.count,
            onUpperClick = handleUserClick,
            onLinkClick = handleLinkClickListener,
        ).apply {
            layoutParams = ViewGroup.LayoutParams(matchParent, wrapContent)
        }
    }

    val ui = miaoBindingUi {
        val contentInsets = windowStore.getContentInsets(parentView)

        recyclerView {
            _leftPadding = contentInsets.left
            _rightPadding = contentInsets.right

            backgroundColor = config.windowBackgroundColor
            _miaoLayoutManage(
                LinearLayoutManager(requireContext())
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