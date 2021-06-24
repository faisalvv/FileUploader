# FileUploader

Handle Android webview file chooser click actions on all Android versions. I want to share with you my solution on how to control a fileChooser HTML input type on Android’s WebView. Let’s start telling that from Android 5.0 to upper, there are a public method defined onShowFileChooser but there is no default methods for lower Android versions. The code bellow handle click events on file chooser button and suggest file choose from android memory, take a photo from cam or select an image from memory. Here’s the full code:
