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

object MAPBOX{
    val KEY = "pk.eyJ1IjoidG9ieWRpZ3oiLCJhIjoiY2l3MTExM3JqMDA0YTJ5cTkxcXJiNzQ3NSJ9.OL1zvr8Hb6HbKPq01CEwsg"
}