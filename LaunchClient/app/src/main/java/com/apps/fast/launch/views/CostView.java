package com.apps.fast.launch.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.UI.EntityIconBitmaps;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;

import java.util.Map;

import launch.game.LaunchClientGame;
import launch.game.entities.conceptuals.Resource.ResourceType;

public class CostView extends LinearLayout
{
    private Context context;
    private LinearLayout lytCostsContainer;
    private LaunchClientGame game;

    public CostView(Context context)
    {
        super(context);
        this.context = context;

        Init(context);
    }

    public CostView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.context = context;
        Init(context);
    }

    public CostView(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        this.context = context;
        Init(context);
    }

    private void Init(Context context)
    {
        // Inflate your XML as the root
        LayoutInflater.from(context).inflate(R.layout.view_costs, this, true);

        // This is the container where rows will be added
        lytCostsContainer = findViewById(R.id.lytCostsContainer);
    }

    /**
     * Populates this view with the given resource costs.
     */
    public void SetCosts(Map<ResourceType, Long> costs, LaunchClientGame game)
    {
        // Clear previous children (except the title)
        lytCostsContainer.removeViews(1, lytCostsContainer.getChildCount() - 1);

        for(Map.Entry<ResourceType, Long> entry : costs.entrySet())
        {
            ResourceType type = entry.getKey();
            long oAmount = entry.getValue();

            if(oAmount != 0)
            {
                LinearLayout row = new LinearLayout(getContext());
                row.setOrientation(HORIZONTAL);
                row.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

                row.setPadding(8, 8, 8, 8);

                ImageView icon = new ImageView(context);

                icon.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

                icon.setImageBitmap(EntityIconBitmaps.GetResourceTypeBitmap(getContext(), type));

                TextView txt = new TextView(getContext());
                LayoutParams txtParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

                txtParams.setMargins(4, 0, 0, 0);
                txt.setLayoutParams(txtParams);
                txt.setText(TextUtilities.GetResourceQuantityString(type, oAmount));
                txt.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
                txt.setTextColor(game.GetOurPlayer().GetCargoSystem().HasQuantity(entry.getKey(), entry.getValue()) ? Utilities.ColourFromAttr(context, R.attr.GoodColour)  : Utilities.ColourFromAttr(context, R.attr.BadColour));

                row.addView(icon);
                row.addView(txt);
                lytCostsContainer.addView(row);
            }
        }
    }
}

