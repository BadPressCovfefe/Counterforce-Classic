package com.apps.fast.launch.views;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import launch.game.Defs;
import launch.game.EntityPointer;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.entities.LaunchEntity;
import launch.game.entities.NavalVessel;
import launch.game.entities.conceptuals.Resource;
import launch.game.systems.LaunchSystem.SystemType;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.MissileStats;

/**
 * Created by tobster on 09/11/15.
 */
public class LaunchableSelectionView extends FrameLayout
{
    private enum LaunchableType
    {
        MISSILE,
        INTERCEPTOR,
        TORPEDO,
    }

    private MainActivity activity;  //Full activity not just context, so we can display dialogs.
    private LaunchClientGame game;

    //If targeting rather than buying a missile.
    private GeoCoord geoTarget = null;
    private GeoCoord geoOrigin;

    private TextView txtName;
    private TextView txtRange;
    private TextView txtEMPRadius;
    private TextView txtSpeed;
    private TextView txtFlightTime;
    private TextView txtWarhead;
    private TextView txtAccuracy;

    private TextView txtRangeTitle;
    private TextView txtSpeedTitle;
    private TextView txtFlightTimeTitle;
    private TextView txtEMPRadiusTitle;

    //If targeting rather than buying an interceptor.
    private boolean bIntercepting = false;
    private long oFlightTime;

    //Purchase counts.
    private TextView txtNumber;
    private int lNumber = 0;
    private LaunchableType launchableType;

    private boolean bShowFlightTime;

    boolean bHostIsUnderWay;

    //Selecting a missile to launch.
    public LaunchableSelectionView(MainActivity activity, LaunchClientGame game, MissileType type, GeoCoord geoTarget, GeoCoord geoOrigin, SystemType systemType)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        this.geoTarget = geoTarget;
        this.geoOrigin = geoOrigin;
        launchableType = LaunchableType.MISSILE;

        Setup(type);
    }

    //Selecting an interceptor to launch.
    public LaunchableSelectionView(MainActivity activity, LaunchClientGame game, InterceptorType type, long oFlightTime, int lSAMSiteID, boolean bShowFlightTime)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        this.oFlightTime = oFlightTime;
        bIntercepting = true;
        launchableType = LaunchableType.INTERCEPTOR;
        this.bShowFlightTime = bShowFlightTime;

        Setup(type);
    }

    public LaunchableSelectionView(MainActivity activity, LaunchClientGame game, TorpedoType type, boolean bLaunching)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        launchableType = LaunchableType.TORPEDO;

        Setup(type);
    }

    private void Setup(MissileType type)
    {
        inflate(activity, R.layout.view_missile_selection, this);

        txtName = findViewById(R.id.txtName);
        txtRange = findViewById(R.id.txtRange);
        txtEMPRadius = findViewById(R.id.txtWeight);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtFlightTime = findViewById(R.id.txtFlightTime);
        txtWarhead = findViewById(R.id.txtHP);
        txtAccuracy = findViewById(R.id.txtAccuracy);

        txtRangeTitle = findViewById(R.id.txtRangeTitle);
        txtSpeedTitle = findViewById(R.id.txtSpeedTitle);
        txtFlightTimeTitle = findViewById(R.id.txtFlightTimeTitle);
        txtEMPRadiusTitle = findViewById(R.id.txtEMPRadiusTitle);

        txtNumber = findViewById(R.id.txtNumber);

        ImageView imgNuclear = findViewById(R.id.imgNuclear);
        ImageView imgTracking = findViewById(R.id.imgTracking);
        ImageView imgECM = findViewById(R.id.imgECM);
        ImageView imgSonobuoy = findViewById(R.id.imgSonobuoy);
        ImageView imgICBM = findViewById(R.id.imgICBM);
        ImageView imgAntiShip = findViewById(R.id.imgAntiShip);
        ImageView imgAntiSubmarine = findViewById(R.id.imgAntiSubmarine);
        ImageView imgFast = findViewById(R.id.imgFast);

        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetMissileRange(type)));
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, game.GetConfig().GetMissileSpeed(type));
        TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetMissileRange(), true);

        txtEMPRadius.setText(TextUtilities.GetDistanceStringFromKM(MissileStats.GetMissileEMPRadius(type, true)));
        TextUtilities.AssignYieldStringAndAppearance(txtWarhead, type, game);

        imgNuclear.setVisibility(type.GetNuclear() ? VISIBLE : GONE);
        imgTracking.setVisibility(type.GetTracking() ? VISIBLE : GONE);
        imgECM.setVisibility(type.GetECM() ? VISIBLE : GONE);
        imgSonobuoy.setVisibility(type.GetSonobuoy() ? VISIBLE : GONE);
        imgICBM.setVisibility(type.GetICBM() ? VISIBLE : GONE);
        imgAntiShip.setVisibility(type.GetAntiShip() ? VISIBLE : GONE);
        imgAntiSubmarine.setVisibility(type.GetAntiSubmarine() ? VISIBLE : GONE);
        imgFast.setVisibility(game.GetMissileIsFast(type) ? VISIBLE : GONE);

        txtEMPRadius.setVisibility(MissileStats.GetMissileEMPRadius(type, true) != 0 ? VISIBLE : GONE);
        txtEMPRadiusTitle.setVisibility(MissileStats.GetMissileEMPRadius(type, true) != 0 ? VISIBLE : GONE);

        txtAccuracy.setText(TextUtilities.GetPercentStringFromFraction(type.GetAccuracy()));

        if(type.GetAccuracy() >= 0.75f)
            txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.GoodColour));
        else if(type.GetAccuracy() > 0.5f)
            txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.WarningColour));
        else
            txtAccuracy.setTextColor(Utilities.ColourFromAttr(activity, R.attr.BadColour));

        if(type.GetAntiShip())
        {
            imgAntiShip.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.antiship_information));
                }
            });
        }

        if(type.GetAntiSubmarine())
        {
            imgAntiSubmarine.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.antisubmarine_information));
                }
            });
        }

        if(type.GetNuclear())
        {
            imgNuclear.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.nuclear_information));
                }
            });
        }

        if(game.GetMissileIsFast(type))
        {
            imgFast.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.fast_missile_information));
                }
            });
        }

        if(type.GetTracking())
        {
            imgTracking.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.tracking_information));
                }
            });
        }

        if(type.GetECM())
        {
            imgECM.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.ecm_information, TextUtilities.GetAccuracyPercentage(game.GetECMHitChanceReduction())));
                }
            });
        }

        if(type.GetSonobuoy())
        {
            imgSonobuoy.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.sonobuoy_information, TextUtilities.GetDistanceStringFromKM(Defs.SONAR_RANGE)));
                }
            });
        }

        txtName.setText(type.GetName());

        //Launching.
        txtRange.setVisibility(GONE);
        txtRangeTitle.setVisibility(GONE);
        txtSpeed.setVisibility(GONE);
        txtSpeedTitle.setVisibility(GONE);

        txtFlightTime.setText(TextUtilities.GetTimeAmount(game.GetTimeToTarget(geoOrigin, geoTarget, game.GetConfig().GetMissileSpeed(type))));
    }

    private void Setup(InterceptorType type)
    {
        inflate(activity, R.layout.view_interceptor_selection, this);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtRange = findViewById(R.id.txtRange);
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        TextView txtFlightTime = findViewById(R.id.txtFlightTime);
        TextView txtYield = findViewById(R.id.txtYield);
        TextView txtYieldTitle = findViewById(R.id.txtYieldTitle);

        TextView txtFlightTimeTitle = findViewById(R.id.txtFlightTimeTitle);

        txtNumber = findViewById(R.id.txtNumber);

        ImageView imgABM = findViewById(R.id.imgABM);
        ImageView imgFast = findViewById(R.id.imgFast);
        ImageView imgNuclear = findViewById(R.id.imgNuclear);

        txtName.setText(type.GetName());
        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetInterceptorRange(type)));
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, game.GetConfig().GetInterceptorSpeed(type));
        TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetInterceptorRange(), false);

        if(type.GetNuclear())
        {
            TextUtilities.AssignYieldStringAndAppearance(txtYield, type, game);
            imgNuclear.setVisibility(VISIBLE);

            imgNuclear.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.nuclear_interceptor_information));
                }
            });
        }
        else
        {
            txtYieldTitle.setVisibility(GONE);
            txtYield.setVisibility(GONE);
            imgNuclear.setVisibility(GONE);
        }

        imgABM.setVisibility(type.GetABM() ? VISIBLE : GONE);

        if(type.GetABM())
        {
            imgABM.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.abm_interceptor_information));
                }
            });
        }

        imgFast.setVisibility(type.GetABM() ? VISIBLE : GONE);

        if(game.GetInterceptorIsFast(type))
        {
            imgFast.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.fast_interceptor_information));
                }
            });
        }

        //Show the flight time if the target is an aircraft, according to this boolean.
        if(bShowFlightTime)
        {
            txtFlightTime.setVisibility(VISIBLE);
            txtFlightTime.setText(TextUtilities.GetTimeAmount(oFlightTime));
        }
        else
        {
            txtFlightTime.setVisibility(GONE);
        }
    }

    private void Setup(TorpedoType type)
    {
        inflate(activity, R.layout.view_torpedo_selection, this);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtRange = findViewById(R.id.txtRange);
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        TextView txtYield = findViewById(R.id.txtYield);
        TextView txtYieldTitle = findViewById(R.id.txtYieldTitle);

        txtNumber = findViewById(R.id.txtNumber);

        ImageView imgHoming = findViewById(R.id.imgHoming);
        ImageView imgNuclear = findViewById(R.id.imgNuclear);

        txtName.setText(type.GetName());
        txtRange.setText(TextUtilities.GetDistanceStringFromKM(type.GetTorpedoRange()));
        txtSpeed.setText(TextUtilities.AssignSpeedFromKPH(type.GetTorpedoSpeed()));
        TextUtilities.AssignTorpedoRangeAppearance(txtRange, type.GetTorpedoRange());

        if(type.GetNuclear())
        {
            TextUtilities.AssignYieldStringAndAppearance(txtYield, type, game);
            imgNuclear.setVisibility(VISIBLE);

            imgNuclear.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.nuclear_torpedo_information));
                }
            });
        }
        else
        {
            txtYieldTitle.setVisibility(GONE);
            txtYield.setVisibility(GONE);
            imgNuclear.setVisibility(GONE);
        }

        imgHoming.setVisibility(type.GetHoming() ? VISIBLE : GONE);

        if(type.GetHoming())
        {
            imgHoming.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.torpedo_homing_information, TextUtilities.GetDistanceStringFromKM(Defs.TORPEDO_HOMING_RANGE)));
                }
            });
        }
    }

    public void SetHighlighted()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableHighlighted));
        //(findViewById(R.id.lytMain)).setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.detail_button_highlighted));
    }

    public void SetNotHighlighted()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableNormal));
        //(findViewById(R.id.lytMain)).setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.detail_button));
    }

    public void SetDisabled()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableDisabled));
        //(findViewById(R.id.lytMain)).setBackgroundDrawable(activity.getResources().getDrawable(R.drawable.detail_button_disabled));
    }

    /*@Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom)
    {
        super.onLayout(changed, left, top, right, bottom);

        if(bMissile)
        {
            TextView txtNameTitle =  findViewById(R.id.txtNameTitle);
            TextView txtRangeTitle =  findViewById(R.id.txtRangeTitle);
            TextView txtBlastRadiusTitle =  findViewById(R.id.txtBlastRadiusTitle);
            TextView txtMaxDamageTitle =  findViewById(R.id.txtMaxDamageTitle);
            TextView txtSpeedTitle =  findViewById(R.id.txtSpeedTitle);
            TextView txtPrepTimeTitle =  findViewById(R.id.txtPrepTimeTitle);
            TextView txtFlightTimeTitle =  findViewById(R.id.txtFlightTimeTitle);
            TextView txtEMPRadiusTitle = findViewById(R.id.txtEMPRadius);
            TextView txtWarheadTitle = findViewById(R.id.txtWarheadTitle);

            //Set title items to same width, to justify text of the values.
            int lTitleLargest = txtNameTitle.getWidth();
            lTitleLargest = Math.max(lTitleLargest, txtRangeTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtBlastRadiusTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtMaxDamageTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtSpeedTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtPrepTimeTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtFlightTimeTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtEMPRadiusTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtWarheadTitle.getWidth());
            txtNameTitle.setWidth(lTitleLargest);
            txtRangeTitle.setWidth(lTitleLargest);
            txtBlastRadiusTitle.setWidth(lTitleLargest);
            txtMaxDamageTitle.setWidth(lTitleLargest);
            txtSpeedTitle.setWidth(lTitleLargest);
            txtPrepTimeTitle.setWidth(lTitleLargest);
            txtFlightTimeTitle.setWidth(lTitleLargest);
            txtEMPRadiusTitle.setWidth(lTitleLargest);
            txtWarheadTitle.setWidth(lTitleLargest);
        }
        else
        {
            TextView txtNameTitle = findViewById(R.id.txtNameTitle);
            TextView txtRangeTitle = findViewById(R.id.txtRangeTitle);
            TextView txtSpeedTitle = findViewById(R.id.txtSpeedTitle);
            TextView txtPrepTimeTitle = findViewById(R.id.txtPrepTimeTitle);
            TextView txtFlightTimeTitle = findViewById(R.id.txtFlightTimeTitle);

            //Set title items to same width, to justify text of the values.
            int lTitleLargest = txtNameTitle.getWidth();
            lTitleLargest = Math.max(lTitleLargest, txtRangeTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtSpeedTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtPrepTimeTitle.getWidth());
            lTitleLargest = Math.max(lTitleLargest, txtFlightTimeTitle.getWidth());
            txtNameTitle.setWidth(lTitleLargest);
            txtRangeTitle.setWidth(lTitleLargest);
            txtSpeedTitle.setWidth(lTitleLargest);
            txtPrepTimeTitle.setWidth(lTitleLargest);
            txtFlightTimeTitle.setWidth(lTitleLargest);
        }
    }*/

    public void IncrementNumber()
    {
        txtNumber.setText(Integer.toString(++lNumber));
    }

    public void SetNumber(int lNumber)
    {
        txtNumber.setText(Integer.toString(lNumber));
    }
}
