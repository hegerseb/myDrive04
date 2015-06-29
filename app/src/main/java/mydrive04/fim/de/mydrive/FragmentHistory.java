package mydrive04.fim.de.mydrive;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;import mydrive03.hegerseb.fim.de.mydrive03.NonFrameworkDatabaseHelper;import mydrive03.hegerseb.fim.de.mydrive03.R;

/**
 * Created by dev on 02.06.2015.
 */
public class FragmentHistory extends Fragment {
    private Spinner selectDataType;
    // Expandable List View Objects
    private ExpandableListAdapter listAdapter;
    private ExpandableListView expListView;
    private String[] listDataHeader;
    private String[][] listDataChild;

    private TextView test;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootview = inflater.inflate(R.layout.fragment_history_layout, container, false);


        // Spinner to select different data in expandable list view
        selectDataType = (Spinner) rootview.findViewById(R.id.selectDataType);
        ArrayAdapter<CharSequence> dataAdapter;
        //= new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.spinnerlayout,list);
        dataAdapter = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.selectDataType, android.R.layout.simple_spinner_item);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectDataType.setAdapter(dataAdapter);
        selectDataType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                prepareListData((int) selectDataType.getSelectedItemId());
                listAdapter = new ExpandableListAdapter(listDataHeader, listDataChild);
                expListView.setAdapter(listAdapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        // Expandable List View
        // get the listview

        expListView = (ExpandableListView) rootview.findViewById(R.id.expandableListView);
        // preparing list data
        prepareListData(0); //id feedback = 0, refuel = 1
        listAdapter = new ExpandableListAdapter(listDataHeader, listDataChild);
        // setting list adapter
        expListView.setAdapter(listAdapter);


        return rootview;
    }

    public void actualizeFragment(){
        prepareListData((int) selectDataType.getSelectedItemId());
        listAdapter = new ExpandableListAdapter(listDataHeader, listDataChild);
        expListView.setAdapter(listAdapter);
        super.onResume();
    }

    private void prepareListData(int id) {

        NonFrameworkDatabaseHelper db = new NonFrameworkDatabaseHelper(getActivity().getApplicationContext());
        Cursor values;
        if (id == 1) {
            values = db.readRefuel();
        } else {
            values = db.readFeedback();
        }
        if (values.getCount() > 0) {
        listDataHeader = new String[values.getCount()];
        listDataChild = new String[values.getCount()][values.getColumnCount() - 2];

            if (id == 1) {
                for (int i = 0; i < values.getCount(); i++) {
                    listDataChild[i][0] = "Getankte Liter: ";
                    listDataChild[i][1] = "Gefahrene Kilometer: ";
                }
            } else {
                for (int i = 0; i < values.getCount(); i++) {
                    listDataChild[i][0] = "Feedback: ";
                    listDataChild[i][1] = "Liter/100km: ";
                }
            }

            if (values != null) {
                if (values.moveToFirst()) {
                    for (int i = 0; i < values.getCount(); i++) {

                        listDataHeader[i] = (String.valueOf(i + 1) + " - " + values.getString(1));

                        values.moveToNext();
                    }
                    values.moveToFirst();
                    for (int i = 0; i < values.getCount(); i++) {

                        for (int j = 2; j < values.getColumnCount(); j++) {
                            String s = values.getString(j);
                            listDataChild[i][j - 2] += s;
                        }
                        values.moveToNext();
                    }
                }
            }
        }else {
            listDataHeader = new String[1];
            listDataChild = new String[1][1];
            listDataHeader[0] = ("Keine Daten vorhanden");
            listDataChild[0][0] = ("Hier erscheinen die Daten nach der ersten Fahrt");
        }
    }

    public class ExpandableListAdapter extends BaseExpandableListAdapter {

        private final LayoutInflater inf;
        private String[] groups;
        private String[][] children;

        public ExpandableListAdapter(String[] groups, String[][] children) {
            this.groups = groups;
            this.children = children;
            inf = LayoutInflater.from(getActivity());
        }

        @Override
        public int getGroupCount() {
            return groups.length;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return children[groupPosition].length;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return groups[groupPosition];
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return children[groupPosition][childPosition];
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            ViewHolder holder;
            if (convertView == null) {
                convertView = inf.inflate(R.layout.list_item, parent, false);
                holder = new ViewHolder();

                holder.text = (TextView) convertView.findViewById(R.id.lblListItem);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getChild(groupPosition, childPosition).toString());

            return convertView;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = inf.inflate(R.layout.list_group, parent, false);

                holder = new ViewHolder();
                holder.text = (TextView) convertView.findViewById(R.id.lblListHeader);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.text.setText(getGroup(groupPosition).toString());

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }

        private class ViewHolder {
            TextView text;
        }
    }

}
