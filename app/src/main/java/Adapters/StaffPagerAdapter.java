package Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.finalprojectapp.staff.DirectorGardensFragment;
import com.example.finalprojectapp.staff.StaffListFragment;
import com.example.finalprojectapp.staff.StaffListInGardenFragment;

/**
 * A FragmentStateAdapter that provides fragments for each tab in a ViewPager2.
 */
public class StaffPagerAdapter extends FragmentStateAdapter {

    /**
     * Constructor for StaffPagerAdapter.
     *
     * @param fragmentActivity The FragmentActivity that this adapter is associated with.
     */
    public StaffPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    /**
     * Creates a new fragment based on the position of the tab.
     *
     * @param position The position of the tab.
     * @return The Fragment corresponding to the given position.
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DirectorGardensFragment();
            case 1:
                return new StaffListFragment();
            case 2:
                return new StaffListInGardenFragment();
            default:
                return new DirectorGardensFragment();
        }
    }

    /**
     * Returns the number of tabs (pages) in the ViewPager2.
     *
     * @return The number of tabs.
     */
    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}
