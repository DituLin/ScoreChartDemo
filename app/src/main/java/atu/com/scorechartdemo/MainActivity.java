package atu.com.scorechartdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import atu.com.scorechartdemo.score.ScoreChartView;

public class MainActivity extends AppCompatActivity {

    ScoreChartView scoreChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scoreChartView = (ScoreChartView) findViewById(R.id.scoreView);
        initScore();
    }

    private void initScore() {
        int max = 100;
        int min = 0;

        List list = new ArrayList<Integer>();

        Random ramdom = new Random();
        for(int i = 0; i < 12; i++)
        {
            list.add(ramdom.nextInt(max) % (max - min + 1) + min);
        }

        scoreChartView.setScore(list);
    }
}
