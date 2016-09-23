[![Download](https://api.bintray.com/packages/straas-io/maven/straas-base/images/download.svg) ](https://bintray.com/bintray/jcenter?filterByPkgName=straas)

# android-sdk-sample
Samples & documentation for the StraaS Android SDK.

Developer Authentication
-----
Read [Credential](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/SDK-Credential) first.

To run our sample, you have to copy `client_id`s into [`gradle.properties` at project root](https://github.com/StraaS/StraaS-android-sdk-sample/blob/master/gradle.properties#L8):
```
your_debug_client_id=xxxxx
your_release_client_id=xxxxx
```

User Identity
-----
Read [Identity](https://github.com/StraaS/StraaS-android-sdk-sample/wiki/User-Identity) first.
To change the identity in out sample, change the Identity declared in `MemberIdentity.ME`.

Usage
-----
To add these dependencies on jCenter using Gradle.

X.X.X is the your preferred version. For the version information, see 
[CHANGELOG](https://github.com/StraaS/StraaS-android-sdk-sample/blob/master/CHANGELOG.md)

- Media browser & playback + Ad integration:
```
compile 'io.straas.android.sdk:straas-extension-ima:X.X.X'
```

- Media browser & playback only:
```
compile 'io.straas.android.sdk:straas-media-core:X.X.X'
```

- ChatRoom
```
compile 'io.straas.android.sdk:straas-messaging:X.X.X'
```

- ChatRoom with UI
```
compile 'io.straas.android.sdk:straas-messaging-ui:X.X.X'
```

Learn about Android SDK
------------------
- [SDK Explained](https://github.com/StraaS/android-sdk-sample/wiki)
