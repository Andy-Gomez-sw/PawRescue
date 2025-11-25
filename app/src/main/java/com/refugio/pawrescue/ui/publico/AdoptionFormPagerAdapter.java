package com.refugio.pawrescue.ui.publico;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AdoptionFormPagerAdapter extends FragmentStateAdapter {

    // Guardamos las instancias para poder pedirles los datos luego
    private Step1PersonalDataFragment step1 = new Step1PersonalDataFragment();
    private Step2FamilyFragment step2 = new Step2FamilyFragment();
    private Step3ExperienceFragment step3 = new Step3ExperienceFragment();
    private Step4CommitmentFragment step4 = new Step4CommitmentFragment();
    private Step5ReviewFragment step5 = new Step5ReviewFragment();

    public AdoptionFormPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return step1;
            case 1: return step2;
            case 2: return step3;
            case 3: return step4;
            case 4: return step5;
            default: return step1;
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    // Métodos públicos para obtener los fragments desde la Actividad
    public Step1PersonalDataFragment getStep1() { return step1; }
    public Step2FamilyFragment getStep2() { return step2; }
    public Step3ExperienceFragment getStep3() { return step3; }
    public Step4CommitmentFragment getStep4() { return step4; }
    public Step5ReviewFragment getStep5() { return step5; }
}