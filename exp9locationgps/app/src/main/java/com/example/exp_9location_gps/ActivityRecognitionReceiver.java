package com.example.exp_9location_gps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;

public class ActivityRecognitionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            if (result != null) {
                List<DetectedActivity> activities = result.getProbableActivities();
                if (!activities.isEmpty()) {
                    DetectedActivity mostProbable = activities.get(0);
                    String activityName = getActivityName(mostProbable.getType());

                    Intent updateIntent = new Intent("ACTIVITY_UPDATE");
                    updateIntent.putExtra("activity", activityName);
                    updateIntent.putExtra("confidence", mostProbable.getConfidence());
                    updateIntent.setPackage(context.getPackageName());
                    context.sendBroadcast(updateIntent);
                }
            }
        }
    }

    private String getActivityName(int type) {
        switch (type) {
            case DetectedActivity.IN_VEHICLE:
                return "In Vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "On Bicycle";
            case DetectedActivity.ON_FOOT:
                return "On Foot";
            case DetectedActivity.RUNNING:
                return "Running";
            case DetectedActivity.STILL:
                return "Still";
            case DetectedActivity.TILTING:
                return "Tilting";
            case DetectedActivity.WALKING:
                return "Walking";
            default:
                return "Unknown";
        }
    }
}
