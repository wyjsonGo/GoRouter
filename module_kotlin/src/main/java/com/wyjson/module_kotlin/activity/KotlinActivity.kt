package com.wyjson.module_kotlin.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.wyjson.module_kotlin.databinding.KotlinActivityKotlinBinding
import com.wyjson.router.annotation.Param
import com.wyjson.router.annotation.Route

@Route(path = "/kotlin/activity", remark = "这是一个kotlin页面，本库支持kapt")
class KotlinActivity : FragmentActivity() {

    @JvmField
    @Param
    var age: Int = 18;

    @JvmField
    @Param(name = "nickname", remark = "昵称", required = true)
    var name: String? = null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vb = KotlinActivityKotlinBinding.inflate(layoutInflater)
        setContentView(vb.root)

//        `KotlinActivity$$Param`.inject(this)
        vb.tvTitle.text = "age:${age},name:${name}"
    }
}