package org.linphone.assistant;
/*
LoginFragment.java
Copyright (C) 2017  Belledonne Communications, Grenoble, France

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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;
import org.linphone.R;
import org.linphone.core.TransportType;

import java.util.ArrayList;

public class LoginFragment extends Fragment implements OnClickListener, TextWatcher {
    private EditText mLogin, mUserid, mPassword, mDomain, mDisplayName;
    private RadioGroup mTransports;
    private Button mApply;
    private ImageButton mScanner;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) { View view = inflater.inflate(R.layout.assistant_login, container, false);


        mLogin = view.findViewById(R.id.assistant_username);
        mLogin.addTextChangedListener(this);
        mDisplayName = view.findViewById(R.id.assistant_display_name);
        mDisplayName.addTextChangedListener(this);
        mUserid = view.findViewById(R.id.assistant_userid);
        mUserid.addTextChangedListener(this);
        mPassword = view.findViewById(R.id.assistant_password);
        mPassword.addTextChangedListener(this);
        mDomain = view.findViewById(R.id.assistant_domain);
        mDomain.addTextChangedListener(this);
        mTransports = view.findViewById(R.id.assistant_transports);
        mApply = view.findViewById(R.id.assistant_apply);
        mApply.setEnabled(false);
        mApply.setOnClickListener(this);
        mScanner = view.findViewById(R.id.qr_button);
        mScanner.setOnClickListener(this);
        try{
            mDomain.setText(getArguments().getString("url", ""));
            if(getArguments().getBoolean("multi")){
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle("Choose a user");
                JSONArray creds = new JSONArray(getArguments().getString("passes", ""));
                final ArrayList<String> users = new ArrayList<>();
                final ArrayList<String> passes = new ArrayList<>();
                boolean end = false;
                int i = 1;
                while(!end){
                    try{
                        JSONObject obj = creds.getJSONObject(i);
                        users.add(getArguments().getString("num", "user") + 'u' + i);
                        passes.add(obj.getString(Integer.toString(i)));
                        i++;
                    }
                    catch (Exception e){
                        end = true;
                    }
                }
                builder.setSingleChoiceItems(users.toArray(new String[i-2]), 0, null);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int pos = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                        mLogin.setText(users.get(pos));
                        mUserid.setText(users.get(pos));
                        mPassword.setText(passes.get(pos));
                    }
                });
                builder.setNegativeButton("Cancel", null);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
            else{
                mPassword.setText(getArguments().getString("pass", ""));
                mLogin.setText(getArguments().getString("num", ""));
                mUserid.setText(getArguments().getString("num", ""));
            }
        }
        catch (Exception e){
            Log.e("QR scanner", e.toString());
        }

        return view;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.assistant_apply) {
            if (mLogin.getText() == null
                    || mLogin.length() == 0
                    || mPassword.getText() == null
                    || mPassword.length() == 0
                    || mDomain.getText() == null
                    || mDomain.length() == 0) {
                Toast.makeText(
                                getActivity(),
                                getString(R.string.first_launch_no_login_password),
                                Toast.LENGTH_LONG)
                        .show();
                return;
            }

            TransportType transport;
            if (mTransports.getCheckedRadioButtonId() == R.id.transport_tls) {
                transport = TransportType.Tls;
            } else {
                if (mTransports.getCheckedRadioButtonId() == R.id.transport_tcp) {
                    transport = TransportType.Tcp;
                } else {
                    transport = TransportType.Udp;
                }
            }

            if (mDomain.getText().toString().compareTo(getString(R.string.default_domain)) == 0) {
                AssistantActivity.instance()
                        .displayLoginLinphone(
                                mLogin.getText().toString(), mPassword.getText().toString());
            } else {
                AssistantActivity.instance()
                        .genericLogIn(
                                mLogin.getText().toString(),
                                mUserid.getText().toString(),
                                mPassword.getText().toString(),
                                mDisplayName.getText().toString(),
                                null,
                                mDomain.getText().toString(),
                                transport);
            }
        }
        else if(id == R.id.qr_button){
            AssistantActivity.instance().displayQRCodeReader();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mApply.setEnabled(
                !mLogin.getText().toString().isEmpty()
                        && !mPassword.getText().toString().isEmpty()
                        && !mDomain.getText().toString().isEmpty());
    }

    @Override
    public void afterTextChanged(Editable s) {}
}
