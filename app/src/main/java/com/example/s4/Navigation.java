package com.example.s4;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;

public class Navigation {

    // Retourne true si l'item du menu a été traité
    public static boolean gerer(AppCompatActivity a, int id) {
        Class<?> cible = null;
        if (id == R.id.standard)         cible = MainActivity2.class;
        else if (id == R.id.Scientifique) cible = ScientifiqueActivity.class;
        else if (id == R.id.devise)       cible = DeviseActivity.class;
        else if (id == R.id.Temperature)  cible = TemperatureActivity.class;

        if (cible == null) return false;          // ex : le parent « Convertisseur »
        if (a.getClass() == cible) return true;   // déjà sur cet écran

        Intent i = new Intent(a, cible);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        a.startActivity(i);
        if (!(a instanceof MainActivity2)) a.finish(); // la Standard reste toujours en dessous
        return true;
    }
}