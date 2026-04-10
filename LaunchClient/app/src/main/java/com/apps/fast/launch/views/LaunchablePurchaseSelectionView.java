package com.apps.fast.launch.views;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.PurchaseLaunchableView;
import com.apps.fast.launch.launchviews.PurchaseTorpedoView;

import launch.game.Defs;
import launch.game.GeoCoord;
import launch.game.LaunchClientGame;
import launch.game.LaunchGame;
import launch.game.entities.ArtilleryGun;
import launch.game.entities.LaunchEntity;
import launch.game.entities.NavalVessel;
import launch.game.entities.conceptuals.Resource;
import launch.game.types.InterceptorType;
import launch.game.types.MissileType;
import launch.game.types.TorpedoType;
import launch.utilities.MissileStats;

public class LaunchablePurchaseSelectionView extends FrameLayout
{
    private enum LaunchableType
    {
        MISSILE,
        INTERCEPTOR,
        TORPEDO,
    }

    private MainActivity activity;  //Full activity not just context, so we can display dialogs.
    private LaunchClientGame game;
    private LinearLayout lytType;
    private LinearLayout lytSpecs;
    private ImageView imgType;
    private TextView txtName;
    private TextView txtRange;
    private TextView txtEMPRadius;
    private TextView txtSpeed;
    private TextView txtPrepTime;
    private TextView txtWarhead;
    private TextView txtAccuracy;
    private TextView txtEMPRadiusTitle;
    private TextView txtCost;

    //Purchase counts.
    private TextView txtNumber;
    private int lNumber = 0;
    boolean bHostIsUnderWay;
    private PurchaseLaunchableView purchaseView;
    private PurchaseTorpedoView purchaseTorpedoView;

    public LaunchablePurchaseSelectionView(MainActivity activity, LaunchClientGame game, MissileType type, LaunchEntity host, PurchaseLaunchableView purchaseView)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        this.purchaseView = purchaseView;

        if(host instanceof NavalVessel)
        {
            bHostIsUnderWay = !game.ShipInPort((NavalVessel)host);
        }

        Setup(type);
    }

    public LaunchablePurchaseSelectionView(MainActivity activity, LaunchClientGame game, InterceptorType type, LaunchEntity host, PurchaseLaunchableView purchaseView)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        this.purchaseView = purchaseView;

        if(host instanceof NavalVessel)
        {
            bHostIsUnderWay = !game.ShipInPort((NavalVessel)host);
        }

        Setup(type);
    }

    public LaunchablePurchaseSelectionView(MainActivity activity, LaunchClientGame game, TorpedoType type, NavalVessel host, PurchaseTorpedoView purchaseView)
    {
        super(activity);
        this.activity = activity;
        this.game = game;
        this.purchaseTorpedoView = purchaseView;

        if(host != null)
        {
            bHostIsUnderWay = !game.ShipInPort((NavalVessel)host);
        }

        Setup(type);
    }

    private void Setup(MissileType type)
    {
        inflate(activity, R.layout.view_missile_purchase, this);

        txtName = findViewById(R.id.txtName);
        txtRange = findViewById(R.id.txtRange);
        txtEMPRadius = findViewById(R.id.txtWeight);
        txtSpeed = findViewById(R.id.txtSpeed);
        txtPrepTime = findViewById(R.id.txtPrepTime);
        txtWarhead = findViewById(R.id.txtHP);
        txtAccuracy = findViewById(R.id.txtAccuracy);
        txtEMPRadiusTitle = findViewById(R.id.txtEMPRadiusTitle);
        txtNumber = findViewById(R.id.txtNumber);
        txtCost = findViewById(R.id.txtCost);

        lytSpecs = findViewById(R.id.lytSpecs);
        lytType = findViewById(R.id.lytType);
        imgType = findViewById(R.id.imgType);

        imgType.setImageBitmap(EntityIconBitmaps.GetMissileBitmap(activity, game, type, LaunchGame.Allegiance.UNAFFILIATED, type.GetAssetID()));

        ImageView imgNuclear = findViewById(R.id.imgNuclear);
        ImageView imgTracking = findViewById(R.id.imgTracking);
        ImageView imgECM = findViewById(R.id.imgECM);
        ImageView imgSonobuoy = findViewById(R.id.imgSonobuoy);
        ImageView imgICBM = findViewById(R.id.imgICBM);
        ImageView imgAntiShip = findViewById(R.id.imgAntiShip);
        ImageView imgAntiSubmarine = findViewById(R.id.imgAntiSubmarine);
        ImageView imgEMP = findViewById(R.id.imgEMP);
        ImageView imgFast = findViewById(R.id.imgFast);

        long oCost = type.GetCost();

        txtCost.setText(TextUtilities.GetCurrencyString(oCost));
        txtCost.setTextColor(Utilities.ColourFromAttr(activity, oCost <= game.GetOurPlayer().GetWealth() ? R.attr.GoodColour : R.attr.BadColour));
        txtName.setText(type.GetName());
        txtPrepTime.setText(TextUtilities.GetTimeAmount(MissileStats.GetMissileBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss())));

        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetMissileRange(type)));
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, game.GetConfig().GetMissileSpeed(type));
        TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetMissileRange(), true);

        txtEMPRadius.setText(TextUtilities.GetDistanceStringFromKM(MissileStats.GetMissileEMPRadius(type, true)));
        TextUtilities.AssignYieldStringAndAppearance(txtWarhead, type, game);
        TextUtilities.AssignBuildTimeAppearance(txtPrepTime, MissileStats.GetMissileBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss()));

        imgNuclear.setVisibility(type.GetNuclear() ? VISIBLE : GONE);
        imgTracking.setVisibility(type.GetTracking() ? VISIBLE : GONE);
        imgECM.setVisibility(type.GetECM() ? VISIBLE : GONE);
        imgSonobuoy.setVisibility(type.GetSonobuoy() ? VISIBLE : GONE);
        imgICBM.setVisibility(type.GetICBM() ? VISIBLE : GONE);
        imgAntiShip.setVisibility(type.GetAntiShip() ? VISIBLE : GONE);
        imgAntiSubmarine.setVisibility(type.GetAntiSubmarine() ? VISIBLE : GONE);
        imgEMP.setVisibility(type.GetEMPRadius() > 0 ? VISIBLE : GONE);
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

        if(type.GetEMPRadius() > 0.0f)
        {
            imgEMP.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.emp_description));
                }
            });
        }

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
    }

    private void Setup(InterceptorType type)
    {
        inflate(activity, R.layout.view_interceptor_purchase, this);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtRange = findViewById(R.id.txtRange);
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        TextView txtPrepTime = findViewById(R.id.txtPrepTime);
        TextView txtYield = findViewById(R.id.txtYield);
        TextView txtYieldTitle = findViewById(R.id.txtYieldTitle);
        TextView txtPrepTimeTitle = findViewById(R.id.txtPrepTimeTitle);
        txtNumber = findViewById(R.id.txtNumber);
        ImageView imgFast = findViewById(R.id.imgFast);
        ImageView imgABM = findViewById(R.id.imgABM);
        ImageView imgNuclear = findViewById(R.id.imgNuclear);
        lytSpecs = findViewById(R.id.lytSpecs);
        lytType = findViewById(R.id.lytType);
        imgType = findViewById(R.id.imgType);
        txtCost = findViewById(R.id.txtCost);

        long oCost = type.GetCost();

        txtCost.setText(TextUtilities.GetCurrencyString(oCost));
        txtCost.setTextColor(Utilities.ColourFromAttr(activity, oCost <= game.GetOurPlayer().GetWealth() ? R.attr.GoodColour : R.attr.BadColour));

        imgType.setImageBitmap(EntityIconBitmaps.GetInterceptorBitmap(activity, game, type, LaunchGame.Allegiance.UNAFFILIATED, type.GetAssetID()));

        txtName.setText(type.GetName());
        txtRange.setText(TextUtilities.GetDistanceStringFromKM(game.GetConfig().GetInterceptorRange(type)));
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, game.GetConfig().GetInterceptorSpeed(type));
        TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetInterceptorRange(), false);
        TextUtilities.AssignBuildTimeAppearance(txtPrepTime, MissileStats.GetInterceptorBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss()));

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

        imgFast.setVisibility(game.GetInterceptorIsFast(type) ? VISIBLE : GONE);

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

        imgABM.setVisibility(type.GetABM() ? VISIBLE : GONE);

        if(game.GetInterceptorIsFast(type))
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

        txtPrepTime.setText(TextUtilities.GetTimeAmount(MissileStats.GetInterceptorBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss())));
    }

    private void Setup(TorpedoType type)
    {
        inflate(activity, R.layout.view_torpedo_purchase, this);

        TextView txtName = findViewById(R.id.txtName);
        TextView txtRange = findViewById(R.id.txtRange);
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        TextView txtPrepTime = findViewById(R.id.txtPrepTime);
        TextView txtYield = findViewById(R.id.txtYield);
        TextView txtYieldTitle = findViewById(R.id.txtYieldTitle);
        TextView txtPrepTimeTitle = findViewById(R.id.txtPrepTimeTitle);
        txtNumber = findViewById(R.id.txtNumber);
        ImageView imgHoming = findViewById(R.id.imgHoming);
        ImageView imgNuclear = findViewById(R.id.imgNuclear);
        lytSpecs = findViewById(R.id.lytSpecs);
        lytType = findViewById(R.id.lytType);
        imgType = findViewById(R.id.imgType);
        txtCost = findViewById(R.id.txtCost);

        long oCost = type.GetCost();

        txtCost.setText(TextUtilities.GetCurrencyString(oCost));
        txtCost.setTextColor(Utilities.ColourFromAttr(activity, oCost <= game.GetOurPlayer().GetWealth() ? R.attr.GoodColour : R.attr.BadColour));

        imgType.setImageBitmap(EntityIconBitmaps.GetTorpedoBitmap(activity, game, type, LaunchGame.Allegiance.YOU, type.GetAssetID()));

        txtName.setText(type.GetName());
        txtRange.setText(TextUtilities.GetDistanceStringFromKM(type.GetTorpedoRange()));
        TextUtilities.AssignMachSpeedFromKph(txtSpeed, type.GetTorpedoSpeed());
        TextUtilities.AssignMissileRangeAppearance(txtRange, type.GetTorpedoRange(), false);
        TextUtilities.AssignBuildTimeAppearance(txtPrepTime, MissileStats.GetTorpedoBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss()));

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

        if(type.GetHoming())
        {
            imgHoming.setVisibility(VISIBLE);

            imgHoming.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    activity.ShowBasicOKDialog(activity.getString(R.string.torpedo_homing_information));
                }
            });
        }
        else
        {
            imgHoming.setVisibility(GONE);
        }

        txtPrepTime.setText(TextUtilities.GetTimeAmount(MissileStats.GetTorpedoBuildTime(type, bHostIsUnderWay, game.GetOurPlayer().GetBoss())));
    }

    public void SetHighlighted()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableHighlighted));
    }

    public void SetNotHighlighted()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableNormal));
    }

    public void SetDisabled()
    {
        findViewById(R.id.lytMain).setBackground(Utilities.DrawableFromAttr(activity, R.attr.DetailButtonDrawableDisabled));
    }

    public void IncrementNumber()
    {
        txtNumber.setText(Integer.toString(++lNumber));
    }

    public void SetNumber(int lNumber)
    {
        txtNumber.setText(Integer.toString(lNumber));
    }
}
