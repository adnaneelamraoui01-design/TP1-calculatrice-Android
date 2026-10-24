
package com.example.s4;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    EditText etLogin, etPassword;
    String login;
    Button btnSIgnin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });

        // Initialisation des éléments
        etLogin = findViewById(R.id.etLogin);
        btnSIgnin = findViewById(R.id.btnSIgnin);


        // Condition
        btnSIgnin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login = etLogin.getText().toString().trim();

                if (login.isEmpty()) {
                    Toast.makeText(
                            MainActivity.this,
                            "Veuillez saisir votre email",
                            Toast.LENGTH_SHORT
                    ).show();

                } else {
                 startActivity(new Intent(MainActivity.this, MainActivity2.class));
                }
            }
        });
    }
}
