package sr.scanner.HoneywellScanner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.os.Build;
import javax.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.honeywell.aidc.UnsupportedPropertyException;

import static sr.scanner.HoneywellScanner.HoneywellScannerPackage.TAG;

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
    private static final Map<String, String> symbologyCodeMapper = createMap();

    private static Map<String, String> createMap() {
        Map<String, String> result = new HashMap<>();
        result.put(".", "DOTCODE");
        result.put("1", "CODE1");
        result.put(";", "MERGED_COUPON");
        result.put("<", "CODE39_BASE32, CODE32, ITALIAN PHARMACODE, PARAF, LABELCODE_V");
        result.put(">", "LABELCODE_IV");
        result.put("=", "TRIOPTIC");
        result.put("?", "KOREA_POST");
        result.put(",", "INFOMAIL");
        result.put("`", "EAN13_ISBN");
        result.put("[", "SWEEDISH_POST");
        result.put("|", "RM_MAILMARK");
        result.put("]", "BRAZIL_POST");
        result.put("A", "AUS_POST");
        result.put("B", "BRITISH_POST");
        result.put("C", "CANADIAN_POST");
        result.put("D", "EAN8");
        result.put("E", "UPCE");
        result.put("G", "BC412");
        result.put("H", "HAN_XIN_CODE");
        result.put("I", "GS1_128");
        result.put("J", "JAPAN_POST");
        result.put("K", "KIX_CODE");
        result.put("L", "PLANET_CODE");
        result.put("M", "USPS_4_STATE, INTELLIGENT_MAIL");
        result.put("N", "UPU_4_STATE, ID_TAGS");
        result.put("O", "OCR");
        result.put("P", "POSTNET");
        result.put("Q", "HK25, CHINA_POST");
        result.put("R", "MICROPDF");
        result.put("S", "SECURE_CODE");
        result.put("T", "TLC39");
        result.put("U", "ULTRACODE");
        result.put("V", "CODABLOCK_A");
        result.put("W", "POSICODE");
        result.put("X", "GRID_MATRIX");
        result.put("Y", "NEC25");
        result.put("Z", "MESA");
        result.put("a", "CODABAR");
        result.put("b", "CODE39");
        result.put("c", "UPCA");
        result.put("d", "EAN13");
        result.put("e", "I25");
        result.put("f", "S25 (2BAR and 3BAR)");
        result.put("g", "MSI");
        result.put("h", "CODE11");
        result.put("i", "CODE93");
        result.put("j", "CODE128");
        result.put("k", "UNUSED");
        result.put("l", "CODE49");
        result.put("m", "M25");
        result.put("n", "PLESSEY");
        result.put("o", "CODE16K");
        result.put("p", "CHANNELCODE");
        result.put("q", "CODABLOCK_F");
        result.put("r", "PDF417");
        result.put("s", "QRCODE");
        result.put("t", "TELEPEN");
        result.put("u", "CODEZ");
        result.put("v", "VERICODE");
        result.put("w", "DATAMATRIX");
        result.put("x", "MAXICODE");
        result.put("y", "RSS, GS1_DATABAR, COMPOSITE");
        result.put("z", "AZTEC_CODE");
        result.put("-", "MICROQR_ALT");
        result.put("{", "GS1_DATABAR_LIM");
        result.put("}", "GS1_DATABAR_EXP");

        return Collections.unmodifiableMap(result);
    }

    private static String getSymbol(String codeId) {
        if (symbologyCodeMapper.containsKey(codeId)) {
            return symbologyCodeMapper.get(codeId);
        }
        return "UNDEFINED";
    }

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
        if (barcodeReadEvent != null) {
            params.putString("all", barcodeReadEvent.toString());
            params.putString("data", barcodeReadEvent.getBarcodeData());
            params.putString("timeStamp", barcodeReadEvent.getTimestamp());
            params.putString("charSet", barcodeReadEvent.getCharset().displayName());
            params.putString("codeId", barcodeReadEvent.getCodeId());
            params.putString("symbology", getSymbol(barcodeReadEvent.getCodeId()));
        }
        params.putString("propagated", "true");
        sendEvent(BARCODE_READ_SUCCESS, params);
    }

    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {
        if (D) Log.d(TAG, "HONEYWELLSCANNER - Barcode scan failed");
        WritableMap params = Arguments.createMap();
        params.putString("data", "Scan Failed at " + barcodeFailureEvent.getTimestamp());
        sendEvent(BARCODE_READ_FAIL, params);
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
    public void getReaderInfo(Callback cb) {
        if (reader != null) {
            try {
                cb.invoke(null, reader.getInfo().getName());
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                cb.invoke(e.getLocalizedMessage(), null);
            }
        }
    }

    @ReactMethod
    public void softwareTriggerStart(Callback cb) {
        if (reader != null) {
            try {
                reader.softwareTrigger(true);
                cb.invoke(null, "Trigger Started");
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                cb.invoke(e.getLocalizedMessage(), null);
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                cb.invoke(e.getLocalizedMessage(), null);
            }
        }
    }

    @ReactMethod
    public void softwareTriggerStop(Callback cb) {
        if (reader != null) {
            try {
                reader.softwareTrigger(false);
                cb.invoke(null, "Trigger Stopped");
            } catch (ScannerNotClaimedException e) {
                e.printStackTrace();
                cb.invoke(e.getLocalizedMessage(), null);
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                cb.invoke(e.getLocalizedMessage(), null);
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
