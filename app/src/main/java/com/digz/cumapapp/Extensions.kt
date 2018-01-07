package com.digz.cumapapp

import android.location.Address

fun Address.getTextualAddress():String{
    val maxAddressLineIndex = this.maxAddressLineIndex
    val builder = StringBuilder()
    for (i in 0..maxAddressLineIndex){
        builder.append(this.getAddressLine(i))
        if (i!=maxAddressLineIndex)builder.append("/n")
    }
    return builder.toString()
}