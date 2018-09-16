package edu.ohio.minuku.streamgenerator;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import edu.ohio.minuku.Utilities.CSVHelper;
import edu.ohio.minuku.config.Constants;
import edu.ohio.minuku.dao.UserInteractionDataRecordDAO;
import edu.ohio.minuku.manager.MinukuDAOManager;
import edu.ohio.minuku.manager.MinukuStreamManager;
import edu.ohio.minuku.model.DataRecord.UserInteractionDataRecord;
import edu.ohio.minuku.stream.UserInteractionStream;
import edu.ohio.minukucore.dao.DAOException;
import edu.ohio.minukucore.exception.StreamAlreadyExistsException;
import edu.ohio.minukucore.exception.StreamNotFoundException;
import edu.ohio.minukucore.stream.Stream;

/**
 * Created by Lawrence on 2018/8/30.
 */

public class UserInteractionStreamGenerator extends AndroidStreamGenerator<UserInteractionDataRecord> {

    private String TAG = "UserInteractionStreamGenerator";

    private UserInteractionStream mStream;
    private UserInteractionDataRecordDAO mDAO;

    private static final String STRING_FALSE = "0";
    private static final String STRING_TRUE = "1";

    private String present = STRING_FALSE;
    private String unlock = STRING_FALSE;

    public UserInteractionStreamGenerator (Context applicationContext) {

        super(applicationContext);
        this.mStream = new UserInteractionStream(Constants.DEFAULT_QUEUE_SIZE);
        this.mDAO = MinukuDAOManager.getInstance().getDaoFor(UserInteractionDataRecord.class);
        this.register();
    }

    @Override
    public void register() {
        Log.d(TAG, "Registring with StreamManager");
        try {
            MinukuStreamManager.getInstance().register(mStream, UserInteractionDataRecord.class, this);
        } catch (StreamNotFoundException streamNotFoundException) {
            Log.e(TAG, "One of the streams on which" +
                    "UserInteractionDataRecord/UserInteractionStream depends in not found.");
        } catch (StreamAlreadyExistsException streamAlreadyExistsException) {
            Log.e(TAG, "Another stream which provides" +
                    " UserInteractionDataRecord/UserInteractionStream is already registered.");
        }
    }

    @Override
    public Stream<UserInteractionDataRecord> generateNewStream() {
        return mStream;
    }

    @Override
    public boolean updateStream() {

        Log.e(TAG, "Update stream called.");

        UserInteractionDataRecord userInteractionDataRecord = new UserInteractionDataRecord(present, unlock);

        mStream.add(userInteractionDataRecord);

        Log.d(TAG, "UserInteractionDataRecord to be sent to event bus" + userInteractionDataRecord);

        EventBus.getDefault().post(userInteractionDataRecord);
        try {
            mDAO.add(userInteractionDataRecord);
        } catch (DAOException e) {
            e.printStackTrace();
            return false;
        }catch (NullPointerException e){ //Sometimes no data is normal
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public long getUpdateFrequency() {
        return 1;
    }

    @Override
    public void sendStateChangeEvent() {

    }

    @Override
    public void onStreamRegistration() {

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_USER_PRESENT);
        intentFilter.addAction(Intent.ACTION_USER_UNLOCKED);
        mApplicationContext.registerReceiver(mBroadcastReceiver, intentFilter);

        CSVHelper.storeToCSV(CSVHelper.CSV_UserInteract, "present", "unlock");

        Log.d(TAG, "Stream " + TAG + " registered successfully");

    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            present = STRING_FALSE;
            unlock = STRING_FALSE;

            if (action.equals(Intent.ACTION_USER_PRESENT)) {

                present = STRING_TRUE;
            }

            if (action.equals(Intent.ACTION_USER_UNLOCKED)) {

                unlock = STRING_TRUE;
            }

            Log.d(TAG, "present : "+ present);
            Log.d(TAG, "unlock : "+ unlock);


            boolean userPresent = intent.getAction().equals( Intent.ACTION_USER_PRESENT );
            boolean userUnlock = intent.getAction().equals( Intent.ACTION_USER_UNLOCKED );

            Log.d(TAG, "userPresent : "+ userPresent);

            //TODO no need userUnlock set the value in bootCompleteReceiver
            Log.d(TAG, "userUnlock : "+ userUnlock);

            CSVHelper.storeToCSV(CSVHelper.CSV_UserInteract, present, unlock);
            CSVHelper.storeToCSV(CSVHelper.CSV_UserInteract, String.valueOf(userPresent), String.valueOf(userUnlock));
        }
    };

    @Override
    public void offer(UserInteractionDataRecord dataRecord) {

    }

}
