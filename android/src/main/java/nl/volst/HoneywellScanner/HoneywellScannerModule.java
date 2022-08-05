package nl.volst.HoneywellScanner;

import java.util.HashMap;
import java.util.Map;

import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.honeywell.aidc.UnsupportedPropertyException;

import static nl.volst.HoneywellScanner.HoneywellScannerPackage.TAG;

import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.AidcManager.CreatedCallback;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.ScannerNotClaimedException;

@SuppressWarnings("unused")
public class HoneywellScannerModule extends ReactContextBaseJavaModule implements BarcodeReader.BarcodeListener {

    // Debugging
    private static final boolean D = true;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private BarcodeReader reader;
    private ReactApplicationContext mReactContext;

    private static final String BARCODE_READ_SUCCESS = "barcodeReadSuccess";
    private static final String BARCODE_READ_FAIL = "barcodeReadFail";

    public HoneywellScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);

        mReactContext = reactContext;
    }

    @Override
    public String getName() {
        return "HoneywellScanner";
    }

    /**
     * Send event to javascript
     * @param eventName Name of the event
     * @param params Additional params
     */
    private void sendEvent(String eventName, @Nullable WritableMap params) {
        if (mReactContext.hasActiveCatalystInstance()) {
            if (D) Log.d(TAG, "Sending event: " + eventName);
            mReactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
        }
    }

    public void onBarcodeEvent(BarcodeReadEvent barcodeReadEvent) {
        if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan read");
        WritableMap params = Arguments.createMap();
        params.putString("data", barcodeReadEvent.getBarcodeData());
        sendEvent(BARCODE_READ_SUCCESS, params);
    }

    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan failed");
        sendEvent(BARCODE_READ_FAIL, null);
    }

    /*******************************/
    /** Methods Available from JS **/
    /*******************************/

    @ReactMethod
    public void startReader(final Promise promise) {
        AidcManager.create(mReactContext, new CreatedCallback() {
            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                reader = manager.createBarcodeReader();
                if(reader != null){
                    reader.addBarcodeListener(HoneywellScannerModule.this);
                    try {
                        reader.claim();
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_8_ENABLED, true);
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_8_CHECK_DIGIT_TRANSMIT_ENABLED, true);
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_13_ENABLED, true);
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_13_CHECK_DIGIT_TRANSMIT_ENABLED, true);
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_13_TWO_CHAR_ADDENDA_ENABLED, true);
                        reader.setProperty(BarcodeReader.PROPERTY_EAN_13_FIVE_CHAR_ADDENDA_ENABLED, true);
                        promise.resolve(true);
                    } catch (ScannerUnavailableException | UnsupportedPropertyException e) {
                        promise.resolve(false);
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @ReactMethod
    public void stopReader(Promise promise) {
        if (reader != null) {
            reader.close();
        }
        if (manager != null) {
            manager.close();
        }
        promise.resolve(null);
    }

    @ReactMethod
    public void softwareTriggerStart() {
        if (reader != null) {
            try {
                reader.softwareTrigger(true);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();                
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();                
            }
        }
    }

    @ReactMethod
    public void softwareTriggerStop() {
        if (reader != null) {
            try {
                reader.softwareTrigger(false);
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();                
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();                
            }
        }
    }

    private boolean isCompatible() {
        // This... is not optimal. Need to find a better way to performantly check whether device has a Honeywell scanner 
        return Build.BRAND.toLowerCase().contains("honeywell");
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("BARCODE_READ_SUCCESS", BARCODE_READ_SUCCESS);
        constants.put("BARCODE_READ_FAIL", BARCODE_READ_FAIL);
        constants.put("isCompatible", isCompatible());
        return constants;
    }

}
