package com.example.s4;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class DeviseActivity extends AppCompatActivity {

    private static final String URL_API = "https://open.er-api.com/v6/latest/USD";
    private static final String PREFS = "taux_devises";

    EditText etMontant;
    Spinner spDe, spVers;
    TextView tvResultat, tvInfo;
    Button btnInverser;

    String[] devises;      // "MAD - Dirham marocain", ...
    JSONObject rates;      // taux par rapport à 1 USD (null tant qu'on n'a rien)

    private final AdapterView.OnItemSelectedListener ecouteur =
            new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                    convertir();
                }
                @Override
                public void onNothingSelected(AdapterView<?> p) { }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_devise);
        setTitle(R.string.item4);

        View root = findViewById(R.id.Progect);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });

        etMontant = findViewById(R.id.etMontant);
        spDe = findViewById(R.id.spDe);
        spVers = findViewById(R.id.spVers);
        tvResultat = findViewById(R.id.tvResultat);
        tvInfo = findViewById(R.id.tvInfo);
        btnInverser = findViewById(R.id.btnInverser);

        devises = getResources().getStringArray(R.array.devises);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.devises, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spDe.setAdapter(adapter);
        spVers.setAdapter(adapter);
        spDe.setSelection(1);    // EUR
        spVers.setSelection(0);  // MAD
        spDe.setOnItemSelectedListener(ecouteur);
        spVers.setOnItemSelectedListener(ecouteur);

        etMontant.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { convertir(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnInverser.setOnClickListener(v -> {
            int a = spDe.getSelectedItemPosition();
            spDe.setSelection(spVers.getSelectedItemPosition());
            spVers.setSelection(a);
        });

        chargerCache();   // affiche tout de suite les derniers taux connus
        telecharger();    // puis tente une mise à jour
    }

    // ---------- Taux de change ----------
    private void chargerCache() {
        SharedPreferences sp = getSharedPreferences(PREFS, MODE_PRIVATE);
        String json = sp.getString("rates", null);
        if (json == null) return;
        try {
            rates = new JSONObject(json);
            tvInfo.setText("Taux du " + sp.getString("date", "?") + " (enregistrés)");
            convertir();
        } catch (Exception ignore) { }
    }

    private void telecharger() {
        tvInfo.setText("Mise à jour des taux…");
        new Thread(() -> {
            HttpURLConnection c = null;
            try {
                c = (HttpURLConnection) new URL(URL_API).openConnection();
                c.setConnectTimeout(8000);
                c.setReadTimeout(8000);

                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String ligne;
                while ((ligne = br.readLine()) != null) sb.append(ligne);
                br.close();

                JSONObject o = new JSONObject(sb.toString());
                if (!"success".equals(o.optString("result"))) throw new Exception("API");
                JSONObject r = o.getJSONObject("rates");
                String d = o.optString("time_last_update_utc", "");
                String date = d.length() > 16 ? d.substring(0, 16) : d;   // "Fri, 30 Oct 2026"

                getSharedPreferences(PREFS, MODE_PRIVATE).edit()
                        .putString("rates", r.toString())
                        .putString("date", date)
                        .apply();

                runOnUiThread(() -> {
                    rates = r;
                    tvInfo.setText("Taux du " + date);
                    convertir();
                });
            } catch (Exception e) {
                runOnUiThread(() -> tvInfo.setText(rates == null
                        ? "Pas de connexion : taux indisponibles"
                        : tvInfo.getText() + " – hors ligne"));
            } finally {
                if (c != null) c.disconnect();
            }
        }).start();
    }

    // ---------- Conversion ----------
    private String code(int position) {
        return devises[position].substring(0, 3);
    }

    private void convertir() {
        if (rates == null) {
            tvResultat.setText("");
            return;
        }
        try {
            double montant = Double.parseDouble(etMontant.getText().toString().replace(',', '.'));
            String de = code(spDe.getSelectedItemPosition());
            String vers = code(spVers.getSelectedItemPosition());
            double r = montant / rates.getDouble(de) * rates.getDouble(vers);
            tvResultat.setText(String.format(Locale.FRANCE, "%,.2f", r) + " " + vers);
        } catch (Exception e) {   // champ vide ou devise absente
            tvResultat.setText("");
        }
    }

    // ---------- Menu ----------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Navigation.gerer(this, item.getItemId())) return true;
        return super.onOptionsItemSelected(item);
    }
}