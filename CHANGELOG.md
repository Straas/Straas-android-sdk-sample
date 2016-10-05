Change Log
==========
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
