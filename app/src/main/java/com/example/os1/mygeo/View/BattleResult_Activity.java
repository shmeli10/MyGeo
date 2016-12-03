package com.example.os1.mygeo.View;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.os1.mygeo.Model.MyApp;
import com.example.os1.mygeo.R;

/**
 * Created by OS1 on 08.11.2016.
 */
public class BattleResult_Activity  extends     Activity
                                    implements  View.OnClickListener {

    private TextView battleResultTitleTV;
    private LinearLayout closeWrapLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_battle_result);

        battleResultTitleTV = (TextView) findViewById(R.id.BattleResult_Title_TV);

        closeWrapLL = (LinearLayout) findViewById(R.id.BattleResult_CloseWrap_LL);
        closeWrapLL.setOnClickListener(this);

        setBattleResult();
    }

    private void setBattleResult() {

        // Log.d(MyApp.LOG_TAG, "BattleResult_Activity: setBattleResult(): result: " +MyApp.getLastBattleResult());

        switch(MyApp.getLastBattleResult()) {

            case MyApp.I_WON:
                                        battleResultTitleTV.setTextColor(getResources().getColor(R.color.green));
                                        battleResultTitleTV.setText(getResources().getString(R.string.you_won));
                                        break;
            case MyApp.OPPONENT_WON:
                                        battleResultTitleTV.setTextColor(getResources().getColor(R.color.red));
                                        battleResultTitleTV.setText(getResources().getString(R.string.opponent_won));
                                        break;
            case MyApp.NOBODY_WON:
                                        battleResultTitleTV.setTextColor(getResources().getColor(R.color.grey));
                                        battleResultTitleTV.setText(getResources().getString(R.string.nobody_won));
        }
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {

            case R.id.BattleResult_CloseWrap_LL:  BattleResult_Activity.this.finish();
        }
    }
}
