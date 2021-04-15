package com.useriq.demo;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class LayersFragment extends Fragment {

    private LinearLayout layer0;
    private LinearLayout layer1;

    public LayersFragment() {
        // Required empty public constructor
    }

    public static LayersFragment newInstance() {
        LayersFragment fragment = new LayersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layers, container, false);

        layer0 = view.findViewById(R.id.layer0);
        layer1 = view.findViewById(R.id.layer1);

        view.findViewById(R.id.button_alpha_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer1.setAlpha(1);
            }
        });

        view.findViewById(R.id.button_alpha_0).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer1.setAlpha(0);
            }
        });

        view.findViewById(R.id.button_bg_null).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer1.setBackground(null);
            }
        });

        view.findViewById(R.id.button_bg_trans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer1.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        view.findViewById(R.id.button_bg_draw_trans).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layer1.setBackground(new ColorDrawable(Color.TRANSPARENT));
            }
        });

        return view;
    }

}
