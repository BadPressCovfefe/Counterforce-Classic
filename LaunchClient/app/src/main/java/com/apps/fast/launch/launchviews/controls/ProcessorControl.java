package com.apps.fast.launch.launchviews.controls;

import android.os.Build;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.widget.TextViewCompat;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.launchviews.LaunchView;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import launch.game.Defs;
import launch.game.LaunchClientGame;
import launch.game.entities.Player;
import launch.game.entities.Processor;
import launch.game.entities.conceptuals.Resource.ResourceType;
import launch.utilities.LaunchUtilities;

public class ProcessorControl extends LaunchView
{
    private int lID;
    private LinearLayout lytIrradiated;
    private LinearLayout lytInputs;
    private LinearLayout lytOutputs;
    private Processor processor;

    public ProcessorControl(LaunchClientGame game, MainActivity activity, int lProcessorID)
    {
        super(game, activity, true);
        lID = lProcessorID;

        processor = game.GetProcessor(lProcessorID);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.control_processor, this);

        lytIrradiated = findViewById(R.id.lytIrradiated);
        lytInputs = findViewById(R.id.lytInputs);
        lytOutputs = findViewById(R.id.lytOutputs);

        if(processor != null)
        {
            Map<ResourceType, Long> Inputs = Defs.GetProcessorInput(processor.GetType());
            Map<ResourceType, Long> Outputs = Defs.GetProcessorOutput(processor.GetType());

            Player ourPlayer = game.GetOurPlayer();

            if(Inputs != null && Outputs != null)
            {
                for(Entry<ResourceType, Long> input : Inputs.entrySet())
                {
                    TextView txtInput = new TextView(context);
                    TextViewCompat.setTextAppearance(txtInput, android.R.style.TextAppearance_Medium);

                    LayoutParams params = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(0, R.dimen.MainViewSpacing, 0, R.dimen.MainViewSpacing);

                    // Base label
                    String prefix = context.getString(R.string.processor_input); // e.g. "Input: "

                    // Resource part
                    String resourcePart = TextUtilities.GetWeightStringFromKG(input.getValue()) + " " + TextUtilities.GetResourceTypeString(input.getKey());

                    // Combine
                    SpannableStringBuilder sb = new SpannableStringBuilder(prefix + " " + resourcePart);

                    int goodColor = Utilities.ColourFromAttr(context, R.attr.GoodColour);
                    sb.setSpan(new ForegroundColorSpan(goodColor), 0, sb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    // Apply color only to the resource part
                    int start = prefix.length();
                    int end = sb.length();
                    int color = Utilities.ColourFromAttr(context, ourPlayer.GetCargoSystem().HasQuantity(input.getKey(), input.getValue()) ? R.attr.GoodColour : R.attr.BadColour);
                    sb.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    txtInput.setText(sb);
                    lytInputs.addView(txtInput);
                }

                for(Entry<ResourceType, Long> output : Outputs.entrySet())
                {
                    TextView txtOutput = new TextView(context);
                    txtOutput.setVisibility(VISIBLE);
                    TextViewCompat.setTextAppearance(txtOutput, android.R.style.TextAppearance_Medium);

                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    txtOutput.setText(context.getString(R.string.processor_output, TextUtilities.GetWeightStringFromKG(output.getValue()), TextUtilities.GetResourceTypeString(output.getKey())));
                    txtOutput.setLayoutParams(params);
                    txtOutput.setTextColor(Utilities.ColourFromAttr(context, R.attr.GoodColour));
                    lytOutputs.addView(txtOutput);
                }
            }

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
                Processor processor = game.GetProcessor(lID);

                if(processor != null)
                {
                    if(game.GetRadioactive(processor, true))
                    {
                        lytIrradiated.setVisibility(VISIBLE);
                    }
                    else
                    {
                        lytIrradiated.setVisibility(GONE);
                    }
                }
            }
        });
    }
}
