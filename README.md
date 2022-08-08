# React Native Honeywell Scanner With Soft Trigger

> **This package is build from [fork](https://github.com/Volst/react-native-honeywell-scanner). This package would be extended in future from [This fork](https://github.com/AMI3GOLtd/react-native-honeywell-scanner) which might work better.**

This package works with Honeywell devices that have an integrated barcode scanner, like the Honeywell Dolphin CT40. This package was fully tested with a CT40, since the SDK is not specific to the CT40 other devices will likely work as well but this is not guaranteed.

**Tip**: Use [react-native-camera](https://github.com/react-native-community/react-native-camera) as fallback for devices that don't have an integrated scanner; it has an integrated barcode scanner by using the camera.

## Installation

```
yarn add react-native-honeywell-scanner-trigger
```

To install the native dependencies:

```
react-native link react-native-honeywell-scanner-trigger
```

## Usage

First you'll want to check whether the device is a Honeywell scanner:

```js
import HoneywellScanner from 'react-native-honeywell-scanner-trigger';

HoneywellScanner.isCompatible // true or false
```

The barcode reader needs to be "claimed" by your application; meanwhile no other application can use it. You can do that like this:

```js
useEffect(() => {
    if (isCompatible) {
      HoneywellScanner.startReader().then(claimed => {
        console.log(
          deviceClaimed
            ? 'Barcode reader is claimed'
            : 'Barcode reader is busy',
        );
        HoneywellScanner.onBarcodeReadSuccess(event => {
          console.log('Received data', event.propogated);
        });

        HoneywellScanner.onBarcodeReadFail(event => {
          console.log('Barcode read failed');
        });
      });

      return () => {
        HoneywellScanner.stopReader().then(() => {
          console.log('Stop Reader!!');
          HoneywellScanner.offBarcodeReadSuccess();
          HoneywellScanner.offBarcodeReadFail();
        });
      };
    }
  }, [isCompatible]);
```

To free the claim and stop the reader, also freeing up resources:

```js
HoneywellScanner.stopReader().then(() => {
    console.log('Freedom!');
});
```

To get events from the barcode scanner:

```js
HoneywellScanner.onBarcodeReadSuccess(event => {
    console.log('Received data', event);
});

HoneywellScanner.onBarcodeReadFail(event => {
    console.log('Barcode read failed');
});
```

To stop receiving events:

```js
HoneywellScanner.offBarcodeReadSuccess();
HoneywellScanner.offBarcodeReadFail();
```


## Inspiration

The [react-native-bluetooth-serial](https://github.com/rusel1989/react-native-bluetooth-serial) project was used as inspiration. [cordova-honeywell](https://github.com/icsfl/cordova-honeywell) also served as some inspiration.

## Sample React Native APP illustrating the above soft trigger capability

Clone the [Project](https://github.com/subramanianr/react-native-app-illustrator-with-scan-trigger-app). Follow the README to build and generate APK. Happy Testing!
