package Adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import Objects.GardenClass;

/**
 * Custom ArrayAdapter for displaying a list of GardenClass objects in a Spinner.
 */
public class CourseArrayAdapter extends ArrayAdapter<GardenClass> {

    /**
     * Constructor for the CourseArrayAdapter.
     *
     * @param context The context in which the ArrayAdapter is running.
     * @param courses The list of GardenClass objects to display.
     */
    public CourseArrayAdapter(Context context, List<GardenClass> courses) {
        super(context, android.R.layout.simple_spinner_item, courses);
    }

    /**
     * Provides a view for the selected item in the Spinner.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A view for the selected item.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        GardenClass course = getItem(position);
        if (course != null) {
            textView.setText(course.getCourseNumber());
        }
        return view;
    }

    /**
     * Provides a view for a dropdown item in the Spinner.
     *
     * @param position The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent The parent that this view will eventually be attached to.
     * @return A view for the dropdown item.
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getDropDownView(position, convertView, parent);
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        GardenClass course = getItem(position);
        if (course != null) {
            textView.setText(course.getCourseNumber());
        }
        return view;
    }
}
