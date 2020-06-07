<p align="center">
  <img src="https://github.com/iammert/TabScrollAttacher/blob/master/art/artgif.gif">
</p>

## What? 🤔
It is not a custom `TabLayout` or `RecyclerView`. It is just a helper library to attach to your `RecyclerView` with your `TabLayout`. If you don't want to go with sticky header `RecyclerView` or something like that, and also want to provide a good experience when your user is scrolling the content, this small library is for you.

## How? 🤨

You fetch your product list and their categories. All you need to do is calcualate start index for all categories. Attacher will do the rest.

If your backend guy returns this json,
```json
CategoryA -> 10 items (between 0..10)
CategoryB -> 20 items (between 10..30)
CategoryC -> 30 items (between 30..60)
```

Then your offset list will be,

```kotlin
//3 tabs and their offset indexes in total list.
val categoryIndexOffsetList = [0,10,30]
```

## Then? 🙄

Then you attach.
```kotlin
val attacher = TabScrollAttacher(tabLayout, recyclerView, categoryIndexOffsetList)
attacher.attach()
```

Detach when you don't need anymore.
```kotlin
attacher.detach()
```

## Configure? 🧑‍🔧

* `scrollDirectly()`: If you don't want to see the scrolling process, use it.
* `scrollSmoothly()`: If you want to see the flowing items while scrolling, this is for you.
* `scrollSmoothly(limit = 10)`: If you have a lot of items and smooth scrolling takes too long, you can set a scrolling item limit. It will scroll directly until the `limit` items left, then rest of the items will scroll smoothly.
```kotlin
TabScrollAttacher(tabLayout, recyclerView, categoryIndexOffsetList) {
    // Configuration
}
```

## Where? 🤩

```gradle
maven { url 'https://jitpack.io' }
```

```gradle
dependencies {
  compile 'com.github.iammert:TabScrollAttacher:1.0.2'
}
```

## When? 😇

When you have `LinearLayoutManager`, `GridLayoutManager` in any orientation (horizontal or vertical) you can use this library.

<p align="center">
  <img src="https://raw.githubusercontent.com/iammert/TabScrollAttacher/master/art/1.png">
</p>

<p align="center">
  <img src="https://raw.githubusercontent.com/iammert/TabScrollAttacher/master/art/2.png">
</p>

## Who? 👻

License
--------


    Copyright 2019 Mert Şimşek

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


