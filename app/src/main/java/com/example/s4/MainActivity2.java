package com.example.s4;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity2 extends AppCompatActivity {
    TextView textView;
    String message;
    EditText editText;

    boolean op;          // true : le prochain chiffre remplace l'affichage
    boolean attente;     // true :  pressé, 2e nombre pas encore saisi
    double val1, val2;
    String operateur = "";

    Button   btnPourcentage, btnCE, btnC, btnEffacer, btnInverse, btnXaucarre, btcRacinecarre, btnDivision, btn7, btn8, btn9, btnMultiplication, btn4, btn5, btn6, btnMoins, btn1, btn2, btn3, btnPlus, btnPlusMoins, btn0, btnVirgule, btnEgale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //  partie instantiation
        textView = findViewById(R.id.tvTextView);
        editText = findViewById(R.id.etTxt);
        btnPourcentage = findViewById(R.id.btnPourcentage);
        btnCE = findViewById(R.id.btnCE);
        btnC = findViewById(R.id.btnC);
        btnEffacer = findViewById(R.id.btnEffacer);
        btnInverse = findViewById(R.id.btnInverse);
        btnXaucarre = findViewById(R.id.btnXaucarre);
        btcRacinecarre = findViewById(R.id.btcRacinecarre);
        btnDivision = findViewById(R.id.btnDivision);
        btn7 = findViewById(R.id.btn7);
        btn8 = findViewById(R.id.btn8);
        btn9 = findViewById(R.id.btn9);
        btnMultiplication = findViewById(R.id.btnMultiplication);
        btn4 = findViewById(R.id.btn4);
        btn5 = findViewById(R.id.btn5);
        btn6 = findViewById(R.id.btn6);
        btnMoins = findViewById(R.id.btnMoins);
        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);
        btnPlus = findViewById(R.id.btnPlus);
        btnPlusMoins = findViewById(R.id.btnPlusMoins);
        btn0 = findViewById(R.id.btn0);
        btnVirgule = findViewById(R.id.btnVirgule);
        btnEgale = findViewById(R.id.btnEgale);

        textView.setText("Bienvenue dans MA Calculatrice");
        op = false;
        attente = false;

        //  chiffres//
        btn0.setOnClickListener(v->saisir("0"));
        btn1.setOnClickListener(v -> saisir("1"));
        btn2.setOnClickListener(v -> saisir("2"));
        btn3.setOnClickListener(v -> saisir("3"));
        btn4.setOnClickListener(v -> saisir("4"));
        btn5.setOnClickListener(v -> saisir("5"));
        btn6.setOnClickListener(v -> saisir("6"));
        btn7.setOnClickListener(v -> saisir("7"));
        btn8.setOnClickListener(v -> saisir("8"));
        btn9.setOnClickListener(v -> saisir("9"));

        // ===== virgule =====
        btnVirgule.setOnClickListener(v -> {
            String txt = editText.getText().toString();
            if (op) {// après un opérateur ou un résultat
                editText.setText("0,");//on démarre un nouveau nombre "0,"
                op = false;
                attente = false;
            } else if (!txt.contains(",")) { // une seule virgule autorisée
                editText.setText(txt + ",");
            }
        });

        // ===== opérateurs =====
        btnPlus.setOnClickListener(v -> choisirOperateur("+"));
        btnMoins.setOnClickListener(v -> choisirOperateur("-"));
        btnMultiplication.setOnClickListener(v -> choisirOperateur("x"));
        btnDivision.setOnClickListener(v -> choisirOperateur("÷"));

        // ===== égal =====
        btnEgale.setOnClickListener(v -> {
            if (operateur.isEmpty()) return;
            val2 = lire();
            if (operateur.equals("÷") && val2 == 0) {
                erreurDivisionZero();
                return;
            }
            double resultat = calculer(val1, val2, operateur);
            message = formater(val1) + " " + operateur + " " + formater(val2) + " =";
            textView.setText(message);
            editText.setText(formater(resultat));
            val1 = resultat;
            operateur = "";
            op = true;
            attente = false;
        });

        // ===== effacement =====
        btnC.setOnClickListener(v -> toutEffacer());

        btnCE.setOnClickListener(v -> {
            editText.setText("0");
            op = false;
        });

        btnEffacer.setOnClickListener(v -> {
            if (op) return; // on n'efface pas un résultat
            String t = editText.getText().toString();
            if (t.length() > 1) {
                t = t.substring(0, t.length() - 1);
                if (t.equals("-")) t = "0";
            } else {
                t = "0";
            }
            editText.setText(t);
        });

        // ===== +/- =====
        btnPlusMoins.setOnClickListener(v -> {
            String t = editText.getText().toString();
            if (t.equals("0")) return;
            if (t.startsWith("-")) {
                editText.setText(t.substring(1));
            } else {
                editText.setText("-" + t);
            }
        });

        // ===== % =====
        btnPourcentage.setOnClickListener(v -> {
            double valeur = lire();
            double r = operateur.isEmpty() ? 0 : val1 * valeur / 100;
            String debut = operateur.isEmpty() ? "" : formater(val1) + " " + operateur + " ";
            textView.setText(debut + formater(r));
            editText.setText(formater(r));
            op = true;
            attente = false;
        });

        // ===== x², √x, 1/x =====
        btnXaucarre.setOnClickListener(v -> {
            double valeur = lire();
            afficherUnaire("sqr", valeur, valeur * valeur);
        });

        btcRacinecarre.setOnClickListener(v -> {
            double valeur = lire();
            if (valeur < 0) {
                Toast.makeText(MainActivity2.this, "Entrée invalide", Toast.LENGTH_SHORT).show();
                return;
            }
            afficherUnaire("√", valeur, Math.sqrt(valeur));
        });

        btnInverse.setOnClickListener(v -> {
            double valeur = lire();
            if (valeur == 0) {
                erreurDivisionZero();
                return;
            }
            afficherUnaire("1/", valeur, 1 / valeur);
        });
    }

    // ================= méthodes utilitaires =================

    // Lit le nombre affiché (la virgule devient un point pour Java)
    private double lire() {
        String s = editText.getText().toString().replace(",", ".");
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    // Affiche un nombre proprement : 5.0 -> "5", 2.5 -> "2,5"
    private String formater(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d)) return "0";
        BigDecimal bd = new BigDecimal(d).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString().replace(".", ",");
    }

    // Ajoute un chiffre à l'affichage
    private void saisir(String chiffre) {
        String txt = editText.getText().toString();
        if (op || txt.equals("0")) {
            editText.setText(chiffre);
        } else {
            editText.setText(txt + chiffre);
        }
        op = false;
        attente = false;
    }

    // Calcul de base
    private double calculer(double a, double b, String o) {
        switch (o) {
            case "+": return a + b;
            case "-": return a - b;
            case "x": return a * b;
            case "÷": return a / b;
        }
        return b;
    }

    // Gère l'appui sur + - x ÷ (avec calcul en chaîne : 2 + 3 + ...)
    private void choisirOperateur(String o) {
        if (!operateur.isEmpty() && !attente) {
            double valeur = lire();
            if (operateur.equals("÷") && valeur == 0) {
                erreurDivisionZero();
                return;
            }
            val1 = calculer(val1, valeur, operateur);
            editText.setText(formater(val1));
        } else {
            val1 = lire();
        }
        operateur = o;
        attente = true;
        op = true;
        message = formater(val1) + " " + o + " ";
        textView.setText(message);
    }

    // Affiche le résultat d'une fonction à un seul nombre (x², √x, 1/x)
    private void afficherUnaire(String nom, double valeur, double resultat) {
        String debut = operateur.isEmpty() ? "" : formater(val1) + " " + operateur + " ";
        textView.setText(debut + nom + "(" + formater(valeur) + ")");
        editText.setText(formater(resultat));
        op = true;
        attente = false;
    }

    private void erreurDivisionZero() {
        Toast.makeText(MainActivity2.this,
                "Division par zéro impossible", Toast.LENGTH_SHORT).show();
        toutEffacer();
    }

    private void toutEffacer() {
        val1 = 0;
        val2 = 0;
        operateur = "";
        message = "";
        op = false;
        attente = false;
        editText.setText("0");
        textView.setText("Calculatrice");
    }
}