# Remotrix
Receive SMS in Matrix chatroom

## What does this app do?
Whenever you receive SMS on the phone that has this app installed, this app will automatically forward them via Matrix using one of the account registered in this app. This app uses the Space feature in Matrix to automatically create multiple rooms, each dedicated to a sender, whilst keeping them in one place to not clutter the list of rooms you are in.

## What is space feature?
Space feature is a fairly recent Matrix feature that allows you to create a special form of room where you can add rooms under it. This app will create a room for each SMS sender, under a single Messaging space.
![image](https://github.com/MangoCubes/remotrix/assets/10383115/73f21724-37d5-4360-90c6-4e360b926673)


## Features 
 - [x] Receive messages upon arrival
 - [x] SMS forwarding rooms grouped up using Space feature
 - [x] Maintenance room, for when you want to update your settings without getting to your phone, or need to test things out real quick
 - [ ] Multiple senders (or none, in case you want to filter out spams), chosen based on the SMS sender and content regex
 - [ ] Send messages back

## Screenshot
Main screen
![Screenshot_20230615-200448](https://github.com/MangoCubes/remotrix/assets/10383115/ec7e4424-2520-4368-b3d0-01ecddce800e)
Account management screen
![Screenshot_20230615-200148](https://github.com/MangoCubes/remotrix/assets/10383115/67171edc-c8b4-4b8f-9c38-297ad5d15d31)
Settings
![Screenshot_20230615-200154](https://github.com/MangoCubes/remotrix/assets/10383115/a0854bc0-6a5f-4a74-9646-97da94a7ccfa)
Log view (Log can be disabled in the settings)
![Screenshot_20230615-200407](https://github.com/MangoCubes/remotrix/assets/10383115/b31cf783-b6a3-405f-9983-9240d124f19c)
