package com.haesunggroup.myday;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.ChartData;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

public class ChartFragment extends Fragment {
    PieChart pieChart;
    PieDataSet dataSet;
    PieData data;
    int otherTime;
    int totalTime;
    UsageStatsManager usageManager;
    Calendar calendar;
    List<UsageStats> usageQuery;
    PackageManager packageManager;
    ApplicationInfo appInfo;
    static int oneDay = 86400000;
    static int oneHour = 3600000;
    static int oneMinute = 60000;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_chart, container, false);
        pieChart = v.findViewById(R.id.pieChart);


        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setExtraOffsets(10, 10, 10, 10);
        pieChart.setCenterText(generateCenterSpannableText());
        pieChart.setRotationEnabled(false);
        pieChart.animateY(1400, Easing.EaseInOutCubic);


        //Get UsageList
        calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR,-1);
        usageManager = (UsageStatsManager) Objects.requireNonNull(getActivity()).getSystemService(Context.USAGE_STATS_SERVICE);
        assert usageManager != null;
        usageQuery = usageManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,
                System.currentTimeMillis()-oneDay, System.currentTimeMillis());

        //Get PackageLabel
        packageManager = getActivity().getPackageManager();

        setChartData();

        return v;

    }


    public void setChartData() {

        ArrayList<PieEntry> Values = new ArrayList<>();
        for (UsageStats stats : usageQuery) {
            int time = (int) stats.getTotalTimeInForeground();
            if (time > 0 && time >= oneHour) {
                Values.add(new PieEntry(time, getPackageLabel(stats.getPackageName())));
                Log.d("123", getPackageLabel(stats.getPackageName()) + time);
                totalTime += time;
            } else if (time > 0) {
                otherTime += time;
                Log.d("123", getPackageLabel(stats.getPackageName()) + time);
            }
        }

        totalTime += otherTime;
        if (otherTime > 0) {
            Values.add(new PieEntry(otherTime, "Others"));
        }
        Values.add(new PieEntry(oneDay - totalTime, "blankTime"));

        dataSet = new PieDataSet(Values, "appsTime");
        dataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        dataSet.setDrawValues(true);

        data = new PieData((dataSet));
        data.setDrawValues(true);
        pieChart.setData(data);

    }

    public SpannableString generateCenterSpannableText() {
        SpannableString s = new SpannableString("총 시간\n" + totalTime);
        return s;
    }

    public String getPackageLabel(String packageName){
        try {
            appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageManager.getApplicationLabel(appInfo).toString();
    }

    public void setPieChart() {

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(false);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);

        //animation
    }

}
