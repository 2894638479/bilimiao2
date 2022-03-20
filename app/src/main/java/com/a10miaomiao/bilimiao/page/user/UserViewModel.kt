package com.a10miaomiao.bilimiao.page.user

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import com.a10miaomiao.bilimiao.Bilimiao
import com.a10miaomiao.bilimiao.MainNavGraph
import com.a10miaomiao.bilimiao.R
import com.a10miaomiao.bilimiao.comm.MiaoBindingUi
import com.a10miaomiao.bilimiao.comm.apis.UserApi
import com.a10miaomiao.bilimiao.comm.entity.ResultInfo
import com.a10miaomiao.bilimiao.comm.entity.ResultListInfo
import com.a10miaomiao.bilimiao.comm.entity.user.SpaceInfo
import com.a10miaomiao.bilimiao.comm.entity.user.UpperChannelInfo
import com.a10miaomiao.bilimiao.comm.network.MiaoHttp.Companion.gson
import com.a10miaomiao.bilimiao.store.FilterStore
import com.a10miaomiao.bilimiao.store.UserStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance
import splitties.toast.toast

class UserViewModel(
    override val di: DI,
) : ViewModel(), DIAware {

    val activity: AppCompatActivity by instance()
    val ui: MiaoBindingUi by instance()
    val fragment: Fragment by instance()
    val userStore: UserStore by instance()
    val filterStore: FilterStore by instance()

    val id by lazy { fragment.requireArguments().getString(MainNavGraph.args.id, "") }

    var loading = false
    var dataInfo: SpaceInfo? = null
    var channelList = listOf<UpperChannelInfo>()

    var noLike = false // 不喜欢，是否屏蔽

    val isSelf get() = userStore.isSelf(id)

    val isFiltered get() = !filterStore.filterUpper(id)

    init {
        loadData()
    }

    fun loadData() = viewModelScope.launch(Dispatchers.IO) {
        try {
            ui.setState {
                loading = true
            }
            val res = UserApi().space(id).awaitCall().gson<ResultInfo<SpaceInfo>>()
            if (res.code == 0) {
                ui.setState {
                    dataInfo = res.data
                }
            } else {
                withContext(Dispatchers.Main) {
                    activity.toast(res.message)
                }
            }
            val res2 = UserApi().upperChanne(id).awaitCall().gson<ResultListInfo<UpperChannelInfo>>()
            if (res.code == 0) {
                ui.setState {
                    channelList = res2.data
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                activity.toast("网络错误")
            }
            e.printStackTrace()
        } finally {
            ui.setState {
                loading = false
            }
        }
    }

    fun filterUpperDelete () {
        filterStore.deleteUpper(id.toLong())
    }

    fun filterUpperAdd () {
        val info = dataInfo
        if (info == null) {
            activity.toast("请等待信息加载完成")
        } else {
            filterStore.addUpper(
                info.card.mid.toLong(),
                info.card.name,
            )
        }
    }

    fun logout() {
        AlertDialog.Builder(activity).apply {
            setTitle("确定退出登录，喵？")
            setNegativeButton("确定退出") { dialog, which ->
                userStore.logout()
                val nav = activity.findNavController(R.id.nav_host_fragment)
                nav.popBackStack()
                activity.toast("已退出登录了喵")
            }
            setPositiveButton("取消", null)
        }.show()
    }

}