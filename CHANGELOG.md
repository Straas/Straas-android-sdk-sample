Change Log
==========
*   If you want to upgrade StraaS Android SDK, please check all the **Important Change** below from your current version.

## Version 0.8.0
_2017-09-15_

*   **Retire deprecated methods and constants**
*   **Target Android API 26.**
    *   upgrade support library to [v26.0.2](https://developer.android.com/topic/libraries/support-library/revisions.html#26-0-2).
*   feat: Upgrade okhttp to [v3.9.0](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-390).
*   Upgrade Google Play Service to [11.2.2](https://developers.google.com/android/guides/releases#august_2017_-_version_1120).
*   Upgrade Glide to [4.1.1](https://github.com/bumptech/glide/releases/tag/v4.1.1).
*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.5.2](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r252).
*   straas-streaming
    *   **feat: Screencast APIs. See [Wiki](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/Screencast-Streaming) for more detail.**
    *   feat: Decide the original bitrate by resolution.


## Version 0.7.15
_2017-09-12_

Bugfix release only.
*   fix: Allow more aggressive switching for HLS


## Version 0.7.14
_2017-07-20_

*   straas-messaging
    *   feat: Metadata API. See [setMetadata(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#setMetadata-java.lang.String-java.lang.Object-boolean-),  [getMetadata(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getMetadata-java.lang.String...-), and [onMetadataUpdated(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/interfaces/EventListener.html#onMetadataUpdated-android.support.v4.util.SimpleArrayMap-).
*   straas-media-core
    *   feat: Improve the scalability and deliver efficiency for [low latency](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLAY_OPTION_LIVE_LOW_LATENCY_FIRST).


## Version 0.7.12
_2017-06-23_

*   feat: Upgrade okhttp to [v3.8.1](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-381).
*   straas-messaging
    *   feat: You can [pin a message](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#pinMessage-io.straas.android.sdk.messaging.Message-) to the chatroom now.
*   straas-messaging-ui
    *   feat: Pinned message ui, see [setPinnedMessageCustomView()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ui/ChatroomOutputView.html#setPinnedMessageCustomView-android.view.View-) and [attr since v0.7.12](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/Messaging-UI#style) for further usage.


## Version 0.7.11
_2017-06-15_

*   straas-messaging
    *   feat: Remove the message count limit of [getArchivedMessages](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ArchivedMessagesManager.html#getArchivedMessages-java.lang.String-java.lang.String-long-long-) and both of `startTime` and `endTime` couldn't be 0 now.
*   straas-media-core
    *   feat: disable/re-enable audio through [COMMAND_DISABLE_AUDIO](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#COMMAND_DISABLE_AUDIO). Please read the document about how we control `audio focus` with this command.<br>
If you just need to adjust the volume, please use [setVolumeTo(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.html#setVolumeTo(int%2C%20int)) and [adjustVolume(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.html#adjustVolume(int%2C%20int)) provided by Android Support Library.
    *   feat: Improve [low latency](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLAY_OPTION_LIVE_LOW_LATENCY_FIRST) experience.
    *   feat: Upgrade ExoPlayer library to [v2.4.2](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r242).


## Version 0.7.10
_2017-05-26_

*   straas-messaging
    *   feat: Add [ArchivedMessagesManager](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ArchivedMessagesManager.html).
*   straas-messaging-ui
    *   feat: Add [setMessageItemCustomView](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ui/ChatroomOutputView.html#setMessageItemCustomView-int-), [setMsgDividerColor](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ui/ChatroomOutputView.html#setMsgDividerColor-int-), and [setVerticalScrollBarEnabled](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ui/ChatroomOutputView.html#setVerticalScrollBarEnabled-boolean-) for more more custom situations.
*   straas-extension-ima
    *   Upgrade IMA to 3.7.2
*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.4.1](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r241).


## Version 0.7.9
_2017-05-24_

Bugfix release only.
*   fix: Add proguard rules for okhttp v3.8.0 [(#3355)](https://github.com/square/okhttp/issues/3355)
*   straas-messaging-ui
    *   fix: Vector drawables incorrect setting.
    *   fix: Incorrect author attributes with icon and the color of name at random.


## Version 0.7.7
_2017-05-18_

*   feat: Upgrade okhttp to [v3.8.0](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-380).
*   straas-media-core
    *   fix: Unregister [Callback](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.Callback.html) when [MediaBrowser](https://developer.android.com/reference/android/support/v4/media/MediaBrowserCompat.html) disconnected.
*   straas-streaming
    *   feat: Adjust the max bitrate according to streaming resolution.
    *   fix: Handle SurfaceTexture transform matrix.


## Version 0.7.6
_2017-05-04_

*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.4.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r240).
We only include `exoplayer-core` & `exoplayer-hls` modules in our SDK, which are all the needs for [playFromMediaId(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#prepareFromMediaId(java.lang.String%2C%20android.os.Bundle)) to play all your contents served by StraaS. To play other media types (e.g. DASH, SmoothStreaming) outside of StraaS via [playFromUri(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#prepareFromUri(android.net.Uri%2C%20android.os.Bundle)), or UI components and resources provided by ExoPlayer, please include `exoplayer-dash`, `exoplayer-smoothstreaming`, `exoplayer-ui` in your dependencies manually.
*   straas-messaging-ui
    *   fix: The attribute `msgAuthorColor` works now.
    *   fix: Join the chatroom that doesn't have any history messages will crash.


## Version 0.7.5
_2017-04-20_

*   feat: Upgrade okhttp to [v3.7.0](https://github.com/square/okhttp/blob/master/CHANGELOG.md#version-370).
*   straas-media-core
    *  feat: Support player runs in foreground, see the [introduction](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/Interact-with-Video#foreground-service).
    *  feat: (BETA) Significantly reduce the latency between broadcaster by adding the key [PLAY_OPTION_LIVE_LOW_LATENCY_FIRST](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#PLAY_OPTION_LIVE_LOW_LATENCY_FIRST) with `true` value in the Bundle of [playFromMediaId(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#playFromMediaId(java.lang.String%2C%20android.os.Bundle)).
*   straas-messaging-ui
    *   change: New message hint wording is changed and appears only when there are new messages now.


## Version 0.7.4
_2017-04-06_

*   straas-media-core
    *  fix: Workaround for [Defect-36811209](https://code.google.com/p/android/issues/detail?id=269491), internal MediaControllerCompat.Callback will clear outer class reference when session destroyed.
    *  fix: Prevent [get video formats](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#COMMAND_GET_VIDEO_FORMATS) NPE.


## Version 0.7.3
_2017-03-30_

*   Upgrade support library to [v25.3.1](https://developer.android.com/topic/libraries/support-library/revisions.html#25-3-1).
*   straas-messaging
    *   feat: Simplify reconnection flow.
*   straas-messaging-ui
    *   feat: Add button for scrolling to new message.
    *   feat: Moderators highlight.


## Version 0.7.2
_2017-03-27_

*   straas-extension-ima
    *   Upgrade IMA to 3.7.1
*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.3.1](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r231).
    *   feat: Enhance live streaming playback experience.
    *   fix: Crash when MediaBrowserCompat disconnected if no UiContainer injected.
    *   fix: Memory issue when using multiple UiContainers between Fragments.


## Version 0.7.1
_2017-03-23_

*   Upgrade Google Play Service to 10.2.1
*   straas-streaming
    *   **Important Change**: Start from this version, the video stream of front camera will be flipped horizontally from preview, you could use [Builder.frontCameraFlipHorizontally](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamConfig.Builder.html#frontCameraFlipHorizontally-boolean-) to reverse it.
    *   feat: Add [StreamOccupiedException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/error/StreamException.StreamOccupiedException.html).
    *   feat: Enhance the error message in [ServerException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/error/StreamException.ServerException.html).
    *   deprecated: Constructor of [StreamConfig](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamConfig.html#StreamConfig--), use [StreamConfig.Builder](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamConfig.Builder.html) instead.
*   straas-media-core
    *   feat: Upgrade ExoPlayer library to [v2.3.0](https://github.com/google/ExoPlayer/blob/release-v2/RELEASENOTES.md#r230).
    *   feat: MediaControllerCompat could receive a live event state change ([ready](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_EVENT_STATE_READY)/[started](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_EVENT_STATE_STARTED)/[ended](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_EVENT_STATE_ENDED)) with [LIVE_EXTRA_EVENT_STATE](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_EVENT_STATE) key.
    *   fix: Memory leak issue
    *   fix: Handle SurfaceTexture transform matrix


## Version 0.7.0
_2017-03-15_

*   **Important Change**: Starting from this version, you have to bind the [`client id`](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/SDK-Credential#get-client-id) with an [application package name](https://developer.android.com/studio/build/application-id.html), please set it with your exsiting [Certificate](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/SDK-Credential#generate-key-hashes) and [`client id`](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/SDK-Credential#get-client-id) in the same page.
*   Upgrade support library to [v25.3.0](https://developer.android.com/topic/libraries/support-library/revisions.html#25-3-0).
*   straas-extension-ima
    *   Upgrade IMA to 3.7.0
*   straas-messaging
    *   change: [getCurrentUser](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getCurrentUser--) will return cache if chatroom state is not connected.
    *   fix: Returns [ChatroomNameNotFoundException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/MessagingException.ChatroomNameNotFoundException.html) when calling [connect](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#connect-java.lang.String-io.straas.android.sdk.base.identity.Identity-boolean-) to a non-existent chatroom.
    *   fix: Crash when create User info from Parcel.
*   straas-messaging-ui
    *   fix: Crash when send message after disconnection.
    *   fix: Crash may happen when click sticker button.
*   straas-streaming
    *   feat: Improve adaptive bitrate in bad bandwidth condition on [API 19](https://developer.android.com/reference/android/os/Build.VERSION_CODES.html#KITKAT) and higher.
*   straas-media-core
    *   fix: [setIdentity(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#setIdentity-io.straas.android.sdk.base.identity.Identity-) fail.



## Version 0.6.2
_2017-03-02_

*   Upgrade support library to v25.2.0.
*   straas-messaging
    *   feat: Add [updateUserRole](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#updateUserRole-io.straas.android.sdk.messaging.User-io.straas.android.sdk.messaging.Role-) and [updateUserRoleByMemberId](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#updateUserRoleByMemberId-java.lang.String-io.straas.android.sdk.messaging.Role-) for upgrading a user to moderator.
    *   feat: Add [UserNotFoundException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/MessagingException.UserNotFoundException.html) and [NotFoundException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/MessagingException.NotFoundException.html).
*   straas-streaming
    *   fix: Broken texture in some devices.
    *   fix: Trying to start a owner-undefined live event will crash.
    *   fix: Prepare will crash if camera is broken. Now, you could catch the error and try another camera.
    *   change: Throw [UnavailableException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/error/StreamException.UnavailableException.html) when the "available" flag of the live event is set to false.
*   straas-media-core
    *   deprecated: [CUSTOM_METADATA_VIEWS_COUNT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#CUSTOM_METADATA_VIEWS_COUNT), use [PLAY_COUNT_SUM](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#PLAY_COUNT_SUM) instead.
    *   deprecated: [CUSTOM_METADATA_VIEWERS_COUNT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#CUSTOM_METADATA_VIEWERS_COUNT).
    *   Add [BASE_PLAY_COUNT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#BASE_PLAY_COUNT),
    [PLAY_DURATION_SUM](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#PLAY_DURATION_SUM),
    [BASE_PLAY_DURATION](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#BASE_PLAY_DURATION),
    [HIT_COUNT_SUM](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#HIT_COUNT_SUM),
    [BASE_HIT_COUNT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#BASE_HIT_COUNT),
    [UPDATED_AT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/VideoCustomMetadata.html#UPDATED_AT).


## Version 0.6.1
_2017-02-20_

*   Upgrade Google Play Service to 10.2.0
*   straas-extension-ima
    *   Upgrade IMA to 3.6.0
*   straas-streaming
    *   feat: Add [vodListed(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/LiveEventConfig.Builder.html#vodListed-boolean-) and [vodAvailable(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/LiveEventConfig.Builder.html#vodAvailable-boolean-).
*   straas-messaging
    *   Adjust minimum Android API level to 14 (Android 4.0.1, Ice Cream Sandwich).
    *   deprecated: `sendAggregatedDataTypeMessage` is renamed to [sendAggregatedData(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#sendAggregatedData-java.lang.String-)
    *   Add data channel history, useful for replaying.
        *  [getRawData(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getRawData-io.straas.android.sdk.messaging.message.MessageRequest-)
        *  [getAggregatedData(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getAggregatedData-io.straas.android.sdk.messaging.message.MessageRequest-)
    *   [getTotalAggregatedData()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/ChatroomManager.html#getTotalAggregatedData--) could be used to query all aggregated data. If you use it in [onAggregatedDataAdded(...)](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/interfaces/EventListener.html#onAggregatedDataAdded-android.support.v4.util.SimpleArrayMap-), you will get all aggregated data in real-time.
*   straas-messaging-ui
    *   Adjust minimum Android API level to 14 (Android 4.0.1, Ice Cream Sandwich).
*   straas-media-core
    *   Live event could know whether it was ended or not by [checking](https://developer.android.com/reference/android/support/v4/media/MediaBrowserCompat.html#getItem(java.lang.String%2C%20android.support.v4.media.MediaBrowserCompat.ItemCallback)) if [isPlayable()](https://developer.android.com/reference/android/support/v4/media/MediaBrowserCompat.MediaItem.html#isPlayable()). An ended event will be [browsable](https://developer.android.com/reference/android/support/v4/media/MediaBrowserCompat.MediaItem.html#isBrowsable()), then you could [query all VODs](https://developer.android.com/reference/android/support/v4/media/MediaBrowserCompat.html#subscribe(java.lang.String%2C%20android.support.v4.media.MediaBrowserCompat.SubscriptionCallback)).
    *   Implement [prepareFromMediaId(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#prepareFromMediaId(java.lang.String%2C%20android.os.Bundle)),
    once the preparation is done, the session will change its playback state to [STATE_PAUSED](https://developer.android.com/reference/android/support/v4/media/session/PlaybackStateCompat.html#STATE_PAUSED),
    which is the same effect as [playFromMediaId(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#playFromMediaId(java.lang.String%2C%20android.os.Bundle)) then [pause()](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#pause()) immediately.
    *   Live event send two statistics value: [LIVE_EXTRA_STATISTICS_CCU](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_STATISTICS_CCU) and [LIVE_EXTRA_STATISTICS_HIT_COUNT](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/media/StraasMediaCore.html#LIVE_EXTRA_STATISTICS_HIT_COUNT) represent the _concurrent users_ and the _media hit count_ of the live event respectively.


## Version 0.6.0
_2017-02-16_

*   **Important Change**: [client id](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/SDK-Credential#get-client-id) now declares through `straas_client_id` key-value pair with [manifestPlaceholders](http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.ProductFlavor.html#com.android.build.gradle.internal.dsl.ProductFlavor:manifestPlaceholders) property instead of [resValue](http://google.github.io/android-gradle-dsl/current/com.android.build.gradle.internal.dsl.ProductFlavor.html#com.android.build.gradle.internal.dsl.ProductFlavor:resValue(java.lang.String%2C%20java.lang.String%2C%20java.lang.String)):
```
manifestPlaceholders = [straas_client_id: "$your_client_id"]
```
*   Retire all deprecated APIs in `0.5.x`.
*   Upgrade support library to v25.1.1.
*   straas-messaging
    *   feat: Add [order parameter](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/messaging/message/MessageRequest.Builder.html#order-java.lang.String-) for determining the order of messages request.
*   straas-streaming
    *   fix: Clear variable reference of preview after [destroy](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#destroy--).
    *   fix: [Prepare](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#prepare-io.straas.android.sdk.streaming.StreamConfig-android.view.TextureView-) fails in some devices.
* straas-media-core
    *   feat: Add [RTMP](https://en.wikipedia.org/wiki/Real-Time_Messaging_Protocol) playback functionality, you could feel the power by filling a RTMP link in [playFromUri(...)](https://developer.android.com/reference/android/support/v4/media/session/MediaControllerCompat.TransportControls.html#playFromUri(android.net.Uri%2C%20android.os.Bundle)).
    *   fix: Incorrect metadata value with `CUSTOM_METADATA_VIEWS_COUNT` and `CUSTOM_METADATA_VIEWERS_COUNT`.


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
    *   feat: add live category and highest resolution setter in new [StreamManager.createLiveEvent()](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/StreamManager.html#createLiveEvent-io.straas.android.sdk.streaming.LiveEventConfig-).
    reuseLiveEvent flag doesn't exist in this method any more, you will always receive [LiveCountLimitException](https://straas.github.io/StraaS-android-sdk-sample/io/straas/android/sdk/streaming/error/StreamException.LiveCountLimitException.html) if there is a not-ended live event.
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
