package com.apps.fast.launch.launchviews;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apps.fast.launch.R;
import com.apps.fast.launch.activities.MainActivity;
import com.apps.fast.launch.views.ReportView;

import java.util.List;

import launch.game.LaunchClientGame;
import launch.game.entities.Player;
import launch.game.Defs;
import launch.utilities.LaunchEvent;
import launch.utilities.LaunchReport;

public class ReportsView extends LaunchView
{
    private enum ReportsPage
    {
        ALL,
        REPORTS,
        ECONOMY,
        COMBAT,
    }

    private String[] SortOrderNames;

    private void InitialiseSortTitles()
    {
        SortOrderNames = new String[]
                {
                        context.getString(R.string.reports_channel_all),
                        context.getString(R.string.reports_channel_reports),
                        context.getString(R.string.reports_channel_economy),
                        context.getString(R.string.reports_channel_combat),
                };
    }

    private LinearLayout lytReports;
    private ReportsPage reportsPage;
    private TextView btnSortBy;

    public ReportsView(LaunchClientGame game, MainActivity activity)
    {
        super(game, activity);
    }

    @Override
    protected void Setup()
    {
        inflate(context, R.layout.view_reports, this);

        InitialiseSortTitles();

        reportsPage = ReportsPage.ALL;
        lytReports = findViewById(R.id.lytReports);
        btnSortBy = findViewById(R.id.btnSortBy);
        lytReports.removeAllViews();

        btnSortBy.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Display dialog with sort by options.
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(context.getString(R.string.sort_missile_types_by));

                builder.setSingleChoiceItems(SortOrderNames, reportsPage.ordinal(), new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i)
                    {
                        reportsPage = ReportsPage.values()[i];
                        RefreshReportList();
                        dialogInterface.dismiss();
                    }
                });

                builder.show();
            }
        });

        RefreshReportList();

        findViewById(R.id.txtNone).setVisibility(game.GetNewReports().size() > 0 || game.GetOldReports().size() > 0 ? View.GONE : View.VISIBLE);
    }

    private void RefreshReportList()
    {
        lytReports.removeAllViews();

        switch(reportsPage)
        {
            case ALL:
            {
                btnSortBy.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.reports_channel_all)));
            }
            break;

            case REPORTS:
            {
                btnSortBy.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.reports_channel_reports)));
            }
            break;

            case ECONOMY:
            {
                btnSortBy.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.reports_channel_economy)));
            }
            break;

            case COMBAT:
            {
                btnSortBy.setText(context.getString(R.string.sorted_by_format, context.getString(R.string.reports_channel_combat)));
            }
            break;
        }

        PopulateWithReports(game.GetOldReports(), false);
        PopulateWithReports(game.GetNewReports(), true);
        game.TransferNewReportsToOld();
    }

    private void PopulateWithReports(List<LaunchReport> Reports, boolean bNew)
    {
        for(final LaunchReport report : Reports)
        {
            String msg = report.GetMessage();

            //Filter reports based on selected sort option
            switch (reportsPage)
            {
                case ECONOMY:
                    if (!msg.contains("[ECONOMY]")) continue;
                    break;
                case COMBAT:
                    if (!msg.contains("[COMBAT]")) continue;
                    break;
                case ALL:

                default:
                    break;
            }

            ReportView reportView = new ReportView(game, activity, report, bNew);

            if(!report.GetLeftIDAlliance())
            {
                reportView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        Player doer = game.GetPlayer(report.GetLeftID());

                        if(doer != null)
                        {
                            activity.SelectEntity(doer);
                        }
                    }
                });
            }

            lytReports.addView(reportView, 0);
        }
    }

    @Override
    public void Update()
    {

    }
}

