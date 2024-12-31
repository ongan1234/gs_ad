# GS_AD

## Installation
#### repositories
Vào mục setting.gradle.kts. Copy:
```gradle
maven { url 'https://jitpack.io' }
```
Như hình:
![Install](https://github.com/user-attachments/assets/24efb505-9b88-4c28-a9b1-aa80196f44d3)


#### dependencies
Cài thư viện
```gradle
implementation("com.github.ongan1234:gs_ad:1.0.1")
```
Thêm các thư viện
```gradle
implementation ("com.google.android.gms:play-services-ads:23.5.0")
implementation ("com.google.android.ump:user-messaging-platform:3.1.0")

implementation ("androidx.multidex:multidex:2.0.1")
implementation ("com.github.eriffanani:ContentLoader:1.2.0")
implementation ("com.facebook.shimmer:shimmer:0.5.0@aar")
```

## AndroidManifest
Copy các thẻ meta vào trong application
```manifest
<uses-permission android: name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android: name="android.permission.INTERNET" />
![Install](https://github.com/user-attachments/assets/64bcb3d5-1688-4b9b-9f37-d86571058d40)

<meta-data
     android:name="com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION"
     android:value="true" />
<meta-data
     android:name="com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING"
     android:value="true" />
<meta-data
     android:name="com.google.android.gms.ads.AD_MANAGER_APP"
     android:value="true" />
<meta-data
     android:name="com.google.android.gms.ads.APPLICATION_ID"
     android:value="@string/app_ad_id" />
<meta-data
     android:name="com.google.android.gms.ads.flag.NATIVE_AD_DEBUGGER_ENABLED"
     android:value="false" />
```
Tạo 1 AppOwner.kt như hình:


## Kotlin
```kotlin
```


### Licence
```license
Copyright 2022 Mukhammad Erif Fanani

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
