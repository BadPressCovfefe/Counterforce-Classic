package com.apps.fast.launch.launchviews.entities;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.StructureIconBitmaps;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.launchviews.LaunchView;
import com.apps.fast.launch.views.EntityControls;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;
import launch.game.entities.Blueprint;
import launch.game.EntityPointer.EntityType;

/**
 * Created by tobster on 09/11/15.
 */
public class BlueprintView extends LaunchView
{
    private ImageView imgBlueprint;
    private TextView txtExpiry;
    private TextView txtBlueprintTitle;
    private LinearLayout btnSell;
    private int lID;

    public BlueprintView(LaunchClientGame game, MainActivity activity, int lID)
    {
        super(game, activity, true);
        this.lID = lID;
        Setup();
    }

    @Override
    protected void Setup()
    {
        Blueprint blueprint = game.GetBlueprint(lID);

        if(blueprint != null)
        {
            inflate(context, R.layout.view_blueprint, this);
            ((EntityControls)findViewById(R.id.entityControls)).SetActivity(activity);

            imgBlueprint = (ImageView)findViewById(R.id.imgBlueprint);
            txtExpiry = (TextView) findViewById(R.id.txtExpiry);
            txtBlueprintTitle = (TextView) findViewById(R.id.txtBlueprintTitle);
            btnSell = findViewById(R.id.btnSell);

            txtBlueprintTitle.setText(TextUtilities.GetEntityTypeAndName(blueprint, game));
            imgBlueprint.setImageBitmap(StructureIconBitmaps.GetBlueprintBitmap(context, game, blueprint.GetType()));

            btnSell.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderPurchase();
                    launchDialog.SetMessage(context.getString(R.string.confirm_remove_blueprint));
                    launchDialog.SetOnClickYes(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                            game.SellEntity(blueprint.GetPointer());
                        }
                    });
                    launchDialog.SetOnClickNo(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            });

            Update();
        }
        else
        {
            Finish(true);
        }
    }

    @Override
    public void Update()
    {
        activity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                Blueprint blueprint = game.GetBlueprint(lID);

                if(blueprint != null)
                    txtExpiry.setText(TextUtilities.GetTimeAmount(blueprint.GetExpiryRemaining()));
                else
                    Finish(true);
            }
        });
    }
}
