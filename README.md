# Alexa-Voice-Service

It is implementation of alexa voice service in android app. The app also includes the ability to set an alarm or a timer. 

## Compile and run this application

   1. Follow the process for creating a connected device detailed at this Amazon link  https://developer.amazon.com/appsandservices/solutions/alexa/alexa-voice-service/getting-started-with-the-alexa-voice-service.
   2. Also see this github link for better understanding https://github.com/alexa/alexa-avs-sample-app. Don't forget to enable your
   security profile for Login with Amazon!
   3. Add your api_key.txt file (part of the Amazon process) to the app/src/main/assets folder
   4. Enter the PRODUCT_ID in the MainActivity that you configured for "Application Type Id" above.
   5. Build and run the sample app using Gradle from the command line or Android Studio!
   
   After installing the app in your android mobile. Press the Login button of the first screen it will take you to your web browser
   to login in your account through your security profile and to get access token for communicating with Alexa. After that you will
   be directed to second screen where you can ask your questions to Alexa. Just keep your finger down on the button and ask your 
   question and release it when you are finished. You can set an alarm by asking "Set an alarm" and wait for the response as it is 
   a continuous session. Now don't press the button as Alexa will ask you the time for the alarm. After setting the alarm you can  
   continue as before.
   
   See below screeshots
   
   ![1](https://user-images.githubusercontent.com/24502136/28382555-251d4fbe-6cdc-11e7-8187-f65c7c265bb3.jpg)
   
   ![2](https://user-images.githubusercontent.com/24502136/28382707-b510074c-6cdc-11e7-9643-d65785ab5c00.jpg)
   
   ![3](https://user-images.githubusercontent.com/24502136/28382706-b50bfa76-6cdc-11e7-851c-25833ba06586.jpg)
   
   ![4](https://user-images.githubusercontent.com/24502136/28382708-b5351d34-6cdc-11e7-8df4-80f3ea95087f.jpg)
   
   You can delete the alarm from the list by long pressing the desired alarm.
   
   Note* - Bugs are still being fixed.
