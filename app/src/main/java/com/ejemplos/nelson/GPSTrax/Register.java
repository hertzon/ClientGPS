package com.ejemplos.nelson.GPSTrax;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends Activity {
    EditText editText_correoElectronico;
    EditText editText_usuario;
    EditText editText_contrasena;
    EditText editText_nombreEquipo;
    Button button_registrarse;
    String strCorreo=null;
    String strUsuario=null;
    String strContrasena=null;
    String strNombreEquipo=null;
    String imei=null;
    private static final String TAG = "GPSTrax";
    boolean done=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        editText_correoElectronico=(EditText)findViewById(R.id.editText_correoElectronico);
        editText_usuario=(EditText)findViewById(R.id.editText_usuario);
        editText_contrasena=(EditText)findViewById(R.id.editText_contrasena);
        editText_nombreEquipo=(EditText)findViewById(R.id.editText_nombreEquipo);
        button_registrarse=(Button)findViewById(R.id.button_registrarse);

        TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        imei = tm.getDeviceId();

        button_registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                strCorreo="nelsonhenge@hotmail.com";
//                strUsuario="nelsonj";
//                strContrasena="123456";
//                strNombreEquipo="movil1";
//                loginServer conexion=new loginServer();
//                conexion.execute();

                boolean error=false;
                strCorreo=editText_correoElectronico.getText().toString().trim();
                strUsuario=editText_usuario.getText().toString().trim();
                strContrasena=editText_contrasena.getText().toString().trim();
                strNombreEquipo=editText_nombreEquipo.getText().toString().trim();
                Log.d(TAG,"strNombreEquipo: "+strNombreEquipo);
                Log.d(TAG,"Correo: "+strCorreo);
                Log.d(TAG,"strUsuario: "+strUsuario);
                Log.d(TAG,"strContrasena: "+strContrasena);
//                if (hasSpaces(strCorreo)){
//                    error=true;
//                    editText_correoElectronico.setError("Correo electronico no puede tener espacios!!");
//                    editText_correoElectronico.setText("");
//                    editText_correoElectronico.setHint("Correo electronico");
//                }
//                if (TextUtils.isEmpty(strCorreo)){
//                    error=true;
//                    editText_correoElectronico.setError("Ingrese un correo electronico valido!!");
//                }

                if (revisarCaracteresEspeciales(strNombreEquipo)){
                    error=true;
                    editText_nombreEquipo.setError("Nombre no puede llevar tildes ni caracteres especiales");
                }

                if (TextUtils.isEmpty(strNombreEquipo)){
                    error=true;
                    editText_nombreEquipo.setError("Ingrese un nombre de equipo!!");
                }

                if (isEmailValid(strCorreo)){
                    Log.d(TAG,"Correo valido");

                }else {
                    error=true;
                    editText_correoElectronico.setError("Ingrese un correo electrónico valido!!");
                }


                if (hasSpaces(strUsuario)){
                    error=true;
                    editText_usuario.setError("Usuario no puede tener espacios!!");
                    editText_usuario.setText("");
                    editText_usuario.setHint("Usuario");
                }
                if (TextUtils.isEmpty(strUsuario)){
                    error=true;
                    editText_usuario.setError("Ingrese un usuario!!");
                }
                if (revisarCaracteresEspeciales(strUsuario)){
                    error=true;
                    editText_usuario.setError("Usuario no puede llevar tildes ni caracteres especiales");
                }




                if (hasSpaces(strContrasena)){
                    error=true;
                    editText_contrasena.setError("Contraseña no puede tener espacios!!");
                    editText_contrasena.setText("");
                    editText_contrasena.setHint("Contraseña");
                }
                if (TextUtils.isEmpty(strContrasena)){
                    error=true;
                    editText_contrasena.setError("Ingrese una contraseña");
                }
                if (revisarCaracteresEspeciales(strContrasena)){
                    error=true;
                    editText_contrasena.setError("Usuario no puede llevar tildes ni caracteres especiales");
                }




                if (!error){
                    Log.d(TAG,"no hay error en datos de ingreso");
                    //Arrancamos dialog box de confirmacion de datos
                    AlertDialog.Builder ab=new AlertDialog.Builder(Register.this);
                    ab.setMessage("Desea registrarse con estos datos?\n" +
                            "Nombre equipo: "+strNombreEquipo+"\n" +
                            "Cuenta: "+strCorreo+"\n" +
                            "Usuario: "+strUsuario+"\n" +
                            "Contraseña: "+strContrasena);
                    ab.setPositiveButton("Si", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Arrancamos webservice de registro:
                            Log.d(TAG,"Arrancando webservice....");
                            loginServer conexion=new loginServer();
                            conexion.execute();

//                            AlertDialog.Builder ab1=new AlertDialog.Builder(Register.this);
//                            ab1.setMessage("Registro exitoso, revise los datos de acceso en su correo electronico!");
//                            ab1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialogInterface, int i) {
//                                    Toast.makeText(Register.this, "Registro exitoso, revise su correo electronico!", Toast.LENGTH_SHORT).show();
//                            if (done){
//                                finish();
//                            }
//
//
//                                }
//                            });

                            //Grabamos registroHecho=true
                            Log.d(TAG,"Done: "+done);
                            if (done){

                            }else{

                            }



                        }
                    });
                    ab.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    AlertDialog ad=ab.create();
                    ad.show();
                }else {
                    Log.d(TAG,"Error en datos de acceso!!");
                }



            }
        });




    }
    private boolean hasSpaces(String str) {
        return ((str.split(" ").length > 1) ? true : false);
    }

    public static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }


    private class loginServer extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog pd;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG,"onPreExecute");
            pd = ProgressDialog.show(Register.this, "Estado Conexion Servidor", "Conectando...");
        }
        @Override
        protected Boolean doInBackground(Void... voids) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("correoCuenta", strCorreo);
                jsonObject.put("usuario", strUsuario);
                jsonObject.put("clave", strContrasena);
                jsonObject.put("imei", imei);
                jsonObject.put("nombreEquipo", strNombreEquipo);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                nameValuePairs.add(new BasicNameValuePair("json", jsonObject.toString()));
                Log.d(TAG,"Enviando peticion a servidor.....");
                //String response = makePOSTRequest("http://104.236.203.72/WebserviceGpsTrax.php", nameValuePairs );
                String response = makePOSTRequest("http://138.197.20.62/WebserviceGpsTrax.php", nameValuePairs );




            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            Log.d(TAG,"onPostExecute");
            super.onPostExecute(aBoolean);
            if (pd.isShowing()) {
                pd.dismiss();
            }
            Log.d(TAG,"Done en onPostExecute: "+done);
            if (done){
                Log.d(TAG,"Guardando datos en sharedPreferences");
                SharedPreferences sharedPreferences = getApplicationContext().getSharedPreferences("com.websmithing.gpstracker.prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("registroHecho", true);
                editor.putString("imei", imei);
                editor.putString("nombreEquipo",strNombreEquipo);
                editor.putString("correoCuenta",strCorreo);
                editor.putString("usuario",strUsuario);
                editor.putString("clave",strContrasena);
                editor.apply();

                AlertDialog.Builder ab1=new AlertDialog.Builder(Register.this);
                ab1.setMessage("Registro exitoso, revise los datos de acceso en su correo electronico!");
                ab1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(Register.this, "Registro exitoso, revise su correo electronico!", Toast.LENGTH_SHORT).show();
                        finish();


                    }
                });
                AlertDialog ad=ab1.create();
                ad.show();




            }
            //Toast.makeText(getApplicationContext(),"Registro exitoso, revise en su correo los datos de acceso!!!!",Toast.LENGTH_LONG).show();
        }


    }

    private String makePOSTRequest(String url, List<NameValuePair> nameValuePairs) {
        String response = "";
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
            try {
                HttpResponse httpResponse = httpClient.execute(httpPost);
                String jsonResult = inputStreamToString(httpResponse.getEntity().getContent()).toString();
                JSONObject object = null;
                Log.d(TAG, "Response:" + jsonResult);
                object = new JSONObject(jsonResult);
                done = object.getBoolean("done");
//                try {
//                    object = new JSONObject(jsonResult);
//                    String estadoLogin = object.getString("rta");
//
//                    Log.d(TAG, "Respuesta Server Login: " + estadoLogin);
//                    response=estadoLogin;
//
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                    Log.d(TAG, "Error makePOSTRequest: " + e);
//                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return response;

    }

    public boolean revisarCaracteresEspeciales(String x){
        boolean hasSpecial=false;
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(x);
        hasSpecial = m.find();
        return  hasSpecial;
    }

    private StringBuilder  inputStreamToString(InputStream content) {
        String rLine = "";
        StringBuilder answer = new StringBuilder();
        BufferedReader rd = new BufferedReader(new InputStreamReader(content));
        try
        {
            while ((rLine = rd.readLine()) != null)
            {
                answer.append(rLine);
            }
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
        return answer;
    }
}
