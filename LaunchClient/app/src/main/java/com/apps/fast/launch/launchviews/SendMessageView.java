package com.apps.fast.launch.launchviews;

import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.components.Sounds;
import com.apps.fast.launch.components.TextUtilities;
import com.apps.fast.launch.components.Utilities;
import com.apps.fast.launch.views.LaunchDialog;

import launch.game.LaunchClientGame;
import launch.game.entities.MissileFactory;
import launch.game.entities.LaunchEntity;
import launch.game.entities.Player;

/**
 * Created by corbin/Biscuit on 4/27/23.
 */
public class SendMessageView extends LaunchView
{

    private String strMessage;
    private int lReceivingID;

    protected LinearLayout lytMessageEdit;
    protected EditText txtMessageEdit;
    protected LinearLayout btnSendMessage;

    private Player receiver;

    public SendMessageView(LaunchClientGame game, MainActivity activity, int lPlayerID)
    {
        super(game, activity, true);

        this.lReceivingID = lPlayerID;

        if(game.GetPlayer(lPlayerID) != null)
            receiver = game.GetPlayer(lPlayerID);

        Setup();
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_send_message, this);

        lytMessageEdit = findViewById(R.id.lytMessageEdit);
        txtMessageEdit = findViewById(R.id.txtMessageEdit);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        txtMessageEdit.setHint(context.getString(R.string.type_message));

        btnSendMessage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(game.PlayerHasMissileFactory(game.GetOurPlayerID()))
                {
                    Utilities.DismissKeyboard(activity, activity.getCurrentFocus());

                    //Clear any existing errors.
                    txtMessageEdit.setError(null);

                    //Get the values.
                    strMessage = txtMessageEdit.getText().toString();

                    if(!strMessage.isBlank())
                    {
                        game.SendMessage(lReceivingID, MissileFactory.ChatChannel.PRIVATE, strMessage);
                        ReturnToParentView();
                    }

                    Utilities.DismissKeyboard(activity, txtMessageEdit);
                }
                else
                {
                    final LaunchDialog launchDialog = new LaunchDialog();
                    launchDialog.SetHeaderLaunch();
                    launchDialog.SetMessage(context.getString(R.string.no_missile_factory));
                    launchDialog.SetOnClickOk(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            launchDialog.dismiss();
                        }
                    });
                    launchDialog.show(activity.getFragmentManager(), "");
                }
            }
        });
    }

    @Override
    public void Update()
    {

    }

    private void ReturnToParentView()
    {
        activity.SelectEntity(game.GetPlayer(lReceivingID));
    }
}