package com.refugio.pawrescue.ui.publico;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdoptionFormPagerAdapter extends FragmentStateAdapter {

    public AdoptionFormPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Aquí definimos qué fragmento (pantalla) va en cada paso
        switch (position) {
            case 0:
                return new Step1PersonalDataFragment();
            case 1:
                return new Step2FamilyFragment();
            case 2:
                return new Step3ExperienceFragment();
            case 3:
                return new Step4CommitmentFragment();
            case 4:
                return new Step5ReviewFragment();
            default:
                return new Step1PersonalDataFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5; // Tenemos 5 pasos en total
    }
}