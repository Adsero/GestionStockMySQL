package com.example.marouane.gestionstockmysql;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import Models.Annonceur;

public class RegisterActivity extends AppCompatActivity {

    EditText name, lastName, login, pwd, pwd2nd;
    Button validate, pickDate;

    Annonceur annonceur;
    String birthDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
//        getSupportActionBar().hide();

        name = findViewById(R.id.name);
        lastName = findViewById(R.id.lastName);
        login = findViewById(R.id.login);
        pwd = findViewById(R.id.pass);
        pwd2nd = findViewById(R.id.pass2ndTime);

        validate = findViewById(R.id.btnAddUser);
        pickDate = findViewById(R.id.pickDate);


        pickDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                int day = cal.get(Calendar.DAY_OF_MONTH);
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);

                DatePickerDialog datePicker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                        birthDate = year + "-" + (++month) + "-" + day;
                        Toast.makeText(RegisterActivity.this, birthDate, Toast.LENGTH_LONG).show();
                    }
                }, day, month, year);
                datePicker.show();
            }
        });

        validate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!name.getText().toString().equals("") &&
                        !lastName.getText().toString().equals("") &&
                        !pwd.getText().toString().equals("") &&
                        !birthDate.toString().equals("") &&
                        !pwd2nd.getText().toString().equals("")) {
                    try {
                        annonceur = new Annonceur(123, name.getText().toString().trim(), lastName.getText().toString().trim(), new SimpleDateFormat("yyyy-MM-dd").parse(birthDate), login.getText().toString().trim(), pwd.getText().toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    if (pwd.getText().toString().equals(pwd2nd.getText().toString())) {
                        ClientConnectClass con = new ClientConnectClass("loginCheck");
                        con.execute(
                                LoginActivity.serverIP + "/Annonceur/getUserByLoginAndPass.php?login=" + annonceur.getLogin() + "&pass=" + annonceur.getPass());
                        System.out.println(LoginActivity.serverIP + "/Annonceur/getUserByLoginAndPass.php?login=" + annonceur.getLogin() + "&pass=" + annonceur.getPass());
                    } else {
                        Toast.makeText(RegisterActivity.this, "La confirmation de votre pass a été échoué !", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Vous devez remplir tout les champs !", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    class ClientConnectClass extends AsyncTask<String, Integer, String> {

        private ArrayList<Annonceur> allRecords = new ArrayList<>();
        ProgressDialog pDialog;
        String choice;

        ClientConnectClass(String choice) {
            this.choice = choice;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(RegisterActivity.this);
            pDialog.setMessage("Chargement...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            // Convert data to a list of clients
            try {
                JSONArray all = new JSONArray(s);
                for (int i = 0; i < all.length(); i++) {
                    JSONObject object = all.getJSONObject(i);
                    allRecords.add(new Annonceur(object.getInt("id"), object.getString("nom"), object.getString("prenom"), new SimpleDateFormat("yyyy-MM-dd").parse(object.getString("date_naiss")), object.getString("login"), object.getString("pass")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Toast.makeText(RegisterActivity.this, "Erreur >> " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
            pDialog.dismiss();

            switch (choice) {
                case "insertUser":
//                    Toast.makeText(RegisterActivity.this, "Félicitations vous avez maintenant un compte avec nous !", Toast.LENGTH_LONG).show();

                    // Assign the inserted primary_key as we're already in the insertUser choice.
                    annonceur.setIdAnnonceur(Integer.parseInt(s));
                    Intent intent = new Intent();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("owner", annonceur);
                    intent.putExtras(bundle);
                    setResult(RESULT_OK, intent);
                    finish();
                    break;
                case "loginCheck":
                    Toast.makeText(RegisterActivity.this, "Checking if the user already exists...", Toast.LENGTH_LONG).show();
                    handleTheRegisterStuff(allRecords);
                    break;
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... strings) {
            String returnedValue = null;
            StringBuffer buffer = new StringBuffer();
            try {
                URL url = new URL(strings[0]); //Use the 1st String cause it is the URL needed
                HttpURLConnection con = (HttpURLConnection) url.openConnection();

                InputStream inputStream = con.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                String line = null;

                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line);
                }
                returnedValue = buffer.toString();
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
            System.out.println("From the server >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + returnedValue);
            return returnedValue;
        }

    }

    public void handleTheRegisterStuff(ArrayList<Annonceur> allRecords) {
        try {
            // if an IndexOutOfBoundsException occurs, it means that annonceur doesn't exist in the DB
            allRecords.get(0);

            AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
            builder.setTitle("Information")
                    .setMessage("Ces coordonnées existe déjà !!")
                    .setPositiveButton("Je comprend !", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();

        } catch (NullPointerException ex) {
            Toast.makeText(RegisterActivity.this, "Error >>>>>" + ex.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IndexOutOfBoundsException exi) {
            Calendar gregorianCal = new GregorianCalendar();
            gregorianCal.setTime(annonceur.getDate_naiss());

            new ClientConnectClass("insertUser").execute(
                    LoginActivity.serverIP + "/Annonceur/add.php?id=" + annonceur.getIdAnnonceur() +
                            "&name=" + annonceur.getNom() +
                            "&lastname=" + annonceur.getPrenom() +
                            "&login=" + annonceur.getLogin() +
                            "&pass=" + annonceur.getPass() +
                            "&date_naiss=" + gregorianCal.get(Calendar.YEAR) + "-" + (gregorianCal.get(Calendar.MONTH) + 1) + "-" + gregorianCal.get(Calendar.DAY_OF_MONTH));
        }
    }
}
