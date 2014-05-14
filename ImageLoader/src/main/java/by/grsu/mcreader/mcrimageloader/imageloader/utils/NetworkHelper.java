package by.grsu.mcreader.mcrimageloader.imageloader.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.epam.android.framework.R;

import context.ContextHolder;

@SuppressLint("ValidFragment")
public class NetworkHelper {

    private static final String LOG_TAG = NetworkHelper.class.getSimpleName();
    protected static NetworkHelperDialog helperDialog;
    protected static boolean mCanceled = false;

    private NetworkHelper() {
    }

    public static boolean checkConnection(Context context) {
        if (context == null) {
            context = ContextHolder.getInstance().getContext();
        }
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean connected = connectivityManager.getActiveNetworkInfo() != null;
        return connected;
    }

    public static void checkAndConnect(final Context context,
                                       final NetworkCallback callback) {
        if (checkConnection(context)) {
            callback.processTask(context);
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.no_internet_connection)
                    .setMessage(R.string.connect_request_message)
                    .setCancelable(false)
                    .setPositiveButton(R.string.alert_dialog_connect,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    mCanceled = false;
                                    connectToAvaibleNetwork(context, callback);
                                    helperDialog = new NetworkHelperDialog(
                                            context, callback);
                                    helperDialog.setCancelable(false);
                                    FragmentManager fragmentManager = ((FragmentActivity) context)
                                            .getSupportFragmentManager();
                                    helperDialog.show(fragmentManager, null);
                                }
                            })
                    .setNeutralButton(R.string.alert_dialog_settings,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                    provideToSettings(context);
                                }
                            })
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    callback.onCancel(context);
                                    dialog.dismiss();
                                }
                            }).create().show();

        }
    }

    protected static void provideToSettings(Context context) {
        context.startActivity(new Intent(
                android.provider.Settings.ACTION_SETTINGS));

    }

    protected static void connectToAvaibleNetwork(final Context context,
                                                  final NetworkCallback callback) {

        new AsyncTask<Void, Integer, Object>() {

            @Override
            protected Object doInBackground(Void... params) {

                ConnectivityManager manager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                final WifiManager wifiManager = (WifiManager) context
                        .getSystemService(Context.WIFI_SERVICE);
                if (manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI) != null) {
                    // connect WI-FI

                    publishProgress(R.string.connecting_wifi);

                    boolean wifiEnabled = wifiManager.isWifiEnabled();
                    L.d(LOG_TAG, "connectToAvaibleNetwork: wifi enabled "
                            + wifiEnabled);

                    if (!wifiEnabled) {
                        wifiManager.setWifiEnabled(true);
                        int counter = 0;
                        while (manager.getActiveNetworkInfo() == null
                                && counter < 30) {
                            // waiting max 15 sec
                            counter++;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                return e;
                            }
                            if (mCanceled) {
                                wifiManager.setWifiEnabled(false);
                                return null;
                            }
                        }
                    }
                    if (manager.getActiveNetworkInfo() != null) {
                        if (manager.getActiveNetworkInfo().isConnected()) {
                            L.d(LOG_TAG,
                                    "connectToAvaibleNetwork: wifi connected");
                            return null;
                        }
                    } else
                        wifiManager.setWifiEnabled(false);
                }
                // connect MOBILE

                NetworkInfo mobileInfo = manager
                        .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
                if (mobileInfo == null) {
                    return new ConnectException("Connection unavaible.");
                }
                if (mobileInfo.isAvailable() && !mCanceled) {
                    L.d(LOG_TAG, "connectToAvaibleNetwork: mobile avaible");

                    publishProgress(R.string.connecting_mobile);

                    if (mobileInfo.isRoaming()) {
                        // IMPORTANT
                        // notify user about possible high cost data
                        // transfer
                    }
                    try {
                        connectMobile(context, true);
                        int counter = 0;
                        while (manager.getActiveNetworkInfo() == null
                                && counter < 30) {
                            // wait max 15 sec
                            counter++;
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                return e;
                            }
                            if (!mobileInfo.isAvailable()) {
                                L.d(LOG_TAG,
                                        "connectToAvaibleNetwork: mobile unavaible, disconnect");
                                connectMobile(context, false);
                                break;
                            }
                            if (mCanceled) {
                                connectMobile(context, false);
                                return null;
                            }
                        }
                        if (manager.getActiveNetworkInfo() != null) {
                            if (manager.getActiveNetworkInfo().isConnected()) {
                                L.d(LOG_TAG,
                                        "connectToAvaibleNetwork: mobile connected");
                                return null;
                            }
                        } else
                            return new ConnectException("Connection unavaible.");
                    } catch (IllegalArgumentException e) {
                        return e;
                    } catch (ClassNotFoundException e) {
                        return e;
                    } catch (NoSuchFieldException e) {
                        return e;
                    } catch (IllegalAccessException e) {
                        return e;
                    } catch (NoSuchMethodException e) {
                        return e;
                    } catch (InvocationTargetException e) {
                        return e;
                    }
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                helperDialog.setMessage(values[0]);
            }

            @Override
            protected void onPostExecute(Object result) {
                super.onPostExecute(result);

                helperDialog.showProgress(false);
                if (result instanceof Exception) {
                    callback.onError(context, (Exception) result);
                    helperDialog.setMessage(R.string.connection_failed);
                    // TODO something like add button repeat and cancel
                } else {
                    if (mCanceled) {
                        callback.onCancel(context);
                    } else {
                        callback.processTask(context);
                    }
                }
                helperDialog.dismiss();
            }

        }.execute();

    }

    @SuppressWarnings("unchecked")
    private static boolean connectMobile(Context context, boolean enable)
            throws ClassNotFoundException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.FROYO) {
            L.d("version:", "Found Froyo");
            Method dataConnSwitchmethod;
            Class telephonyManagerClass;
            Object telephonyStub;
            Class telephonyClass;

            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            telephonyManagerClass = Class.forName(telephonyManager.getClass()
                    .getName());
            Method getITelephonyMethod = telephonyManagerClass
                    .getDeclaredMethod("getITelephony");
            getITelephonyMethod.setAccessible(true);
            telephonyStub = getITelephonyMethod.invoke(telephonyManager);
            telephonyClass = Class.forName(telephonyStub.getClass().getName());

            if (enable) {
                dataConnSwitchmethod = telephonyClass
                        .getDeclaredMethod("enableDataConnectivity");
            } else {
                dataConnSwitchmethod = telephonyClass
                        .getDeclaredMethod("disableDataConnectivity");
            }
            dataConnSwitchmethod.setAccessible(true);
            dataConnSwitchmethod.invoke(telephonyStub);
            return true;

        } else {
            L.d("version:", "Found Gingerbread+");
            final ConnectivityManager conman = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            final Class conmanClass = Class
                    .forName(conman.getClass().getName());
            final Field connectivityManagerField = conmanClass
                    .getDeclaredField("mService");
            connectivityManagerField.setAccessible(true);
            final Object iConnectivityManager = connectivityManagerField
                    .get(conman);
            final Class iConnectivityManagerClass = Class
                    .forName(iConnectivityManager.getClass().getName());
            final Method setMobileDataEnabledMethod = iConnectivityManagerClass
                    .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConnectivityManager, enable);
            return true;
        }
    }

    public interface NetworkCallback {

        public void processTask(Context context);

        public void onCancel(Context context);

        public void onError(Context context, Exception e);
    }

    public static class NetworkHelperDialog extends DialogFragment {

        private Context mContext;
        private NetworkCallback mCallback;
        private TextView mTextViewMessage;
        private ProgressBar mProgressBar;

        public NetworkHelperDialog() {
            super();
        }

        public NetworkHelperDialog(Context context, NetworkCallback callback) {
            super();
            mContext = context;
            mCallback = callback;
        }

        public void setMessage(String message) {
            mTextViewMessage.setText(message);
        }

        public void setMessage(int messageResource) {
            mTextViewMessage.setText(messageResource);
        }

        public void showProgress(boolean show) {
            if (show)
                mProgressBar.setVisibility(View.VISIBLE);
            else
                mProgressBar.setVisibility(View.INVISIBLE);
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            LayoutInflater inflater = getActivity().getLayoutInflater();
            View view = inflater.inflate(R.layout.view_network_dialog, null);
            mTextViewMessage = (TextView) view
                    .findViewById(R.id.textViewMessage);
            mProgressBar = (ProgressBar) view
                    .findViewById(R.id.progressBarDialog);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.no_internet_connection)
                    .setView(view)
                    .setNegativeButton(R.string.alert_dialog_cancel,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {
                                    mCanceled = true;
                                    mCallback.onCancel(mContext);
                                }
                            });
            AlertDialog alertDialog = builder.create();
            return alertDialog;
        }

    }
}
