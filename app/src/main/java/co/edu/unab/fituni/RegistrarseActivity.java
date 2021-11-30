package co.edu.unab.fituni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import co.edu.unab.fituni.modelo.Persona;
import co.edu.unab.fituni.network.MyBackendAPIClient;
import co.edu.unab.fituni.pojo.Authorization;
import co.edu.unab.fituni.repository.PersonaRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrarseActivity extends AppCompatActivity {
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy KK:mm:ss a", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrarse);

        EditText txtNombre = findViewById(R.id.textNombreReg);
        EditText txtApellido = findViewById(R.id.textApellidoReg);
        EditText editEmail = findViewById(R.id.editTxtEmailReg);
        Switch swAdmin = findViewById(R.id.switchAdmin);

        Button botonEnviar = findViewById(R.id.buttonEnviarReg);

        botonEnviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fecha =  dateFormat.format(Calendar.getInstance().getTime()); //obtiene fecha y hora actual
                Persona personaDatos = new Persona();
                personaDatos.setNombre(txtNombre.getText().toString().trim());
                personaDatos.setApellido(txtApellido.getText().toString().trim());
                personaDatos.setEmail(editEmail.getText().toString().toLowerCase().trim());
                personaDatos.setFechaRegistro(fecha);
                if(swAdmin.isChecked())
                    personaDatos.setEsAdmin(true);
                else
                    personaDatos.setEsAdmin(false);

                PersonaRepository personaRepo = MyBackendAPIClient.getRetrofit().create(PersonaRepository.class);

                //obtinen token de la cache android
                SharedPreferences sharedPrefe = getPreferences(MODE_PRIVATE);
                String token = MainActivity.getBearerToken();//sharedPrefe.getString("token", "");

                Call<Persona> persona = personaRepo.createPersona(personaDatos, token);
                persona.enqueue(new Callback<Persona>() {
                    @Override
                    public void onResponse(Call<Persona> call, Response<Persona> response) {
                        Log.d("Exito", "Se registro Persona Correctamente");
                        Toast.makeText(RegistrarseActivity.this, "Registro éxitoso!, ahora inicie sesion con el correo", Toast.LENGTH_SHORT);
                        finish();
                    }

                    @Override
                    public void onFailure(Call<Persona> call, Throwable t) {
                        Log.d("Fallo", "Fallo al agregar registro de Persona", t);
                        txtNombre.setText("");
                        txtApellido.setText("");
                        editEmail.setText("");
                        Toast.makeText(RegistrarseActivity.this, "Ocurrio error al registrarse, por favor vuelva a intentarlo", Toast.LENGTH_SHORT);
                    }
                });
            }
        });

    }
}