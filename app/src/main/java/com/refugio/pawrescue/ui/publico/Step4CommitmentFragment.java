package com.refugio.pawrescue.ui.publico;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.refugio.pawrescue.R;
import java.util.HashMap;
import java.util.Map;

public class Step4CommitmentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_step4_commitment, container, false);
    }

    public Map<String, Object> getData() {
        Map<String, Object> data = new HashMap<>();
        return data;
    }
}