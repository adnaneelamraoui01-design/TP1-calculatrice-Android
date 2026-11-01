package com.example.s4;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Arrays;
import java.util.List;

public class ScientifiqueActivity extends AppCompatActivity {

    private static final List<String> FONCTIONS =
            Arrays.asList("sin", "cos", "tan", "asin", "acos", "atan", "ln", "log");
    private static final List<String> NECESSITE_OPERANDE =
            Arrays.asList("×", "÷", "xʸ", "n!", "%", "x²", "1/x");

    TextView tvExpression, tvResultat;
    Button btnDegRad;

    StringBuilder expr = new StringBuilder();
    boolean modeDegres = true;
    boolean resultatAffiche = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scientifique);
        setTitle(R.string.item2);

        View root = findViewById(R.id.Progect);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
            Insets bars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return windowInsets;
        });

        tvExpression = findViewById(R.id.tvExpression);
        tvResultat = findViewById(R.id.tvResultat);
        btnDegRad = findViewById(R.id.btnDegRad);

        int[] ids = {
                R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
                R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
                R.id.btnVirgule, R.id.btnPlus, R.id.btnMoins,
                R.id.btnMultiplication, R.id.btnDivision, R.id.btnEgale,
                R.id.btnC, R.id.btnEffacer, R.id.btnPourcentage,
                R.id.btnDegRad, R.id.btnPi, R.id.btnE,
                R.id.btnSin, R.id.btnCos, R.id.btnTan, R.id.btnLn, R.id.btnLog,
                R.id.btnXaucarre, R.id.btnPuissance, R.id.btcRacinecarre,
                R.id.btnParG, R.id.btnParD, R.id.btnFact, R.id.btnInverse
        };
        for (int id : ids) {
            findViewById(id).setOnClickListener(v ->
                    ajouter(((Button) v).getText().toString()));
        }
    }

    // ================= Saisie =================
    private void ajouter(String t) {
        boolean nouvelleSaisie = Character.isDigit(t.charAt(0)) || FONCTIONS.contains(t)
                || t.equals(",") || t.equals("(") || t.equals("π")
                || t.equals("e") || t.equals("√");

        // après un résultat : un chiffre/fonction démarre un nouveau calcul,
        // un opérateur continue avec le résultat
        if (resultatAffiche && nouvelleSaisie) expr.setLength(0);
        if (!t.equals("=")) resultatAffiche = false;

        // pas d'opérateur binaire sur une expression vide
        if (expr.length() == 0 && NECESSITE_OPERANDE.contains(t)) return;

        switch (t) {
            case "AC":
                expr.setLength(0);
                tvResultat.setText("0");
                break;
            case "⌫":
                effacerDernier();
                break;
            case "DEG":
            case "RAD":
                modeDegres = !modeDegres;
                btnDegRad.setText(modeDegres ? "DEG" : "RAD");
                break;
            case "=":
                calculerEtAfficher();
                return;
            case "x²":  expr.append("^2");     break;
            case "xʸ":  expr.append("^");      break;
            case "√":   expr.append("√(");     break;
            case "1/x": expr.append("^(-1)");  break;
            case "n!":  expr.append("!");      break;
            default:
                if (FONCTIONS.contains(t)) expr.append(t).append("(");
                else expr.append(t);
        }
        afficher();
    }

    private void effacerDernier() {
        int n = expr.length();
        if (n == 0) return;
        int i = n - 1;
        if (expr.charAt(i) == '(') {
            int j = i;
            while (j > 0 && Character.isLetter(expr.charAt(j - 1))) j--;
            if (i - j >= 2) i = j;                       // "sin(" disparaît en entier
            else if (i > 0 && expr.charAt(i - 1) == '√') i--;  // "√(" aussi
        }
        expr.delete(i, n);
    }

    private void afficher() {
        tvExpression.setText(expr.toString());
        if (expr.length() == 0) {
            tvResultat.setText("0");
            return;
        }
        try {   // aperçu en direct ; si l'expression est incomplète on garde l'ancien
            tvResultat.setText(formater(new Evaluateur(expr.toString()).evaluer()));
        } catch (RuntimeException ignore) { }
    }

    private void calculerEtAfficher() {
        String saisie = expr.toString();
        if (saisie.isEmpty()) return;
        try {
            String res = formater(new Evaluateur(saisie).evaluer());
            tvExpression.setText(saisie + " =");
            tvResultat.setText(res);
            expr.setLength(0);
            expr.append(res);
            resultatAffiche = true;
        } catch (ArithmeticException e) {
            Toast.makeText(this, e.getMessage() != null ? e.getMessage() : "Erreur",
                    Toast.LENGTH_SHORT).show();
        } catch (RuntimeException e) {
            Toast.makeText(this, "Expression invalide", Toast.LENGTH_SHORT).show();
        }
    }

    private String formater(double d) {
        if (Double.isNaN(d) || Double.isInfinite(d))
            throw new ArithmeticException("Résultat indéfini");
        if (Math.abs(d) < 1e-12) return "0";   // ex : sin(180°) = 1,2e-16
        BigDecimal bd = new BigDecimal(d).round(new MathContext(12)).stripTrailingZeros();
        return bd.toPlainString().replace('.', ',');
    }

    // ================= Évaluateur =================
    private class Evaluateur {
        final String s;
        int pos = 0;

        Evaluateur(String s) {
            this.s = s.replace(',', '.').replace('−', '-');
        }

        char peek() { return pos < s.length() ? s.charAt(pos) : '\0'; }

        boolean manger(char c) {
            if (peek() == c) { pos++; return true; }
            return false;
        }

        double evaluer() {
            double x = expression();
            if (pos < s.length()) throw new IllegalArgumentException("Syntaxe");
            return x;
        }

        // expression = terme { (+|-) terme }
        double expression() {
            double x = terme();
            while (true) {
                if (manger('+')) x += terme();
                else if (manger('-')) x -= terme();
                else return x;
            }
        }

        // terme = unaire { (×|÷) unaire | multiplication implicite }
        double terme() {
            double x = unaire();
            while (true) {
                if (manger('×') || manger('*')) x *= unaire();
                else if (manger('÷') || manger('/')) {
                    double d = unaire();
                    if (d == 0) throw new ArithmeticException("Division par zéro impossible");
                    x /= d;
                } else if (debutFacteur()) x *= unaire();
                else return x;
            }
        }

        boolean debutFacteur() {
            char c = peek();
            return c == '(' || c == 'π' || c == '√' || c == '.' || Character.isLetterOrDigit(c);
        }

        // unaire = (-|+) unaire | puissance     ( -2^2 = -4 )
        double unaire() {
            if (manger('-')) return -unaire();
            if (manger('+')) return unaire();
            return puissance();
        }

        // puissance = postfixe [ ^ unaire ]     ( associative à droite )
        double puissance() {
            double x = postfixe();
            if (manger('^')) x = Math.pow(x, unaire());
            return x;
        }

        // postfixe = primaire { ! | % }
        double postfixe() {
            double x = primaire();
            while (true) {
                if (manger('!')) x = factorielle(x);
                else if (manger('%')) x /= 100;
                else return x;
            }
        }

        double primaire() {
            int debut = pos;
            char c = peek();

            if (manger('(')) {
                double x = expression();
                manger(')');               // ')' manquante tolérée
                return x;
            }
            if (manger('π')) return Math.PI;
            if (manger('√')) {
                double x = primaire();
                if (x < 0) throw new ArithmeticException("Entrée invalide");
                return Math.sqrt(x);
            }
            if (Character.isDigit(c) || c == '.') {
                while (Character.isDigit(peek()) || peek() == '.') pos++;
                return Double.parseDouble(s.substring(debut, pos));
            }
            if (Character.isLetter(c)) {
                while (Character.isLetter(peek()) && peek() != 'π') pos++;
                String nom = s.substring(debut, pos);

                // "esin(" = e × sin(
                if (!nom.equals("e") && nom.startsWith("e") && !FONCTIONS.contains(nom)) {
                    pos = debut + 1;
                    return Math.E;
                }
                if (nom.equals("e")) return Math.E;

                if (!manger('(')) throw new IllegalArgumentException("Syntaxe");
                double x = expression();
                manger(')');
                return fonction(nom, x);
            }
            throw new IllegalArgumentException("Syntaxe");
        }

        double fonction(String nom, double x) {
            switch (nom) {
                case "sin": return Math.sin(enRadians(x));
                case "cos": return Math.cos(enRadians(x));
                case "tan":
                    if (Math.abs(Math.cos(enRadians(x))) < 1e-12)
                        throw new ArithmeticException("tan indéfinie");
                    return Math.tan(enRadians(x));
                case "asin":
                    if (x < -1 || x > 1) throw new ArithmeticException("Entrée invalide");
                    return deRadians(Math.asin(x));
                case "acos":
                    if (x < -1 || x > 1) throw new ArithmeticException("Entrée invalide");
                    return deRadians(Math.acos(x));
                case "atan": return deRadians(Math.atan(x));
                case "ln":
                    if (x <= 0) throw new ArithmeticException("Entrée invalide");
                    return Math.log(x);
                case "log":
                    if (x <= 0) throw new ArithmeticException("Entrée invalide");
                    return Math.log10(x);
            }
            throw new IllegalArgumentException("Fonction inconnue");
        }

        double enRadians(double x) { return modeDegres ? Math.toRadians(x) : x; }
        double deRadians(double x) { return modeDegres ? Math.toDegrees(x) : x; }

        double factorielle(double x) {
            if (x < 0 || x != Math.floor(x) || x > 170)
                throw new ArithmeticException("Entrée invalide");
            double r = 1;
            for (int i = 2; i <= (int) x; i++) r *= i;
            return r;
        }
    }

    // ================= Menu =================
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