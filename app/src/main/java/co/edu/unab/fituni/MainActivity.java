package co.edu.unab.fituni;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import co.edu.unab.fituni.modelo.IndiceMasaMuscular;
import co.edu.unab.fituni.modelo.Persona;
import co.edu.unab.fituni.modelo.Usuario;
import co.edu.unab.fituni.network.MyBackendAPIClient;
import co.edu.unab.fituni.pojo.Authorization;
import co.edu.unab.fituni.repository.GetAuthorization;
import co.edu.unab.fituni.repository.IndiceMasaMuscularRepository;
import co.edu.unab.fituni.repository.PersonaRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private EditText estaturaText;
    private EditText pesoText;
    private static Double imc;
    private ArrayList<IndiceMasaMuscular> listaReg;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, d MMM yyyy KK:mm:ss a", Locale.US);
    static String correoPersona, bearerToken;
    static Persona userPersona;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Obtinen TOKEN BEARER
        GetAuthorization authRepo = MyBackendAPIClient.getRetrofit().create(GetAuthorization.class);

        //ATENCIÓN: creo las credenciales usuario y contrasenia que deben existir previamente en la tabla usuario
        // de la base de datos para poder obtener token
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPassword("admin");
        Call<Authorization> auth = authRepo.getAuthorization(usuario);
        auth.enqueue(new Callback<Authorization>() {
            @Override
            public void onResponse(Call<Authorization> call, Response<Authorization> response) {
                bearerToken = response.body().getToken(); //obtiene token Bearer

                //guardo en cache el token para para solicitarlo desde otras actividades
                SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("token", bearerToken); //pares de valores
                editor.commit(); //aqui lo guarda ya en cache
                Log.d("Exito", "Se obtuvo corectamente el Token "+bearerToken);
            }

            @Override
            public void onFailure(Call<Authorization> call, Throwable t) {
                Log.d("Fallo", "Fallo obtencion del Bearer Token", t);
            }
        });

        listaReg = new ArrayList<>(); //crea lista donde se añadiran los registros
        estaturaText = (EditText) findViewById(R.id.editTextNumber);
        pesoText = (EditText) findViewById(R.id.editTextNumber2);

        Button botonCalcular = (Button) findViewById(R.id.button2);
        Button botonRegistros = (Button) findViewById(R.id.button3);
        Button botonRegistrarse = findViewById(R.id.buttonRegistrarse);

        EditText campoEmail = findViewById(R.id.editTextEmailAddress);
        ImageButton imageButtonSesion = findViewById(R.id.imageButton);
        ImageButton imageButtonSalir = findViewById(R.id.imageButtonSalir);
        imageButtonSalir.setVisibility(View.INVISIBLE);
/*      //una forma de que guarde en correo del usuario para iniciar sin tener que reingresar (no implementado del todo)
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        correoPersona = sharedPref.getString("correo", null);
        if(correoPersona!=null){
            PersonaRepository personaRepo = MyBackendAPIClient.getRetrofit().create(PersonaRepository.class);
            Call<Persona> personaSesion = personaRepo.readPersona(correoPersona, bearerToken);
            personaSesion.enqueue(new Callback<Persona>() {
                @Override
                public void onResponse(Call<Persona> call, Response<Persona> response) {
                    userPersona = response.body();
                    Log.d("Exito", "Usuario con sesión activa");
                    campoEmail.setEnabled(false);
                }

                @Override
                public void onFailure(Call<Persona> call, Throwable t) {
                    Log.d("Fallo", "Fallo al obtener datos del usuario", t);
                    campoEmail.setText("");
                    Toast.makeText(MainActivity.this, "Correo no esta registrado!!!, por favor registrese", Toast.LENGTH_SHORT);
                }
            });
            campoEmail.setText(correoPersona);
            campoEmail.setEnabled(false);
        }*/


        botonCalcular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imcCalcular();
               Intent intentResultado = new Intent(MainActivity.this, ResultadoImc.class);
               startActivity(intentResultado);
               estaturaText.setText(""); //limpia el campo estatura
               pesoText.setText(""); //limpia el campo peso
               estaturaText.requestFocus(); //posiciona focus del cursor en campo estatura
            }
        });

        botonRegistros.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userPersona!=null) { //verifica si existe un usuario logueado antes de mostrar registros
                    Intent intent = new Intent(MainActivity.this, RegistroActivity.class);
                    intent.putParcelableArrayListExtra("datosLista", listaReg);
                    //intent.putExtras(savedInstanceState);
                    startActivity(intent);
                }
                else
                    Log.d("Fallo", "Debe inciar sesion antes para consultar registros");
            }
        });

        //al hacer click me envia a al formulario de registro de Persona
        botonRegistrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegistrarseActivity.class);
                startActivity(intent);
            }
        });

        imageButtonSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //String correo="";
                PersonaRepository personaRepo = MyBackendAPIClient.getRetrofit().create(PersonaRepository.class);
                correoPersona = campoEmail.getText().toString().toLowerCase().trim();
                //Guarda correo en cache para futuro
                SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("correo", correoPersona); //pares de valores
                editor.commit(); //aqui lo guarda ya en cache

                Call<Persona> personaSesion = personaRepo.readPersona(correoPersona, bearerToken);
                personaSesion.enqueue(new Callback<Persona>() {
                    @Override
                    public void onResponse(Call<Persona> call, Response<Persona> response) {
                        userPersona = response.body();
                        Log.d("Exito", "Usuario con sesión activa");
                        campoEmail.setEnabled(false);
                        imageButtonSesion.setVisibility(View.GONE);
                        imageButtonSalir.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(Call<Persona> call, Throwable t) {
                        Log.d("Fallo", "Fallo al obtener datos del usuario", t);
                        campoEmail.setText("");
                        Toast.makeText(MainActivity.this, "Correo no esta registrado!!!, por favor registrese", Toast.LENGTH_SHORT);
                    }
                });

            }
        });

        imageButtonSalir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                correoPersona=""; //reinicia correo del usuario a vacio
                userPersona=null; //reincia usuario a null
                campoEmail.setEnabled(true);
                campoEmail.setText("");
                imageButtonSalir.setVisibility(View.GONE);
                imageButtonSesion.setVisibility(View.VISIBLE);
                Log.d("Exito", "sesión terminada por usuario");
                Toast.makeText(MainActivity.this, "Sesión terminada", Toast.LENGTH_LONG);
            }
        });
    }


    //metodo calcula IMC y añade los datos a la lista de registro
    private void imcCalcular(){
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        Double peso=0.0;
        Double estatura=0.0;
        //Date fecha=null; //inicializa fecha

        estatura = Double.parseDouble(estaturaText.getText().toString().trim())/100;
        peso = Double.parseDouble(pesoText.getText().toString().trim());
        imc = peso/(Math.pow(estatura, 2));
        imc = Math.round(imc*10.0)/10.0; //redondea a un solo decimal
        //String imcFormated = decimalFormat.format(imc)  ;
        //Double imcD = Double.parseDouble(imcFormated);
        //try {
         String fecha =  dateFormat.format(Calendar.getInstance().getTime()); //obtiene fecha y hora actual
        //} catch (ParseException e) {
         //   e.printStackTrace();
        //}
        IndiceMasaMuscular datosImc = new IndiceMasaMuscular(estatura, peso, imc, fecha, userPersona);
        IndiceMasaMuscularRepository imcRepo = MyBackendAPIClient.getRetrofit().create(IndiceMasaMuscularRepository.class);
        Call<IndiceMasaMuscular> registro = imcRepo.createRegistro(datosImc, bearerToken); //agrega registro a la base de datos
        registro.enqueue(new Callback<IndiceMasaMuscular>() {
            @Override
            public void onResponse(Call<IndiceMasaMuscular> call, Response<IndiceMasaMuscular> response) {
                Log.d("Exito", "se agrego registro de imc a la base de datos");
            }

            @Override
            public void onFailure(Call<IndiceMasaMuscular> call, Throwable t) {
                Log.d("Fallo", "no se agrego registro de imc a la base de datos", t);
            }
        });
        listaReg.add(new IndiceMasaMuscular(estatura, peso, imc , fecha, userPersona)); //añade registro nuevo a lista
    }

    public static Double getImc() {
        return imc;
    }

    public static String getBearerToken() {
        return bearerToken;
    }

    public static String getCorreoPersona() {
        return correoPersona;
    }

    public static Persona getUserPersona() {
        return userPersona;
    }
}