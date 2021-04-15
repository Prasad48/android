package com.useriq.demo;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.useriq.sdk.UserIQSDK;

public class FirstFragment extends Fragment {

    private EditText hostET;
    private EditText apiET;
    private EditText userIdET;
    private EditText accIdET;
    private EditText accNameET;
    private EditText userNameET;
    private EditText emailET;
    private EditText signUpDateET;

    public FirstFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_first, container, false);

        hostET = view.findViewById(R.id.host);
        apiET = view.findViewById(R.id.api);
        userIdET = view.findViewById(R.id.userId);
        accIdET = view.findViewById(R.id.accId);
        accNameET = view.findViewById(R.id.accName);
        userNameET = view.findViewById(R.id.userName);
        emailET = view.findViewById(R.id.email);
        signUpDateET = view.findViewById(R.id.signUpDate);

        view.findViewById(R.id.btn_init).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.init(getActivity(), apiET.getText().toString());

                UserIQSDK.User user = new UserIQSDK.UserBuilder()
                        .setId(userIdET.getText().toString())
                        .setAccountId(accIdET.getText().toString())
                        .setAccountName(accNameET.getText().toString())
                        .setName(userNameET.getText().toString())
                        .setEmail(emailET.getText().toString())
                        .build();

                UserIQSDK.setUser(getActivity(), user);

                ((MainActivity) getActivity()).updatedUser(userIdET.getText().toString(), userNameET.getText().toString(),
                        apiET.getText().toString());

                Toast.makeText(getContext(), "UserIQ SDK: Initialized", Toast.LENGTH_SHORT).show();
            }
        });

        view.findViewById(R.id.btn_update_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.User user = new UserIQSDK.UserBuilder()
                        .setId(userIdET.getText().toString())
                        .setAccountId(accIdET.getText().toString())
                        .setAccountName(accNameET.getText().toString())
                        .setName(userNameET.getText().toString())
                        .setEmail(emailET.getText().toString())
                        .build();

                UserIQSDK.setUser(getActivity(), user);

                ((MainActivity) getActivity()).updatedUser(userIdET.getText().toString(), userNameET.getText().toString(),
                        apiET.getText().toString());

                Toast.makeText(getContext(), "UserIQ SDK: User updated", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

}
