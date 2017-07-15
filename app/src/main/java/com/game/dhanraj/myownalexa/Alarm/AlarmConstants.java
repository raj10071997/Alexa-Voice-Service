package com.game.dhanraj.myownalexa.Alarm;

/**
 * Created by Dhanraj on 29-06-2017.
 */

public class AlarmConstants {

    public String type;
    public String mytime;
    public Integer AlarmKeyId;
    public Integer iconsIDs;

    public Integer getIconsIDs() {
        return iconsIDs;
    }

    public void setIconsIDs(Integer iconsIDs) {
        this.iconsIDs = iconsIDs;
    }

    public Integer getAlarmKeyId() {
        return AlarmKeyId;
    }

    public void setAlarmKeyId(Integer alarmKeyId) {
        AlarmKeyId = alarmKeyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMytime() {
        return mytime;
    }

    public void setMytime(String mytime) {
        this.mytime = mytime;
    }
}
