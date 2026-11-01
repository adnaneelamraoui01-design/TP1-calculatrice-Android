package com.example.s4;

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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TemperatureActivity extends AppCompatActivity {

    private static final String[] SYMBOLES = {"°C", "°F", "K"};

    EditText etValeur;
    Spinner spDe, spVers;
    TextView tvResultat;
    Button btnInverser;

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
        setContentView(R.layout.activity_temperature);
        setTitle(R.string.item5);

        View root = findViewById(R.id.Progect);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });

        etValeur = findViewById(R.id.etValeur);
        spDe = findViewById(R.id.spDe);
        spVers = findViewById(R.id.spVers);
        tvResultat = findViewById(R.id.tvResultat);
        btnInverser = findViewById(R.id.btnInverser);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.unites_temperature, R.layout.item_spinner);
        adapter.setDropDownViewResource(R.layout.item_spinner);
        spDe.setAdapter(adapter);
        spVers.setAdapter(adapter);
        spDe.setSelection(0);    // Celsius
        spVers.setSelection(1);  // Fahrenheit
        spDe.setOnItemSelectedListener(ecouteur);
        spVers.setOnItemSelectedListener(ecouteur);

        etValeur.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) { }
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { convertir(); }
            @Override public void afterTextChanged(Editable s) { }
        });

        btnInverser.setOnClickListener(v -> {
            int a = spDe.getSelectedItemPosition();
            spDe.setSelection(spVers.getSelectedItemPosition());
            spVers.setSelection(a);
        });

        convertir();
    }

    private void convertir() {
        double v;
        try {
            v = Double.parseDouble(etValeur.getText().toString().replace(',', '.'));
        } catch (NumberFormatException e) {   // champ vide, "-" ...
            tvResultat.setText("");
            return;
        }
        int de = spDe.getSelectedItemPosition();
        int vers = spVers.getSelectedItemPosition();

        double c = versCelsius(v, de);
        if (c < -273.15 - 1e-9) {
            tvResultat.setText("Sous le zéro absolu");
            return;
        }
        tvResultat.setText(formater(depuisCelsius(c, vers)) + " " + SYMBOLES[vers]);
    }

    // 0 = Celsius, 1 = Fahrenheit, 2 = Kelvin
    private double versCelsius(double v, int unite) {
        switch (unite) {
            case 1: return (v - 32) * 5 / 9;
            case 2: return v - 273.15;
            default: return v;
        }
    }

    private double depuisCelsius(double c, int unite) {
        switch (unite) {
            case 1: return c * 9 / 5 + 32;
            case 2: return c + 273.15;
            default: return c;
        }
    }

    private String formater(double d) {
        return new BigDecimal(d).setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros().toPlainString().replace('.', ',');
    }

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