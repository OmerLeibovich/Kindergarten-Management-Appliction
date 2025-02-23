package Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.finalprojectapp.parents.ChildGardensFragment;
import com.example.finalprojectapp.parents.SearchGardenFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for managing fragments in a ViewPager2 for parent-related screens.
 */
public class ParentPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();

    /**
     * Constructor for ParentPagerAdapter.
     *
     * @param fragmentActivity The FragmentActivity hosting the fragments.
     */
    public ParentPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Initialize the list of fragments to be displayed
        fragmentList.add(new SearchGardenFragment());
        fragmentList.add(new ChildGardensFragment());
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return the fragment at the specified position
        return fragmentList.get(position);
    }

    @Override
    public int getItemCount() {
        // Return the total number of fragments
        return fragmentList.size();
    }
}
