package com.worldclock.app_themes.ads.preload

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.worldclock.app_themes.ads.utils.Utils

object PreloadController {


    fun heightForBanner(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._50sdp)

    fun heightForNoMediaNative(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._80sdp)

    fun heightForMediaNative(context: Context): Int =
        context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._220sdp)

    fun marginForNativeAds(layout: RelativeLayout, context: Context) {
        val margin = context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp)
        (layout.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            setMargins(margin, margin, margin, margin)
            layout.layoutParams = this
        }
    }

    fun loadAdInBannerPosition(
        flag: Int, position: String,
        context: Context, adIDBanner: String, adIDNative: String
    ) {
        BannerPreload.adBannerBottomLiveData.value = false
        BannerPreload.adBannerTopLiveData.value = false

        NativePreload.adNativeBottomLiveData.value = false
        NativePreload.adNativeTopLiveData.value = false
        NativePreload.adNativeNormalLiveData.value = false

        if (Utils.isPremium){
            return
        }


        if (position == "top") {
            when (flag) {
                1 -> {

                    Log.d("PRLO","load called")

                    //banner
                    BannerPreload.preLoadBannerAdTop(context, adIDBanner)
                }

                2 -> {
                    //collapsible top

                }

                3 -> {
                    //collapsible bottom

                }

                4 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                5 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                6 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                7 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                8 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                9 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }
                10 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }
                11 -> {
                    NativePreload.loadTopNative(context,adIDNative)
                }

                else -> {


                }
            }
        }
        else if (position == "bottom") {
            when (flag) {
                1 -> {
                    //banner
                    Log.d("BANNER_CALLED","called")
                    BannerPreload.preLoadBannerAdBottom(context, adIDBanner)
                }

                2 -> {
                    //collapsible top

                }

                3 -> {
                    //collapsible bottom

                }

                4 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                5 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                6 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                7 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                8 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                9 -> {
                    NativePreload.loadBottomNative(context, adIDNative)
                }

                else -> {


                }
            }
        }
    }

    fun loadAdInNativePosition(flag: Int, context: Context, adIDNative: String) {

        BannerPreload.adBannerBottomLiveData.value = false
        BannerPreload.adBannerTopLiveData.value = false

        NativePreload.adNativeBottomLiveData.value = false
        NativePreload.adNativeTopLiveData.value = false
        NativePreload.adNativeNormalLiveData.value = false


        if (Utils.isPremium){
            return
        }

        when(flag){
            1 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            2 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            3 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            4 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            5 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }

            6 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            7 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            8 -> {
                NativePreload.loadNormalNative(context,adIDNative)
            }
            else -> {


            }
        }
    }

    fun observeBanner(
        context: Context,
        liveData: MutableLiveData<Boolean>,
        adParent: RelativeLayout,
        adLayout: FrameLayout,
        textView: TextView,
        flag: Int,
        adPosition: String,
        window: Window,
        liveDataNative: MutableLiveData<Boolean>,
        callback: () -> Unit
    )
    {


        if (Utils.isPremium){
            adParent.visibility = View.GONE
            return
        }


        if (adPosition == "top") {
            when (flag) {
                1 -> {
                    //banner
                    val owner = context as? LifecycleOwner ?: return
                    liveData.observe(owner) {
                        if (it == true) {
                            textView.visibility = View.GONE
                            BannerPreload.showPreloadedBannerTop(
                                adLayout,
                                adParent,
                                textView,
                                context
                            )
                        }
                    }
                }

                2 -> {
                    //collapsible top
                    callback.invoke()
                }

                3 -> {
                    //collapsible bottom
                    callback.invoke()
                }

                4 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,1,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )

                }

                5 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,2,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                6 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,3,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                7 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,4,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                8 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,5,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                9 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                10 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                11 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdTop,textView, liveDataNative, adPosition
                    )
                }

                else -> {
                    adParent.visibility = View.GONE
                }
            }
        }
        else if (adPosition == "bottom") {
            when (flag) {
                1 -> {
                    //banner
                    val owner = context as? LifecycleOwner ?: return
                    liveData.observe(owner) {
                        if (it == true) {
                            textView.visibility = View.GONE
                            BannerPreload.showPreloadedBannerBottom(
                                adLayout,
                                adParent,
                                textView,
                                context
                            )
                        }
                    }
                }

                2 -> {
                    //collapsible top
                    callback.invoke()
                }

                3 -> {
                    //collapsible bottom
                    callback.invoke()
                }

                4 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,1,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )

                }

                5 -> {
                    adLayout.minimumHeight =
                        heightForMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,2,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                6 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,3,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                7 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,4,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                8 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,5,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                9 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                10 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                11 -> {
                    adLayout.minimumHeight =
                        heightForNoMediaNative(
                            context
                        )
                    marginForNativeAds(
                        adParent,
                        context
                    )
                    NativePreload.showNativeAdForPreload(
                        adParent, adLayout, context as AppCompatActivity, window,6,
                        NativePreload.nativeAdBottom,textView, liveDataNative,adPosition
                    )
                }

                else -> {
                    adParent.visibility = View.GONE
                }
            }
        }


    }

    fun observeNative(
        context: Context,
        adParent: RelativeLayout,
        adLayout: FrameLayout,
        textView: TextView,
        flag: Int,
        window: Window,
        liveDataNative: MutableLiveData<Boolean>
    )
    {


        if (Utils.isPremium){
            adParent.visibility = View.GONE
            return
        }


        when (flag) {
            1 -> {
                adLayout.minimumHeight =
                    heightForMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,1,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )

            }

            2 -> {
                adLayout.minimumHeight =
                    heightForMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,2,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            3 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,3,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            4 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,4,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            5 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,5,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            6 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            7 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            8 -> {
                adLayout.minimumHeight =
                    heightForNoMediaNative(
                        context
                    )
                marginForNativeAds(
                    adParent,
                    context
                )
                NativePreload.showNativeAdForPreload(
                    adParent, adLayout, context as AppCompatActivity, window,6,
                    NativePreload.nativeAdTop,textView, liveDataNative, "adPosition"
                )
            }

            else -> {
                adParent.visibility = View.GONE
            }
        }

    }



}