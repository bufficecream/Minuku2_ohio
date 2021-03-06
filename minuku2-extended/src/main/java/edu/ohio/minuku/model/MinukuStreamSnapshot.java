/*
 * Copyright (c) 2016.
 *
 * DReflect and Minuku Libraries by Shriti Raj (shritir@umich.edu) and Neeraj Kumar(neerajk@uci.edu) is licensed under a Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License.
 * Based on a work at https://github.com/Shriti-UCI/Minuku-2.
 *
 *
 * You are free to (only if you meet the terms mentioned below) :
 *
 * Share — copy and redistribute the material in any medium or format
 * Adapt — remix, transform, and build upon the material
 *
 * The licensor cannot revoke these freedoms as long as you follow the license terms.
 *
 * Under the following terms:
 *
 * Attribution — You must give appropriate credit, provide a link to the license, and indicate if changes were made. You may do so in any reasonable manner, but not in any way that suggests the licensor endorses you or your use.
 * NonCommercial — You may not use the material for commercial purposes.
 * ShareAlike — If you remix, transform, or build upon the material, you must distribute your contributions under the same license as the original.
 * No additional restrictions — You may not apply legal terms or technological measures that legally restrict others from doing anything the license permits.
 */

package edu.ohio.minuku.model;

import java.util.List;
import java.util.Map;

import edu.ohio.minukucore.model.DataRecord;
import edu.ohio.minukucore.model.StreamSnapshot;

/**
 * Created by neerajkumar on 7/27/16.
 */
public class MinukuStreamSnapshot implements StreamSnapshot {

    private Map<Class<? extends DataRecord>, List<? extends DataRecord>> mSnapshotData;

    public MinukuStreamSnapshot(Map<Class<? extends DataRecord>,
            List<? extends DataRecord>> snapshotData) {
        mSnapshotData = snapshotData;
    }

    @Override
    public <T extends DataRecord> T getCurrentValue(Class<T> dataRecordType) {
        return (T) mSnapshotData.get(dataRecordType).get(0);
    }

    @Override
    public <T extends DataRecord> T getPreviousValue(Class<T> dataRecordType) {
        return (T) mSnapshotData.get(dataRecordType).get(1);
    }
}
