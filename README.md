<p align="center" >
  <img src="static/kustomer_logo.png" title="Kustomer logo" float=left>
</p>

----------------

<p align="center" >
  The Android SDK for the <a href="https://www.kustomer.com/">Kustomer.com</a> mobile client
</p>

## Requirements

- A [Kustomer.com](https://www.kustomer.com/) API Key
- Android Min SDK Version 19 i.e.., Android 4.4 Kitkat

#### API Key

The Kustomer Android SDK requires a valid API Key with role `org.tracking`. See [Getting Started - Create an API Key](https://dev.kustomer.com/v1/getting-started)


## Installation

#### Gradle

Include the library in your `app.gradle`:

```gradle
compile 'com.kustomer.kustomersdk:kustomersdk:0.1.4'
```

#### Or through Maven

```xml
<dependency>
  <groupId>com.kustomer.kustomersdk</groupId>
  <artifactId>kustomersdk</artifactId>
  <version>0.1.4</version>
  <type>pom</type>
</dependency>
```


## Setup

1. In your project's Application Class:
```java
import com.kustomer.kustomersdk.Kustomer;

private static final String K_KUSTOMER_API_KEY = "YOUR_API_KEY";

@Override
public void onCreate() {
    super.onCreate();
    Kustomer.init(this, K_KUSTOMER_API_KEY);
}
```

2. When you want to present the Kustomer chat interface to your users:

```java
Intent intent = new Intent(activity, KUSSessionsActivity.class);
activity.startActivity(intent);

// OR

Kustomer.showSupport(activity);
```

##### Add permissions to manifest:

3. Internet Permissions (Required):
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

4. Multimedia Permissions (Optional):
```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
```

Note: If neither of the Multimedia Permissions is declared, the image attachments button will be hidden.

##### Add File Provider (Required only for Camera access):

First you need to add a FileProvider `<provider>` tag in `AndroidManifest.xml` under `<application>`
tag like below:

```xml
<application
        ...
    <provider
        android:name="android.support.v4.content.FileProvider"
        android:authorities="${applicationId}"
        android:exported="false"
        android:grantUriPermissions="true">
        <meta-data
            android:name="android.support.FILE_PROVIDER_PATHS"
            android:resource="@xml/file_paths"/>
    </provider>
</application>
```

Add `res/xml/file_paths.xml` file in project and replace `com.kustomer.kustomer` with your package name.

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-path name="my_images"
        path="Android/data/com.kustomer.kustomer/files/Pictures" />
</paths>
```

##### Declaring Activities

5. Add `KUSSessionsActivity` & `KUSChatActivity` into your `AndroidManifest.xml`
```xml
<activity android:name="com.kustomer.kustomersdk.Activities.KUSSessionsActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/KUSAppTheme" />

<activity android:name="com.kustomer.kustomersdk.Activities.KUSChatActivity"
            android:configChanges="orientation|screenSize|keyboardHidden"
            android:theme="@style/KUSAppTheme" />
```

### Additional API Reference

```java
// Initialize the Kustomer android SDK with an API key, and start a user session.
Kustomer.init(this, "API_KEY");
```

```java
// Convenience method that will present the chat interface.
Kustomer.showSupport(ACTIVITY);

// Convenience methods that will present a browser interface pointing to your KnowledgeBase.
Kustomer.presentKnowledgeBase(ACTIVITY);

// Convenience method that will present a custom web page interface
Kustomer.presentCustomWebPage(ACTIVITY,"https://www.example.com");
```

```java
// Resets the user session, clearing the user's access to any existing chats from the device.
Kustomer.resetTracking();
```

```java
// Securely identify a customer. Requires a valid JSON Web Token.
Kustomer.identify("SECURE_ID_HASH");

/*
 Identifying users is the best way to ensure your users have a great chat experience because
 it gives them access to all of the previous conversations across devices.
 By default, users can see their conversation history only on a single device. By including a secure
 hash with the ID of your user, you can securely identify that user and grant them access.

 JSON Web Token:
 The JWT used for secure identification must use HMAC SHA256 and include the following header and claims:
 Header: { "alg" : "HS256", "typ" : "JWT" }
 Claims: { "externalId" : "your_user_id", "iat" : "current_time_utc" }
 NOTE: tokens with an @"iat" older than 15 minutes will be rejected

 The JWT must be signed with your organization's secret. This secret is accessible to your server,
 via `/v1/auth/customer/settings`. The intent is that your own server fetches the secret, generates
 and signs the JWT and then sends it to your client which in turn calls the `+[Kustomer identify:]`
 method, preventing any risk of falsified indentification calls.
*/
```

```java
/*
 Attach custom attributes to the user

 NOTE:
 Attached key-value pairs via the `custom` property must be enabled on the Customer Klass via the admin portal.
 This can be done by an admin via Settings > Platform Settings > Klasses > Customer
*/

KUSCustomerDescription customerDescription = new KUSCustomerDescription();
customerDescription.setEmail("address@example.com");

JSONObject customObject = new JSONObject();
customObject.put("customAttributeStr", "value");
// ...

customerDescription.setCustom(customObject);
Kustomer.describeCustomer(customerDescription);

/*
 Attach custom attributes to the user's most recent conversation (or the first one they create)

 NOTE:
 These key-value pairs must be enabled on the Conversation Klass via the admin portal.
 This can be done by an admin via Settings > Platform Settings > Klasses > Conversation
*/

JSONObject conversationObject = new JSONObject();
conversationObject.put("customAttributeStr", "value");
// ...

Kustomer.describeConversation(conversationObject);
```

```java
/*
 Mark the user as having navigated to a new page. By marking the user's progress around the app, you will be able to create proactive conversational campaigns that can be triggered as a result of the user's progress in your application flow.
*/
 @Override
    protected void onCreate(Bundle savedInstanceState) {

    // Track the current page on appearance
    Kustomer.setCurrentPageName("Home");
 }

 /*
 You should also declare the following receiver in manifest file to allow proactive notifications to be dismissed.
 */


 <receiver
     android:name="com.kustomer.kustomersdk.Receivers.NotificationDismissReceiver"
     android:enabled="true"
     android:exported="true"/>

```

```java
/*
Check chat management status asynchronously to enable support chat.
*/
Kustomer.isChatAvailable(new KUSChatAvailableListener(){
    @Override
    public void onSuccess(boolean enabled){
        // This is called when API call is successful.
        // enabled represent chat management settings.

    }
    
    @Override
    public void onFailure(){
        // This is called when API call fails.
    }
});

```

### Styling

##### Theme Customization:

For theme customization, you should always extend theme for support activities from `KUSAppTheme` and pass it to activities in the manifest. Only include items that you want to customize.

```xml
<style name="MySupportTheme" parent="KUSAppTheme">
  <item name="colorPrimary">@color/kusToolbarBackgroundColor</item>
  <item name="colorPrimaryDark">@color/kusStatusBarColor</item>
  <item name="colorAccent">@color/kusColorAccent</item>
  <item name="kus_back_image">@drawable/ic_arrow_back_black_24dp</item>
  <item name="kus_dismiss_image">@drawable/ic_close_black_24dp</item>
  <item name="kus_new_session_button_image">@drawable/ic_edit_white_20dp</item>
</style>
```

##### Font Customization:

Import the font files in `res/font` & create a file i.e **montserrat.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<font-family xmlns:android="http://schemas.android.com/apk/res/android"
             xmlns:app="http://schemas.android.com/apk/res-auto">

  <font android:fontStyle="normal" 
        android:fontWeight="400" 
        android:font="@font/montserrat_alternates_black"
        app:fontStyle="normal" 
        app:fontWeight="400" 
        app:font="@font/montserrat_regular"/>

  <font android:fontStyle="italic" 
        android:fontWeight="400" 
        android:font="@font/montserrat_medium"
        app:fontStyle="italic" 
        app:fontWeight="400" 
        app:font="@font/montserrat_regular" />

</font-family>
```

**For more detail:** Please have a look at [Fonts In XML](https://developer.android.com/guide/topics/ui/look-and-feel/fonts-in-xml.html)

You can define any of the following in your `res/values/style` to customize font.
```xml
<style name="KUSChatMessageTimeStampTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSChatMessageTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSChatSessionTitleTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSChatSessionSubtitleTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSChatSessionDateTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSChatSessionUnreadTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSToolbarNameTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSToolbarGreetingTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSToolbarUnreadTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSNewSessionButtonAppearance" parent="@style/TextAppearance.AppCompat.Button">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSEmailInputPromptTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSEmailInputTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>

<style name="KUSInputBarTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
<style name="KUSOptionPickerTextAppearance" parent="@style/TextAppearance.AppCompat">
    <item name="android:fontFamily">@font/montserrat</item>
</style>
```

#### Additional Customization
You can define any of the following items in their respective `res` files to change them.

##### Sessions Screen:

```xml
<color name="kusToolbarBackgroundColor">#000000</color>
<color name="kusStatusBarColor">#000000</color>
<color name="kusToolbarTintColor">#DD2C00</color>
<color name="kusSessionListBackground">#909090</color>
<color name="kusSessionItemBackground">#909090</color>
<color name="kusSessionItemSelectedBackground">#55FFFFFF</color>
<color name="kusSessionTitleColor">#FFFFFF</color>
<color name="kusSessionDateColor">#FFFFFF</color>
<color name="kusSessionSubtitleColor">#FFFFFF</color>
<color name="kusSessionUnreadColor">#FFFFFF</color>
<color name="kusSessionUnreadBackground">#3F51B5</color>
<color name="kusSessionPlaceholderBackground">#909090</color>
<color name="kusSessionPlaceholderLineColor">#55FFFFFF</color>
<color name="kusNewSessionButtonColor">#DD2C00</color>
<color name="kusNewSessionTextColor">#FFFFFF</color>

<string name="com_kustomer_new_conversation">New Conversation</string>

<bool name="kusNewSessionButtonHasShadow">true</bool>
```

<p align="center" >
  Before and after:
  <br><br>
  <img src="static/before_sessions.png">&nbsp&nbsp&nbsp<img src="static/after_sessions.png">
</p>


##### Chat screen:

```xml
<color name="kusToolbarNameColor">#FFFFFF</color>
<color name="kusToolbarGreetingColor">#FFFFFF</color>
<color name="kusToolbarSeparatorColor">#BDBDBD</color>
<color name="kusToolbarUnreadTextColor">#FFFFFF</color>
<color name="kusToolbarUnreadBackground">#aacc0000</color>
<color name="kusEmailInputBackground">#FFFFFF</color>
<color name="kusEmailInputBorderColor">#DD2C00</color>
<color name="kusEmailInputPromptColor">#FFFFFF</color>
<color name="kusChatListBackground">#909090</color>
<color name="kusChatItemBackground">#909090</color>
<color name="kusChatTimestampTextColor">#FFFFFF</color>
<color name="kusCompanyBubbleColor">#000000</color>
<color name="kusCompanyTextColor">#FFFFFF</color>
<color name="kusUserBubbleColor">#DD2C00</color>
<color name="kusUserTextColor">#000000</color>
<color name="kusSendButtonColor">#DD2C00</color>
<color name="kusInputBarTintColor">#DD2C00</color>
<color name="kusInputBarHintColor">#EEEEEE</color>
<color name="kusInputBarTextColor">#FFFFFF</color>
<color name="kusInputBarBackground">#000000</color>
<color name="kusInputBarSeparatorColor">#BDBDBD</color>
<color name="kusInputBarAttachmentIconColor">#FFFFFF</color>
<color name="kusOptionPickerSeparatorColor">#BDBDBD</color>
<color name="kusOptionPickerButtonBorderColor">#2962FF</color>
<color name="kusOptionPickerButtonTextColor">#2962FF</color>
<color name="kusOptionPickerButtonBackground">#F5F5F5</color>

<string name="com_kustomer_type_a_message">Type a message...</string>
<string name="com_kustomer_dont_miss_a_response_get_notified_soon_by_email">Don\'t miss a response! Get notified by email:</string>
<string name="com_kustomer_email_example">example@domain.com</string>

<integer name="kusMaximumAvatarsToDisplay">3</integer>
```
<p align="center" >
  Before and after:
  <br><br>
  <img src="static/before_chat.png">&nbsp&nbsp&nbsp<img src="static/after_chat.png">
</p>

### Localization
Kustomer SDK support both <b>Right-to-left (RTL)</b> and <b>Left-to-right (LTR)</b> formatted languages. English and Urdu language translation is already added in SDK.

```xml
// List of all strings that you can override
 <string name="com_kustomer_week">week</string>
     <string name="com_kustomer_day">day</string>
     <string name="com_kustomer_hour">hour</string>
     <string name="com_kustomer_minute">minute</string>
     <string name="com_kustomer_weeks">weeks</string>
     <string name="com_kustomer_days">days</string>
     <string name="com_kustomer_hours">hours</string>
     <string name="com_kustomer_minutes">minutes</string>
     <string name="com_kustomer_just_now">Just now</string>
     <string name="com_kustomer_ago">ago</string>
 
     <string name="com_kustomer_attachment">Attachment</string>
     <string name="com_kustomer_camera">Camera</string>
     <string name="com_kustomer_gallery">Gallery</string>
     <string name="com_kustomer_cancel">Cancel</string>
 
     <string name="com_kustomer_something_went_wrong_please_try_again">Something went wrong. Please try again.</string>
     <string name="com_kustomer_try_again">Try Again</string>
     <string name="com_kustomer_new_conversation">New Conversation</string>
     <string name="com_kustomer_dont_miss_a_response_get_notified_soon_by_email">Don\'t miss a response! Get notified by email:</string>
     <string name="com_kustomer_email_example">example@domain.com</string>
     <string name="com_kustomer_type_a_message">Type a message...</string>
     <string name="com_kustomer_Loading...">Loading...</string>
 
     <string name="com_kustomer_chat_with">Chat with</string>
     <string name="com_kustomer_processing">Processing…</string>
     <string name="com_kustomer_no_internet_connection">No Internet Connection</string>
     <string name="com_kustomer_chat_screen">Chat Screen</string>
     <string name="com_kustomer_we_re_here_let_us_know_if_we_can_help">We\'re here, Let us know if we can help.</string>
     <string name="com_kustomer_example">Example</string>
     <string name="com_kustomer_please_provide_a_message_body">Please provide a message body</string>
     <string name="com_kustomer_no_internet_message">The Internet connection appears to be offline.</string>
     <string name="com_kustomer_camera_permission_denied">Camera permission is denied.</string>
     <string name="com_kustomer_storage_permission_denied">Storage permission is denied.</string>
     <string name="com_kustomer_authorities">authority</string>
     <string name="com_kustomer_share_via">Share Via</string>
     <string name="com_kustomer_please_provide_storage_permission">Please provide storage permission to this app</string>
     <string name="com_kustomer_dismiss">Dismiss</string>
     <string name="com_kustomer_closed">CLOSED</string>
     <string name="com_kustomer_start_a_new_conversation">Start a New Conversation</string>
     <string name="com_kustomer_thank_you_we_will_follow_up_on_your_request">Thank You! We\'ll follow up on your request.</string>
```

#### Customize existing strings
If you are interested in adding translation for SDK specified language but with different
translation then you just need to add the strings file for the respective language in your app
and write any of the strings defined above and change the translation.

#### Add new localization
If the SDK does not include localized strings for the language you are interested in, you can add new ones.
You just need to add the strings file for the respective language in your app
and write all of the strings defined above and add the translation for that language.

#### Custom locale
By default, SDK use mobile preferred locale. If you want to use different locale, you can override locale as well.
To do that:
```java
// Set Custom Locale
Kustomer.setLocale(new Locale("language_code"));

// Example
Kustomer.setLocale(new Locale("ur"));
```
You must set locale before calling `Kustomer.init()` method. SDK will load only the language whose translation exists either in SDK or in project. If specified language's translation doesn't exist, SDK will try to load translation of mobile preferred languages before using default language.

After setting SDK locale, SDK automatically updates the layout (LTR or RTL) and will update the text for that language as well.

### Development

#### Incrementing the build version

- Update the version number references in the above **Installation** section
- Update the `libraryVersion` in the SDK's `build.gradle` file (/kustomersdk/build.gradle)
- Update the [changelog](CHANGELOG.md) if necessary
- Run the following command on the root of the project. `./gradlew install bintrayUpload`. Make sure
you have the correct `bintray.user` & `bintray.apikey` values in `local.properties` file.
- Commit & push the changes