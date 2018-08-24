package com.example.toandx.sms;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter {
    private final Activity context;
    private ArrayList<String> name;
    private ArrayList<String> info;
    public CustomListAdapter(Activity context,ArrayList<String> name,ArrayList<String> info)
    {
        super(context,R.layout.listview,name);
        this.context=context;
        this.name=name;
        this.info=info;
    }
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.listview, null,true);

        //this code gets references to objects in the listview_row.xml file
        TextView nameTextField = (TextView) rowView.findViewById(R.id.nameTextViewID);
        TextView infoTextField = (TextView) rowView.findViewById(R.id.infoTextViewID);

        //this code sets the values of the objects to values from the arrays
        nameTextField.setText(name.get(position));
        infoTextField.setText(info.get(position));
        return rowView;

    };
}
