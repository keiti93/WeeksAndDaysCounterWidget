package com.example.weeksanddayscounterwidget;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import yuku.ambilwarna.AmbilWarnaDialog; // Add this dependency in your build.gradle

import java.util.Calendar;

public class WidgetConfigureActivity extends Activity {

    int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    EditText nameInput;
    EditText dateEditText;
    Button colorButton;
    Button colorButtonDate;
    Button btnReady;

    int selectedColourName = Color.WHITE; // default color
    int selectedColourDate = Color.WHITE; // default color

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_widget);

        // Make the configure dialog bigger
        getWindow().setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 0.9),  // 90% of screen width
                (int) (getResources().getDisplayMetrics().heightPixels * 0.5) // 40% of screen height
        );

        setResult(RESULT_CANCELED);

        nameInput = findViewById(R.id.edit_name);
        dateEditText = findViewById(R.id.edit_date);
        colorButton = findViewById(R.id.btn_color_picker_name); // new button in layout
        colorButtonDate = findViewById(R.id.btn_color_picker_date); // new button in layout
        btnReady = findViewById(R.id.btn_ready);

        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) finish();

        // Load previously saved values
        SharedPreferences prefs = getSharedPreferences(CounterWidgetProvider.PREFS_NAME, MODE_PRIVATE);
        String savedName = prefs.getString("name_" + widgetId, "");
        String savedDate = prefs.getString("date_" + widgetId, "");
        String savedColorStr = prefs.getString("color_" + widgetId, "#FFD740");
        selectedColourName = Color.parseColor(savedColorStr);
        String savedDateColorStr = prefs.getString("colordate_" + widgetId, "#00EB9A");
        selectedColourDate = Color.parseColor(savedDateColorStr);

        nameInput.setText(savedName);
        dateEditText.setText(savedDate);
        colorButton.setBackgroundColor(selectedColourName);
        colorButtonDate.setBackgroundColor((selectedColourDate));

        // Date picker
        dateEditText.setOnClickListener(v -> {
            int day, month, year;

            if (!dateEditText.getText().toString().isEmpty()) {
                String[] parts = dateEditText.getText().toString().split("/");
                day = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]) - 1;
                year = Integer.parseInt(parts[2]);
            } else {
                Calendar c = Calendar.getInstance();
                day = c.get(Calendar.DAY_OF_MONTH);
                month = c.get(Calendar.MONTH);
                year = c.get(Calendar.YEAR);
            }

            DatePickerDialog dpd = new DatePickerDialog(this, (view, y, m, d) -> {
                String dateStr = d + "/" + (m + 1) + "/" + y;
                dateEditText.setText(dateStr);
            }, year, month, day);
            dpd.show();
        });

        // Color picker name
        colorButton.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, selectedColourName, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColourName = color;
                    colorButton.setBackgroundColor(selectedColourName);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            colorPicker.show();
        });

        // Color picker date
        colorButtonDate.setOnClickListener(v -> {
            AmbilWarnaDialog colorPicker = new AmbilWarnaDialog(this, selectedColourDate, new AmbilWarnaDialog.OnAmbilWarnaListener() {
                @Override
                public void onOk(AmbilWarnaDialog dialog, int color) {
                    selectedColourDate = color;
                    colorButtonDate.setBackgroundColor(selectedColourDate);
                }

                @Override
                public void onCancel(AmbilWarnaDialog dialog) { }
            });
            colorPicker.show();
        });

        btnReady.setOnClickListener(v -> {
            String name = nameInput.getText().toString();
            String dateStr = dateEditText.getText().toString();
            String colorHex = String.format("#%06X", (0xFFFFFF & selectedColourName));
            String colorDateHex = String.format("#%06X", (0xFFFFFF & selectedColourDate));

            SharedPreferences.Editor editor = getSharedPreferences(CounterWidgetProvider.PREFS_NAME, MODE_PRIVATE).edit();
            editor.putString("name_" + widgetId, name)
                    .putString("color_" + widgetId, colorHex)
                    .putString("date_" + widgetId, dateStr)
                    .putString("colordate_" + widgetId, colorDateHex)
                    .apply();

            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            CounterWidgetProvider.updateWidget(this, appWidgetManager, widgetId);

            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        });
    }
}
