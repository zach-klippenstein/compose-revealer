package com.zachklipp.revealer

import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.runtime.CompositionLocalProvider
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.google.accompanist.coil.LocalImageLoader

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val imageLoader = ImageLoader.Builder(this)
      .componentRegistry {
        if (SDK_INT >= 28) {
          add(ImageDecoderDecoder())
        } else {
          add(GifDecoder())
        }
      }
      .build()

    setContent {
      MaterialTheme(colors = darkColors()) {
        CompositionLocalProvider(LocalImageLoader provides imageLoader) {
          App()
        }
      }
    }
  }
}
