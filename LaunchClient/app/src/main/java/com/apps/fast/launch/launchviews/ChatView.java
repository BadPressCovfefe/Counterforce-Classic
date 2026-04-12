package com.apps.fast.launch.launchviews;

import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.Utilities;

import launch.game.LaunchClientGame;
import launch.game.entities.MissileFactory;
import launch.game.entities.MissileFactory.ChatChannel;
import launch.game.entities.LaunchEntity;

public class ChatView extends LaunchView
{
    protected LinearLayout lytMessageEdit;
    protected EditText txtMessageEdit;
    protected LinearLayout btnSendMessage;
    protected LinearLayout btnChangeChannel;
    protected ImageView imgChannel;
    protected TextView txtChannel;
    protected LinearLayout lytEverything;
    private String strMessage;
    private boolean bGlobal = true; //Using a boolean instead of ChatChannel values because this UI view should only allow alliance or global, not private.

    public ChatView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity, true);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_chat, this);

        lytMessageEdit = findViewById(R.id.lytMessageEdit);
        txtMessageEdit = findViewById(R.id.txtMessageEdit);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        btnChangeChannel = findViewById(R.id.btnChangeChannel);
        imgChannel = findViewById(R.id.imgChannel);
        txtChannel = findViewById(R.id.txtChannel);
        lytEverything = findViewById(R.id.lytEverything);

        txtChannel.setText(context.getString(bGlobal ? R.string.channel_global : R.string.channel_alliance));
        txtChannel.setTextColor(bGlobal ? Color.WHITE : Color.CYAN);
        imgChannel.setImageDrawable(context.getDrawable(bGlobal ? R.drawable.todo : R.drawable.todo));

        btnChangeChannel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.GetOurPlayer().GetAllianceMemberID() != LaunchEntity.ID_NONE)
                {
                    bGlobal = !bGlobal;
                    txtChannel.setText(context.getString(bGlobal ? R.string.channel_global : R.string.channel_alliance));
                    txtChannel.setTextColor(bGlobal ? Color.WHITE : Color.CYAN);
                    imgChannel.setImageDrawable(context.getDrawable(bGlobal ? R.drawable.todo : R.drawable.todo));
                }
                else
                {
                    activity.ShowBasicOKDialog(context.getString(R.string.cant_change_channel));
                }

            }
        });

        btnSendMessage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Utilities.DismissKeyboard(activity, activity.getCurrentFocus());

                //Clear any existing errors.
                txtMessageEdit.setError(null);

                //Get the values.
                strMessage = txtMessageEdit.getText().toString();

                if(!strMessage.isBlank())
                {
                    game.SendMessage(LaunchEntity.ID_NONE, bGlobal ? ChatChannel.GLOBAL : ChatChannel.ALLIANCE, strMessage);
                    activity.ClearSelectedEntity();
                    activity.ReturnToMainView();
                }

                Utilities.DismissKeyboard(activity, txtMessageEdit);
            }
        });
    }
}
