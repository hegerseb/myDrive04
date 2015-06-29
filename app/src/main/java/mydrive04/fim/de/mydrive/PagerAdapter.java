package mydrive04.fim.de.mydrive;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by dev on 02.06.2015.
 */
public class PagerAdapter extends FragmentPagerAdapter {
    private Context mcontext;
    public PagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        mcontext = context;
    }

    @Override
    public Fragment getItem(int position) {
        switch(position){
            case 0:
                return new FragmentHistory();

            case 1:
                FragmentTracking ft = new FragmentTracking();
                return ft;

            case 2:
                FragementConsumption fc = new FragementConsumption();
                return fc;

            default:
                break;
        }


        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "Verlauf";
            case 1:
                return "myDrive";
            case 2:
                return "Tankstopp";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
