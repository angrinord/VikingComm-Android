package org.linphone.settings;

/*
AccountSettingsFragment.java
Copyright (C) 2019 Belledonne Communications, Grenoble, France

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import org.linphone.assistant.AssistantActivity;
import org.linphone.fragments.FragmentsAvailable;
import org.linphone.settings.widget.BasicSetting;
import org.linphone.settings.widget.ListSetting;
import org.linphone.settings.widget.SettingListenerBase;
import org.linphone.settings.widget.SwitchSetting;
import org.linphone.settings.widget.TextSetting;
import org.linphone.LinphoneActivity;
import org.linphone.LinphoneManager;
import org.linphone.R;
import org.linphone.core.AVPFMode;
import org.linphone.core.Address;
import org.linphone.core.AuthInfo;
import org.linphone.core.Core;
import org.linphone.core.Factory;
import org.linphone.core.ProxyConfig;
import org.linphone.core.TransportType;
import org.linphone.core.tools.Log;
import org.linphone.utils.PushNotificationUtils;

public class AccountSettingsFragment extends Fragment {
    protected View mRootView;
    protected LinphonePreferences mPrefs;
    private int mAccountIndex;
    private ProxyConfig mProxyConfig;
    private AuthInfo mAuthInfo;
    private boolean mIsNewlyCreatedAccount;

    private TextSetting mUsername,
            mUserId,
            mPassword,
            mDomain,
            mDisplayName,
            mProxy,
            //mStun,
            mExpire,
            //mPrefix,
            mAvpfInterval;
    private SwitchSetting mDisable,
            mUseAsDefault,
            mOutboundProxy,
            //mIce,
            mAvpf,
            mReplacePlusBy00,
            mPush;
    private BasicSetting mChangePassword, mDeleteAccount, mLinkAccount;
    private ListSetting mTransport;

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.settings_account, container, false);

        loadSettings();

        mIsNewlyCreatedAccount = true;
        mAccountIndex = getArguments().getInt("Account", -1);
        if (mAccountIndex == -1 && savedInstanceState != null) {
            mAccountIndex = savedInstanceState.getInt("Account", -1);
        }

        mProxyConfig = null;
        Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (mAccountIndex >= 0 && core != null) {
            ProxyConfig[] proxyConfigs = core.getProxyConfigList();
            if (proxyConfigs.length > mAccountIndex) {
                mProxyConfig = proxyConfigs[mAccountIndex];
                mIsNewlyCreatedAccount = false;
            } else {
                Log.e("[Account Settings] Proxy config not found !");
            }
        }

        return mRootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("Account", mAccountIndex);
    }

    @Override
    public void onResume() {
        super.onResume();

        mPrefs = LinphonePreferences.instance();
        if (LinphoneActivity.isInstantiated()) {
            LinphoneActivity.instance()
                    .selectMenu(
                            FragmentsAvailable.SETTINGS_SUBLEVEL,
                            getString(R.string.pref_sipaccount));
        }

        updateValues();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mIsNewlyCreatedAccount) {
            Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
            if (core != null && mProxyConfig != null && mAuthInfo != null) {
                core.addAuthInfo(mAuthInfo);
                core.addProxyConfig(mProxyConfig);
                if (mUseAsDefault.isChecked()) {
                    core.setDefaultProxyConfig(mProxyConfig);
                }
            }
        }
    }

    protected void loadSettings() {
        mUsername = mRootView.findViewById(R.id.pref_username);
        mUsername.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        mUserId = mRootView.findViewById(R.id.pref_auth_userid);
        mUserId.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        mPassword = mRootView.findViewById(R.id.pref_passwd);
        mPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

        mDomain = mRootView.findViewById(R.id.pref_domain);
        mDomain.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        mDisplayName = mRootView.findViewById(R.id.pref_display_name);
        mDisplayName.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);

        mProxy = mRootView.findViewById(R.id.pref_proxy);
        mProxy.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

//        mStun = mRootView.findViewById(R.id.pref_stun_server);
//        mStun.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

        mExpire = mRootView.findViewById(R.id.pref_expire);
        mExpire.setInputType(InputType.TYPE_CLASS_NUMBER);

//        mPrefix = mRootView.findViewById(R.id.pref_prefix);
//        mPrefix.setInputType(InputType.TYPE_CLASS_TEXT);

        mAvpfInterval = mRootView.findViewById(R.id.pref_avpf_rr_interval);
        mAvpfInterval.setInputType(InputType.TYPE_CLASS_NUMBER);

        mDisable = mRootView.findViewById(R.id.pref_disable_account);

        mUseAsDefault = mRootView.findViewById(R.id.pref_default_account);

        mOutboundProxy = mRootView.findViewById(R.id.pref_enable_outbound_proxy);

        //mIce = mRootView.findViewById(R.id.pref_ice_enable);

        mAvpf = mRootView.findViewById(R.id.pref_avpf);

        mReplacePlusBy00 = mRootView.findViewById(R.id.pref_escape_plus);

        mPush = mRootView.findViewById(R.id.pref_push_notification);
        mPush.setVisibility(
                PushNotificationUtils.isAvailable(getActivity()) ? View.VISIBLE : View.GONE);

        mChangePassword = mRootView.findViewById(R.id.pref_change_password);
        mChangePassword.setVisibility(View.GONE); // TODO

        mDeleteAccount = mRootView.findViewById(R.id.pref_delete_account);

        mLinkAccount = mRootView.findViewById(R.id.pref_link_account);

        mTransport = mRootView.findViewById(R.id.pref_transport);
        initTransportList();
    }

    protected void setListeners() {
        mUsername.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE
                                || actionId == EditorInfo.IME_ACTION_NEXT) {
                            if (mAuthInfo != null) {
                                mAuthInfo.setUsername(mUsername.getValue());
                            } else {
                                Log.e("[Account Settings] No auth info !");
                            }
                            if (mProxyConfig != null) {
                                mProxyConfig.edit();
                                Address identity = mProxyConfig.getIdentityAddress();
                                if (identity != null) {
                                    identity.setUsername(mUsername.getValue());
                                }
                                mProxyConfig.setIdentityAddress(identity);
                                mProxyConfig.done();
                            } else {
                                Log.e("[Account Settings] No proxy config !");
                            }
                        }
                        return false;
                    }
                });

        mUserId.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mAuthInfo != null) {
                            mAuthInfo.setUserid(mUserId.getValue());
                            Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
                            if (core != null) {
                                core.refreshRegisters();
                            }
                        } else {
                            Log.e("[Account Settings] No auth info !");
                        }
                        return false;
                    }
                });

        mPassword.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mAuthInfo != null) {
                            mAuthInfo.setPassword(mPassword.getValue());
                            Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
                            if (core != null && mPassword.getValue() != null) {
                                AuthInfo[] a = core.getAuthInfoList();
                                a[0].setPassword(mPassword.getValue());
                                core.clearAllAuthInfo();
                                core.addAuthInfo(
                                        Factory.instance()
                                                .createAuthInfo(
                                                        a[0].getUsername(),
                                                        a[0].getUserid(),
                                                        mPassword.getValue(),
                                                        null,
                                                        null,
                                                        a[0].getDomain()));
                                core.refreshRegisters();
                            }
                        } else {
                            Log.e("[Account Settings] No auth info !");
                        }
                        return false;
                    }
                });

        mDomain.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mAuthInfo != null) {
                            mAuthInfo.setDomain(mDomain.getValue());
                        } else {
                            Log.e("[Account Settings] No auth info !");
                        }

                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            Address oldIdentity = mProxyConfig.getIdentityAddress();
                            String identity =
                                    "sip:" + oldIdentity.getUsername() + "@" + mDomain.getValue();
                            Address identityAddr = Factory.instance().createAddress(identity);
                            if (identityAddr != null) {
                                identityAddr.setDisplayName(oldIdentity.getDisplayName());
                                mProxyConfig.setIdentityAddress(identityAddr);
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                        return false;
                    }
                });

        mDisplayName.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            Address identity = mProxyConfig.getIdentityAddress();
                            if (identity != null) {
                                identity.setDisplayName(mDisplayName.getValue());
                            }
                            mProxyConfig.setIdentityAddress(identity);
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                        return false;
                    }
                });

        mProxy.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {

                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            Address proxy = Factory.instance().createAddress(mProxy.getValue());
                            if (proxy != null) {
                                mProxyConfig.setServerAddr(proxy.asString());
                                if (mOutboundProxy.isChecked()) {
                                    mProxyConfig.setRoute(proxy.asString());
                                }
                                mTransport.setValue(proxy.getTransport().toInt());
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                        return false;
                    }
                });

//        mStun.setOnEditorActionListener(
//                new EditText.OnEditorActionListener() {
//                    @Override
//                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                        if (mProxyConfig != null) {
//                            mProxyConfig.edit();
//                            NatPolicy natPolicy = mProxyConfig.getNatPolicy();
//                            if (natPolicy == null) {
//                                Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
//                                if (core != null) {
//                                    natPolicy = core.createNatPolicy();
//                                    mProxyConfig.setNatPolicy(natPolicy);
//                                }
//                            }
//                            if (natPolicy != null) {
//                                natPolicy.setStunServer(mStun.getValue());
//                            }
//                            if (mStun.getValue() == null || mStun.getValue().isEmpty()) {
//                                mIce.setChecked(false);
//                            }
//                            mIce.setEnabled(
//                                    mStun.getValue() != null && !mStun.getValue().isEmpty());
//                            mProxyConfig.done();
//                        } else {
//                            Log.e("[Account Settings] No proxy config !");
//                        }
//                        return false;
//                    }
//                });

        mExpire.setOnEditorActionListener(
                new EditText.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            try {
                                mProxyConfig.setExpires(Integer.parseInt(mExpire.getValue()));
                            } catch (NumberFormatException nfe) {
                                Log.e(nfe);
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                        return false;
                    }
                });

//        mPrefix.setOnEditorActionListener(
//                new EditText.OnEditorActionListener() {
//                    @Override
//                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                        if (mProxyConfig != null) {
//                            mProxyConfig.edit();
//                            mProxyConfig.setDialPrefix(mPrefix.getValue());
//                            mProxyConfig.done();
//                        } else {
//                            Log.e("[Account Settings] No proxy config !");
//                        }
//                        return false;
//                    }
//                });

        mAvpfInterval.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onTextValueChanged(String newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            try {
                                mProxyConfig.setAvpfRrInterval(Integer.parseInt(newValue));
                            } catch (NumberFormatException nfe) {
                                Log.e(nfe);
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mDisable.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            mProxyConfig.enableRegister(!newValue);
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mUseAsDefault.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
                            if (core != null && newValue) {
                                core.setDefaultProxyConfig(mProxyConfig);
                                mUseAsDefault.setEnabled(false);
                            }
                            LinphoneActivity.instance().refreshAccounts();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mOutboundProxy.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            if (newValue) {
                                mProxyConfig.setRoute(mProxy.getValue());
                            } else {
                                mProxyConfig.setRoute(null);
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

//        mIce.setListener(
//                new SettingListenerBase() {
//                    @Override
//                    public void onBoolValueChanged(boolean newValue) {
//                        if (mProxyConfig != null) {
//                            mProxyConfig.edit();
//
//                            NatPolicy natPolicy = mProxyConfig.getNatPolicy();
//                            if (natPolicy == null) {
//                                Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
//                                if (core != null) {
//                                    natPolicy = core.createNatPolicy();
//                                    mProxyConfig.setNatPolicy(natPolicy);
//                                }
//                            }
//
//                            if (natPolicy != null) {
//                                natPolicy.enableIce(newValue);
//                                natPolicy.enableStun(newValue);
//                            }
//                            mProxyConfig.done();
//                        } else {
//                            Log.e("[Account Settings] No proxy config !");
//                        }
//                    }
//                });

        mAvpf.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            mProxyConfig.setAvpfMode(
                                    newValue ? AVPFMode.Enabled : AVPFMode.Disabled);
                            mAvpfInterval.setEnabled(mProxyConfig.avpfEnabled());
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mReplacePlusBy00.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            mProxyConfig.setDialEscapePlus(newValue);
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mPush.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onBoolValueChanged(boolean newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            mProxyConfig.setPushNotificationAllowed(newValue);
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });

        mChangePassword.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        // TODO
                    }
                });

        mDeleteAccount.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
                        if (core != null) {
                            if (mProxyConfig != null) {
                                core.removeProxyConfig(mProxyConfig);
                            }
                            if (mAuthInfo != null) {
                                core.removeAuthInfo(mAuthInfo);
                            }
                        }
                        LinphoneActivity.instance().displaySettings();
                        LinphoneActivity.instance().refreshAccounts();
                    }
                });

        mLinkAccount.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onClicked() {
                        Intent assistant = new Intent();
                        assistant.setClass(LinphoneActivity.instance(), AssistantActivity.class);
                        assistant.putExtra("LinkPhoneNumber", true);
                        assistant.putExtra("FromPref", true);
                        assistant.putExtra("AccountNumber", mAccountIndex);
                        startActivity(assistant);
                    }
                });

        mTransport.setListener(
                new SettingListenerBase() {
                    @Override
                    public void onListValueChanged(int position, String newLabel, String newValue) {
                        if (mProxyConfig != null) {
                            mProxyConfig.edit();
                            String server = mProxyConfig.getServerAddr();
                            Address serverAddr = Factory.instance().createAddress(server);
                            if (serverAddr != null) {
                                try {
                                    serverAddr.setTransport(
                                            TransportType.fromInt(Integer.parseInt(newValue)));
                                    server = serverAddr.asString();
                                    mProxyConfig.setServerAddr(server);
                                    if (mOutboundProxy.isChecked()) {
                                        mProxyConfig.setRoute(server);
                                    }
                                    mProxy.setValue(server);
                                } catch (NumberFormatException nfe) {
                                    Log.e(nfe);
                                }
                            }
                            mProxyConfig.done();
                        } else {
                            Log.e("[Account Settings] No proxy config !");
                        }
                    }
                });
    }

    protected void updateValues() {
        Core core = LinphoneManager.getLcIfManagerNotDestroyedOrNull();
        if (core == null) return;

        // Create a proxy config if there is none
        if (mProxyConfig == null) {
            // Ensure the default configuration is loaded first
            String defaultConfig = LinphoneManager.getInstance().getDefaultDynamicConfigFile();
            core.loadConfigFromXml(defaultConfig);
            mProxyConfig = core.createProxyConfig();
            mAuthInfo = Factory.instance().createAuthInfo(null, null, null, null, null, null);
            mIsNewlyCreatedAccount = true;
        }

        if (mProxyConfig != null) {
            Address identityAddress = mProxyConfig.getIdentityAddress();
            mAuthInfo = mProxyConfig.findAuthInfo();
//            NatPolicy natPolicy = mProxyConfig.getNatPolicy();
//            if (natPolicy == null) {
//                natPolicy = core.createNatPolicy();
//                core.setNatPolicy(natPolicy);
//            }

            if (mAuthInfo != null) {
                mUserId.setValue(mAuthInfo.getUserid());
                // If password is hashed we can't display it
                mPassword.setValue(mAuthInfo.getPassword());
            }

            mUsername.setValue(identityAddress.getUsername());

            int prt = identityAddress.getPort();
            if(prt>0){
                mDomain.setValue(identityAddress.getDomain() + ":" + prt);
            }
            else{
                mDomain.setValue(identityAddress.getDomain());
            }

            mDisplayName.setValue(identityAddress.getDisplayName());

            mProxy.setValue(mProxyConfig.getServerAddr());

            //mStun.setValue(natPolicy.getStunServer());

            mExpire.setValue(mProxyConfig.getExpires());

//            mPrefix.setValue(mProxyConfig.getDialPrefix());

            mAvpfInterval.setValue(mProxyConfig.getAvpfRrInterval());
            mAvpfInterval.setEnabled(mProxyConfig.avpfEnabled());

            mDisable.setChecked(!mProxyConfig.registerEnabled());

            mUseAsDefault.setChecked(core != null && mProxyConfig.equals(core.getDefaultProxyConfig()));
            mUseAsDefault.setEnabled(!mUseAsDefault.isChecked());

            mOutboundProxy.setChecked(mProxyConfig.getRoute() != null);

//            mIce.setChecked(natPolicy.iceEnabled());
//            boolean stun = natPolicy.stunEnabled();
//            String server = natPolicy.getStunServer();
//            boolean ice = natPolicy.iceEnabled();
//            mIce.setEnabled(natPolicy.getStunServer() != null && !natPolicy.getStunServer().isEmpty());

            mAvpf.setChecked(mProxyConfig.avpfEnabled());

            mReplacePlusBy00.setChecked(mProxyConfig.getDialEscapePlus());

            mPush.setChecked(mProxyConfig.isPushNotificationAllowed());

            Address proxy = Factory.instance().createAddress(mProxyConfig.getServerAddr());
            if (proxy != null) {
                mTransport.setValue(proxy.getTransport().toInt());
            }
        }

        setListeners();
    }

    private void initTransportList() {
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();

        entries.add(getString(R.string.pref_transport_udp));
        values.add(String.valueOf(TransportType.Udp.toInt()));
        entries.add(getString(R.string.pref_transport_tcp));
        values.add(String.valueOf(TransportType.Tcp.toInt()));

        if (!getResources().getBoolean(R.bool.disable_all_security_features_for_markets)) {
            entries.add(getString(R.string.pref_transport_tls));
            values.add(String.valueOf(TransportType.Tls.toInt()));
        }

        mTransport.setItems(entries, values);
    }
}
