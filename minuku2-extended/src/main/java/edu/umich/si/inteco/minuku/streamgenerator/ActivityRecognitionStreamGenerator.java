package edu.umich.si.inteco.minuku.streamgenerator;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.DetectedActivity;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import edu.umich.si.inteco.minuku.config.Constants;
import edu.umich.si.inteco.minuku.dao.ActivityRecognitionDataRecordDAO;
import edu.umich.si.inteco.minuku.manager.MinukuDAOManager;
import edu.umich.si.inteco.minuku.manager.MinukuStreamManager;
import edu.umich.si.inteco.minuku.model.ActivityRecognitionDataRecord;
import edu.umich.si.inteco.minuku.service.ActivityRecognitionService;
import edu.umich.si.inteco.minuku.stream.ActivityRecognitionStream;
import edu.umich.si.inteco.minukucore.dao.DAOException;
import edu.umich.si.inteco.minukucore.exception.StreamAlreadyExistsException;
import edu.umich.si.inteco.minukucore.exception.StreamNotFoundException;
import edu.umich.si.inteco.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2017/5/22.
 */

public class ActivityRecognitionStreamGenerator extends AndroidStreamGenerator<ActivityRecognitionDataRecord> implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public final String TAG = "ActivityRecognitionStreamGenerator";

    private PendingIntent mActivityRecognitionPendingIntent;
    private static GoogleApiClient mGoogleApiClient;
    private Context mContext;

    private ActivityRecognitionStream mStream;
    ActivityRecognitionDataRecordDAO mDAO;

    private static List<DetectedActivity> sProbableActivities;
    private static DetectedActivity sMostProbableActivity;
    private static long sDetectedtime;

    public static int ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS = 5;
    public static long ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL =
            ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL_IN_SECONDS * Constants.MILLISECONDS_PER_SECOND;

    public ActivityRecognitionStreamGenerator(Context applicationContext) { //,Context mContext
        super(applicationContext);
        //this.mContext = mMainServiceContext;
        this.mContext = applicationContext;
        this.mStream = new ActivityRecognitionStream(Constants.LOCATION_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(ActivityRecognitionDataRecord.class);
        this.register();
    }
    /*
        public void setContext(Context Context){
            mContext = Context;
        }

        public static ActivityRecognitionStreamGenerator getInstance() {
            if(ActivityRecognitionStreamGenerator.instance == null) {
                try {
                    ActivityRecognitionStreamGenerator.instance = new ActivityRecognitionStreamGenerator();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return ActivityRecognitionStreamGenerator.instance;
        }
    */
    @Override
    public void register() {
        Log.d(TAG, "Registering with StreamManager.");
        try {
            MinukuStreamManager.getInstance().register(mStream, ActivityRecognitionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which LocationDataRecord depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides LocationDataRecord is already registered.");
        }
    }

    @Override
    public void onStreamRegistration() {
        buildGoogleApiClient();
/*
        EventBus.getDefault().post(new IncrementLoadingProcessCountEvent());

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try
                {
                    Log.d(TAG, "Stream " + TAG + "initialized from previous state");
                    Future<List<ActivityRecognitionDataRecord>> listFuture =
                            mDAO.getLast(Constants.LOCATION_QUEUE_SIZE); //TODO Constants.LOCATION_QUEUE_SIZE must be replaced.
                    while(!listFuture.isDone()) {
                        Thread.sleep(1000);
                    }
                    Log.d(TAG, "Received data from Future for " + TAG);
                    mStream.addAll(new LinkedList<>(listFuture.get()));
                } catch (DAOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } finally {
                    EventBus.getDefault().post(new DecrementLoadingProcessCountEvent());
                }
            }
        });

        */
    }

    protected synchronized void buildGoogleApiClient() {

        if (mGoogleApiClient==null){

            mGoogleApiClient =
                    new GoogleApiClient.Builder(mApplicationContext) // "mApplicationContext" is inspired by LocationStreamGenerator,it might not wrong.
                            .addApi(com.google.android.gms.location.ActivityRecognition.API)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(this)
                            .build();

            mGoogleApiClient.connect();

        }

    }

    @Override
    public Stream<ActivityRecognitionDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {
        Log.e(TAG, "Update stream called.");
        ActivityRecognitionDataRecord activityRecognitionDataRecord
                = new ActivityRecognitionDataRecord(sMostProbableActivity,sDetectedtime);
        mStream.add(activityRecognitionDataRecord);
        Log.e(TAG, "Location to be sent to event bus" + activityRecognitionDataRecord);

        /*
        *  update data in DataRecord
        * */

        EventBus.getDefault().post(activityRecognitionDataRecord);
        try {
            mDAO.add(activityRecognitionDataRecord);
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 15;
    }

    @Override
    public void sendStateChangeEvent() {

    }



    @Override
    public void offer(ActivityRecognitionDataRecord dataRecord) {
        Log.e(TAG, "Offer for ActivityRecognition data record does nothing!");
    }

    @Override
    public void onConnected(Bundle bundle) {

        Log.e(TAG,"onConnected");

        startActivityRecognitionUpdates();
    }

    private void startActivityRecognitionUpdates() {

        Log.d(TAG, "[startActivityRecognitionUpdates]");

        mActivityRecognitionPendingIntent = createRequestPendingIntent();

        //request activity recognition update
        if (com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi!=null){
            com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(
                    mGoogleApiClient,                    //GoogleApiClient client
                    ACTIVITY_RECOGNITION_DEFAULT_UPDATE_INTERVAL,//detectionIntervalMillis
                    mActivityRecognitionPendingIntent);   //callbackIntent

            //Log.d(TAG, "[com.google.android.gms.location.ActivityRecognition.ActivityRecognitionApi] is running!!!");

        }

    }

    private PendingIntent createRequestPendingIntent() {
        Log.d(TAG, "createRequestPendingIntent");
        // If the PendingIntent already exists
        if (mActivityRecognitionPendingIntent != null) {
            return mActivityRecognitionPendingIntent;
            // If no PendingIntent exists
        } else {
            // Create an Intent pointing to the IntentService

            Intent intent = new Intent(
                    mApplicationContext, ActivityRecognitionService.class);

            PendingIntent pendingIntent =
                    PendingIntent.getService(mApplicationContext, //mApplicationContext || mContext
                            0,
                            intent,
                            PendingIntent.FLAG_UPDATE_CURRENT);

            mActivityRecognitionPendingIntent = pendingIntent;
            return pendingIntent;
        }
    }

    public void setActivitiesandDetectedtime (List<DetectedActivity> probableActivities, DetectedActivity mostProbableActivity, long detectedtime) {
        //set activities

        //set a list of probable activities
        setProbableActivities(probableActivities);
        //set the most probable activity
        setMostProbableActivity(mostProbableActivity);

        setDetectedtime(detectedtime);
        /*
        ActivityRecognitionRecord record = ContextManager.getActivityRecognitionRecord();

        record.setProbableActivities(sProbableActivities);
        record.setMostProbableActivity(sMostProbableActivity);
        record.setDetectedtime(sDetectedtime);

        setActivityRecord(record);
*/
        Log.e(TAG,detectedtime+"||"+ mostProbableActivity);

    }

    public void setProbableActivities(List<DetectedActivity> probableActivities) {
        sProbableActivities = probableActivities;

    }

    public void setMostProbableActivity(DetectedActivity mostProbableActivity) {
        sMostProbableActivity = mostProbableActivity;

    }

    public void setDetectedtime(long detectedtime){
        sDetectedtime = detectedtime;

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            // Log.d(LOG_TAG,"[onConnectionFailed] Conntection to Google Play services is failed");

        } else {
            Log.e(TAG, "[onConnectionFailed] No Google Play services is available, the error code is "
                    + connectionResult.getErrorCode());
        }
    }


}
