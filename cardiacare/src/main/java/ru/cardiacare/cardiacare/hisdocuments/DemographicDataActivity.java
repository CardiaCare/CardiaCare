package ru.cardiacare.cardiacare.hisdocuments;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

import com.petrsu.cardiacare.smartcare.hisdocuments.DemographicData;

import ru.cardiacare.cardiacare.R;

/* Экран "Демографические данные" */

public class DemographicDataActivity extends AppCompatActivity {

    DemographicData dd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_demographic_data);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        assert toolbar != null;
        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        dd = new DemographicData("name", "surname", "patronymic", "birthDate", "sex",
                "residence", "contactInformation");

        EditText etName = (EditText) findViewById(R.id.etName);
        assert etName != null;
        etName.setText(dd.getPatientName());
        EditText etSurname = (EditText) findViewById(R.id.etSurname);
        assert etSurname != null;
        etSurname.setText(dd.getSurname());
        EditText etPatronymic = (EditText) findViewById(R.id.etPatronymic);
        assert etPatronymic != null;
        etPatronymic.setText(dd.getPatronymic());
        EditText etBirthDate = (EditText) findViewById(R.id.etBirthDate);
        assert etBirthDate != null;
        etBirthDate.setText(dd.getBirthDate());
        EditText etSex = (EditText) findViewById(R.id.etSex);
        assert etSex != null;
        etSex.setText(dd.getSex());
        EditText etResidence = (EditText) findViewById(R.id.etResidence);
        assert etResidence != null;
        etResidence.setText(dd.getResidence());
        EditText etContactInformation = (EditText) findViewById(R.id.etContactInformation);
        assert etContactInformation != null;
        etContactInformation.setText(dd.getContactInformation());
    }
}
