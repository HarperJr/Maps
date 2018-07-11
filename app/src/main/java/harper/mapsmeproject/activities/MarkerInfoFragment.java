package harper.mapsmeproject.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import harper.mapsmeproject.R;
import harper.mapsmeproject.models.EntityInfo;

import java.util.List;

public class MarkerInfoFragment extends Fragment {


    private FrameLayout containerLayout;

    private Animation openingAnimation;
    private Animation closingAnimation;

    private TextView title;
    private TextView description;

    private boolean isOpened;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Anims
        openingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.flex_animation_opening);
        closingAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.flex_animation_closing);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        final View infoView = inflater.inflate(R.layout.flex_info, container, false);
        containerLayout = infoView.findViewById(R.id.flex_list);


        return infoView;
    }

    public void showInfo() {
        if (!isOpened) {
            containerLayout.startAnimation(openingAnimation);
            Toast.makeText(getContext(), "Open info", Toast.LENGTH_LONG).show();
            isOpened = true;
        }
    }

    public void closeInfo() {
        if (isOpened) {
            containerLayout.startAnimation(closingAnimation);
            Toast.makeText(getContext(), "Close info", Toast.LENGTH_LONG).show();
            isOpened = false;
        }
    }

    public void updateInfo(List<EntityInfo> info) {

    }

}
