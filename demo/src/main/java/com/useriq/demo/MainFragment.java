package com.useriq.demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.useriq.sdk.UserIQSDK;

public class MainFragment extends Fragment {

    public MainFragment() {
        // Required empty public constructor
    }

    public static MainFragment newInstance() {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        view.findViewById(R.id.button_log_out).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.logOut();
            }
        });

        view.findViewById(R.id.button_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.findViewById(R.id.button_dialog).setEnabled(false);
                view.findViewById(R.id.button_dialog).setBackgroundColor(getContext().getResources().getColor(R.color.grey));
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.findViewById(R.id.button_dialog).setEnabled(true);
                        view.findViewById(R.id.button_dialog).setBackgroundColor(getContext().getResources().getColor(R.color.catalyst_redbox_background));
                        new AlertDialog.Builder(getContext())
                                .setTitle("Dialog Test")
                                .setMessage("This is a test dialog, you can tag this for the tracking")
                                .setPositiveButton("Sure", null)
                                .create()
                                .show();
                    }
                }, 5000);
            }
        });

        view.findViewById(R.id.button_screen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), EmptyActivity.class));
            }
        });

        view.findViewById(R.id.button_ctxHelp).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.showCtxHelp(getContext());
            }
        });

        view.findViewById(R.id.button_helpCentre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.showHelpCentre(getContext());
            }
        });

        view.findViewById(R.id.button_update_user).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserIQSDK.User user = new UserIQSDK.UserBuilder()
                        .addParams("cigarette", "Marlboro")
                        .addParams("beer", "Bud")
                        .addParams("biksy", "kala kutta")
                        .addParams("maal", "parvati")
                        .addParams("l_ass_d", "steager")
                        .addParams("hash", "kasol")
                        .build();
                UserIQSDK.setUser(getActivity(), user);
            }
        });


        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), ReactActivity.class));
            }
        });
        return view;
    }
}
