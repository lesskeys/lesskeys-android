# Keyless App for Android

Keyless-App is one of the three components of our multi-client capable nfc based locking system. 
The App is used: 

* as nfc key to get access to a certain lock (main entrance door, flat door)
* to write on factory new NFC-Chips to create new keys. 
* for administration of users and keys
* as radio bell system
* as component to open doors via remote control 
* ... 

## used systems

### Firebase 
Firebase is used as remote message service for notifications in our application. 
https://firebase.google.com/

### Mifare TapLinx
To write on our factory new DESFireEV1 NFC-chips we use the TapLinx SDK developed by MIFARE. 
MIFARE is a manufacturer of near-field comunication chips. Taplinx is an SDK that allows you to communicate with NFC devices on an Android system easily. 
https://www.mifare.net/en/products/tools/taplinx/
