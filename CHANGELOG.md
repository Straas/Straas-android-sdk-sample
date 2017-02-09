Change Log
==========
## Version 0.5.8
_2017-02-09_

*   straas-base
    *   feat: Upgrade okhttp to [v3.6.0](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-360).
*   straas-messaging
    *   **Important Change**: The RawData received from [onRawDataAdded(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/interfaces/EventListener.html#onRawDataAdded-io.straas.android.sdk.messaging.Message-) now changes to [Message](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/Message.html), you could use [getRawData()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/Message.html#getRawData--) to retrive the RawData.
    *   **Important Change**: String in [ChatroomManager.sendMessage(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#sendMessage-java.lang.String-) should be within 300 characters or task will fail.
    *   feat: New [ChatroomManager.getUsers(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getUsers-io.straas.android.sdk.messaging.user.UserType-).
    *   deprecated: [ChatroomManager.getUsers(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getUsers-int-int-)
	*   deprecated: connect flags within [connect(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#connect-java.lang.String-io.straas.android.sdk.base.identity.Identity-int-) simplify to `boolean isPersonalChat`.
*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.2.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r220).
    
## Version 0.5.7
_2017-01-13_

*   straas-streaming
    *   feat: add live category and highest resolution setter in new [StreamManager.createLiveEvent()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#createLiveEvent-io.straas.android.sdk.streaming.LiveEventConfig-)
    *   deprecated: [StreamManager.startStreaming()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#startStreaming-java.lang.String-java.lang.String-boolean-boolean-)
    *   deprecated: [StreamManager.createLiveEvent()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#createLiveEvent-java.lang.String-java.lang.String-boolean-boolean-)
    *   deprecated: [EventListener.onError()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/interfaces/EventListener.html#onError-io.straas.android.sdk.streaming.error.StreamError-java.lang.String-)
*   straas-messaging
    *   feat: changes for adapting to new server
*   straas-media-core
    *   fix: be able to re-attach the same [video container](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.UiContainer.html#getVideoContainer--) in the middle of playback
*   straas-extension-ima:
    *   fix/feat: enhance the stability and functionality

## Version 0.5.5
_2017-01-05_

*   straas-streaming
    *   fix: [createLiveEvent](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#createLiveEvent-java.lang.String-java.lang.String-boolean-boolean-) before [prepare](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#prepare-io.straas.android.sdk.streaming.StreamConfig-android.view.TextureView-) will fail issue
    *   fix: [prepare](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#prepare-io.straas.android.sdk.streaming.StreamConfig-android.view.TextureView-) after [destroy](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#destroy--) may crash issue
    *   fix: [switchCamera](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/CameraController.html#switchCamera--) fails in certain condition


## Version 0.5.4
_2017-01-03_

Bugfix release only. 
*   straas-streaming
    *   fix: back camera preview upside down issue


## Version 0.5.3
_2017-01-03_

*   Fix proguard issue ([#48](https://github.com/StraaS/StraaS-android-sdk-sample/issues/48))
*   straas-streaming
    *   fix: preview upside down when ROTATION_90
*   straas-messaging
    *   feat: [disconnect()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#disconnect--) now return
     a [Task](https://developers.google.com/android/reference/com/google/android/gms/tasks/Task.html) as well.


## Version 0.5.2
_2016-12-28_

*   straas-streaming
    *   **Important Change**: [Identity](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/base/identity/Identity.html) is only needed in [initialize](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#initialize-io.straas.android.sdk.base.identity.Identity-io.straas.android.sdk.base.interfaces.OnResultListener-) now.
    *   **Important Change**: [stopStreaming](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#stopStreaming--) and [destroy](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#destroy--) won't set current live event to ended now.
    *   **Important Change**: Remove parameter sent by [EventListener.onFinish](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/interfaces/EventListener.html#onFinished--).
    *   **Important Change**: Separate [addEventListener](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#addEventListener-io.straas.android.sdk.streaming.interfaces.EventListener-) and [startStreaming](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#startStreaming-java.lang.String-java.lang.String-boolean-boolean-).
    *   feat: Add [createLiveEvent](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#createLiveEvent-java.lang.String-java.lang.String-boolean-boolean-) and [startStreaming](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#startStreaming-java.lang.String-).
    *   fix: TextureView aspect ratio unexpected issue
    *   deprecated: [EventListener.onFinished](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/interfaces/EventListener.html#onFinished--) and [EventListener.onStreaming](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/interfaces/EventListener.html#onStreaming-java.lang.String-). You could get the result by methods in [StreamManager](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html).

## Version 0.5.1
_2016-12-23_

*   Upgrade support library to v25.0.1
*   straas-messaging-ui
    *   `ChatroomInputView` will auto expand the height when text length grows up. Use
     [setInputMaxLines](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ui/ChatroomInputView.html#setInputMaxLines-int-)
     to change at most many lines tall, default is 4 lines. 
*   straas-media-core   
    *   [issue 209385](https://code.google.com/p/android/issues/detail?id=209385) and [issue 210013](https://code.google.com/p/android/issues/detail?id=210013)
    are fixed, so we don't have to put json string in `onSessionEvent` anymore. 
        *   Use `playbackState.getExtras().getString(StraasMediaCore.EVENT_PLAYER_ERROR_MESSAGE, "")` when state is error.
    *   Now you could set [crop](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLANE_PROJECTION_MODE_CROP)/
    [fit](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLANE_PROJECTION_MODE_FIT)/
    [full](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLANE_PROJECTION_MODE_FULL) 
    using [setPlaneProjectionMode](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#setPlaneProjectionMode-int-). 
    If you are using the [cardboard](https://vr.google.com/cardboard/), remember switch to [DISPLAY_MODE_CARDBOARD](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#DISPLAY_MODE_CARDBOARD)
    using [setDisplayMode](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#setDisplayMode-int-) and enjoy the VR world.
    

## Version 0.5.0

_2016-12-16_

*   Upgrade support library to v25.0.1
*   Upgrade Google Play Service to 10.0.1
*   The age of [Tasks](https://developers.google.com/android/guides/tasks), APIs with OnResultListener injection
are `deprecated` and return a [Task<T>](https://developers.google.com/android/reference/com/google/android/gms/tasks/Task.html) for you.
*   Error enums are `deprecated`, using Exception to get more details about the error reason. 
*   straas-messaging
    *   **feat: data channel and personal chat**
        * enable them via `ChatroomManager.WITH_DATA_CHANNEL` and `ChatroomManager.IS_PERSONAL_CHAT` flags
        * data channel provide aggregated and raw data types of message
            * `chatroomManager.sendAggregatedDataTypeMessage("love")` will receive one or more love being aggregated in 
            `onAggregatedDataAdded(SimpleArrayMap<String, Integer>)`
            * `chatroomManager.sendRawData(RawData)` will receive a single JSON data in `onRawDataAdded(RawData)`. 
            We also provide built-in JSON converter let you use own class model to communicate!  
    *   connect API now return a [Task<T>](https://developers.google.com/android/reference/com/google/android/gms/tasks/Task.html), so you could know the chatroom is connected directly within a code block!
    *   feat: add time filter for getMessages
*   straas-streaming
    *   **Change method `validate` naming to `initialize`**
    *   feat: decide resolution by texture aspect ratio.
    *   feat: add limit resolution
    *   fix: switch camera crash issue
    *   startStreaming/stopStreaming/destroy APIs now return a [Task<T>](https://developers.google.com/android/reference/com/google/android/gms/tasks/Task.html).
*   straas-media-core
    *   feat: upgrade ExoPlayer library to [v2.1.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r210)
    
## Version 0.4.5

_2016-12-02_

*   straas-media-core
    *   fix: conflict of okhttp-ws version
*   straas-streaming
    *   feat: add interface for setting video filter. see [filter](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/Streaming#filter)


## Version 0.4.4

_2016-11-22_

*   **[Please add maven repo for showing sticker panel] (https://github.com/StraaS/StraaS-android-sdk-sample/blob/master/build.gradle#L19)**  
`maven { url "https://raw.github.com/laenger/maven-releases/master/releases" }`
*   straas-extension-ima
    *   feat: upgrade IMA & Google Play Services Ads
*   straas-messaging-ui
    *   feat: support sticker panel
        * FragmentActivity is required to enable this feature
    *   feat: save/restore ChatroomInputView & ChatroomOutputView state
    *   feat: use StreamModelLoader to set Glide HTTP client, independent of GlideModule
    *   fix: duplicate message, remove pagination loading


## Version 0.4.3

_2016-11-04_

*   using cookies to store user information (internally) 
    *   **[Please add jitpack maven repo](https://github.com/StraaS/StraaS-android-sdk-sample/blob/master/build.gradle#L18)** `maven { url "https://jitpack.io" }`
*   using TLSv1.2 on Android API levels 16~20 
    *   [16~20 not enabled TLSv1.2 by default] (https://developer.android.com/reference/javax/net/ssl/SSLSocket.html)
*   straas-streaming
    *   feat: add getStreamState() for getting StreamingManager state
    *   feat: create new event automatically when stream key expires
    *   fix: developer may receive onError when stopStreaming soon after startStreaming
*   straas-messaging-ui
    *   fix: ChatroomView may receive old ChatroomManager event
    *   feat: add flag to control show relative time or absolute time of a message
    *   feat: support fake message/nickname for blocked users
    *   feat: ANCHOR chat mode UI
    *   feat: remove message when receiving onMessageRemoved
    *   fix: TouchListener for ChatroomInputView could receive MotionEvent.ACTION_UP & MotionEvent.ACTION_DOWN
    

## Version 0.4.2

_2016-11-02_

*   straas-messaging-ui
    *   refactor: move input functionality from ChatroomOutputView into ChatroomInputView
    *   feat: feed ChatroomManager into ChatroomInputView
    *   feat: touch listener will be notified when input is available for typing

## Version 0.4.1

_2016-11-01_

*   **feat: add straas-streaming sdk**.
*   straas-messaging
    *   feat: support two and more EventListeners.
*   straas-messaging-ui
    *   feat: divide input/output view for more flexible
    *   feat: adjust the visibility of messages avatar

## Version 0.3.11

_2016-10-28_

*   straas-messaging-ui
    *   feat: support showing sticker messages in chat room


## Version 0.3.10

_2016-10-21_

*   straas-messaging
    *   feat: provide `block/revive users` and `remove message`
*   straas-media-core
    *   feat: upgrade ExoPlayer library to [v2.0.4](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r204)
    *   fix: playlist Ad behavior


## Version 0.3.9

_2016-10-05_

*   straas-messaging
    *   feat: enable auto create flag in `ChatroomManager.connect()`
*   straas-messaging-ui
    *   feat: enable custom style, see [Style](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/Chatroom-UI#style)
*   straas-media-core
    handle non-public item in public playlist


## Version 0.3.7

_2016-10-03_

*   straas-media-core
    *   feat: upgrade ExoPlayer library


## Version 0.3.6

_2016-09-28_

*   feat: upgrade Socket.io library
*   straas-messaging
    *   **Important Change**: replace EventListener method `void userCount(int guestCount, int memberCount)` by `void userCount(int userCount)`.
    *   **Important Change**: replace ChatroomManager method `int getMemberCount()` and `int getGuestCount()` by `int getUserCount()`.
    *   feat: provide immediate user count by `void userCount(int userCount)` in EventListener and `int getUserCount()` in ChatroomManager.


## Version 0.3.5

_2016-09-25_

*   feat: upgrade Socket.io library
*   straas-media-core
    *   fix: 0.3.4 crash issue
    *   fix: proguard rule
    *   fix: handle playlist video with post-roll Ad
*   straas-extension-ima
    *   feat: upgrade Google Play Services Ads
    

## Version 0.3.4

_2016-09-23_

*   straas-media-core
    *   fix: analytics issue related to ExoPlayer v2 migration
*   straas-messaging
    *   fix: add consumer proguard file


## Version 0.3.3

_2016-09-22_

*   straas-media-core
    *   feat: upgrade to ExoPlayer v2
*   straas-extension-ima
    *   feat: upgrade Google Play Services Ads


## Version 0.3.2

_2016-09-21_

*   straas-messaging-ui
    *   fix: crash in API 20 and older version
    *   feat: support vector drawables


## Version 0.3.1

_2016-09-14_

*   **Important Change**: process `client_id` string at gradle, more convenient to switch between build-types/flavors.
*   **Important Change**: guest now represent as `Identity.GUEST` instead of `new Identity("")`.
*   feat: update Android Support Library to 24.2.1
*   release **straas-media-core**, **straas-extension-ima**


## Version 0.2.2

_2016-09-12_

*   straas-base
    *   feat: add dontwarn in consumerProguardFiles
*   straas-messaging
    *   fix: retry when connect fail
*   straas-messaging-ui
    *   feat: add interface for guest setting nickname 
    *   feat: distinguish logging user and guest at message list
    *   fix: bugs that happened when sending a message
