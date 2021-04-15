package com.useriq.demo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SystemModesFragment extends Fragment {

    int uiOptions;

    public SystemModesFragment() {
        // Required empty public constructor
    }

    public static SystemModesFragment newInstance() {
        SystemModesFragment fragment = new SystemModesFragment();
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
        View view = inflater.inflate(R.layout.fragment_screen_modes, container, false);

        view.findViewById(R.id.button_launch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SystemModesActivity.class);
                intent.putExtra("flags", uiOptions);
                startActivity(intent);
            }
        });

         uiOptions = getActivity().getWindow().getDecorView().getSystemUiVisibility();

        ((Switch) view.findViewById(R.id.switch_full_screen)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {

                }
            }
        });

        ((Switch) view.findViewById(R.id.switch_immersive)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {
                    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_IMMERSIVE);
                }
            }
        });

        ((Switch) view.findViewById(R.id.switch_hide_navigation)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {
                    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                }
            }
        });

        ((Switch) view.findViewById(R.id.switch_layout_hide_navigation)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {
                    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                }
            }
        });

        ((Switch) view.findViewById(R.id.switch_layout_full_screen)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {
                    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                }
            }
        });

        ((Switch) view.findViewById(R.id.switch_immersive_sticky)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    uiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
//                    getActivity().getWindow().getDecorView().setSystemUiVisibility(uiOptions);
                } else {
                    getActivity().getWindow().clearFlags(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }
}
