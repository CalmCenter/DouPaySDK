package me.doupay.sdklib

import me.doupay.sdklib.bean.ContractCoinVo
import me.doupay.sdklib.net.BaseVoObserver
import me.doupay.sdklib.net.ServerApi
import java.util.*

object Payment {
    fun test(
        appid: String,
        key: String) {
        val map = HashMap<String, Any>()
        map["appId"] = appid
        map["key"] = key
        ServerApi.SERVICE_API
            .test(Constants.basrUrl + "asc1/test6", map)
            .subscribe(object : BaseVoObserver<ContractCoinVo>() {
                override fun onPlaintextSuccess(data: ContractCoinVo?) {

                }
            })


    }

    fun test2(
        appid: String,
        timeStamp: String,
        key: String) {
        val map = HashMap<String, Any>()
        map["appId"] = appid
        map["timeStamp"] = timeStamp
        map["key"] = key
        ServerApi.SERVICE_API
            .test(Constants.basrUrl + "asc1/test6", map)
            .subscribe(object : BaseVoObserver<ContractCoinVo>() {
                override fun onPlaintextSuccess(data: ContractCoinVo?) {

                }
            })


    }
}