package com.huawei.nb.model.collectencrypt;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;
import java.util.Date;

public class RawCalenderConf extends AManagedObject {
    public static final Creator<RawCalenderConf> CREATOR = new Creator<RawCalenderConf>() {
        public RawCalenderConf createFromParcel(Parcel in) {
            return new RawCalenderConf(in);
        }

        public RawCalenderConf[] newArray(int size) {
            return new RawCalenderConf[size];
        }
    };
    private String mConfAddr;
    private Date mConfBeginTime;
    private Date mConfEndTime;
    private String mConfSponsor;
    private Integer mConfStat;
    private String mConfTopic;
    private Integer mId;
    private Integer mReservedInt;
    private String mReservedText;
    private Date mTimeStamp;

    public RawCalenderConf(Cursor cursor) {
        Integer num = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.mId = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.mTimeStamp = cursor.isNull(2) ? null : new Date(cursor.getLong(2));
        this.mConfTopic = cursor.getString(3);
        this.mConfBeginTime = cursor.isNull(4) ? null : new Date(cursor.getLong(4));
        this.mConfEndTime = cursor.isNull(5) ? null : new Date(cursor.getLong(5));
        this.mConfAddr = cursor.getString(6);
        this.mConfSponsor = cursor.getString(7);
        this.mConfStat = cursor.isNull(8) ? null : Integer.valueOf(cursor.getInt(8));
        if (!cursor.isNull(9)) {
            num = Integer.valueOf(cursor.getInt(9));
        }
        this.mReservedInt = num;
        this.mReservedText = cursor.getString(10);
    }

    public RawCalenderConf(Parcel in) {
        String str = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.mId = null;
            in.readInt();
        } else {
            this.mId = Integer.valueOf(in.readInt());
        }
        this.mTimeStamp = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mConfTopic = in.readByte() == (byte) 0 ? null : in.readString();
        this.mConfBeginTime = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mConfEndTime = in.readByte() == (byte) 0 ? null : new Date(in.readLong());
        this.mConfAddr = in.readByte() == (byte) 0 ? null : in.readString();
        this.mConfSponsor = in.readByte() == (byte) 0 ? null : in.readString();
        this.mConfStat = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.mReservedInt = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        if (in.readByte() != (byte) 0) {
            str = in.readString();
        }
        this.mReservedText = str;
    }

    private RawCalenderConf(Integer mId, Date mTimeStamp, String mConfTopic, Date mConfBeginTime, Date mConfEndTime, String mConfAddr, String mConfSponsor, Integer mConfStat, Integer mReservedInt, String mReservedText) {
        this.mId = mId;
        this.mTimeStamp = mTimeStamp;
        this.mConfTopic = mConfTopic;
        this.mConfBeginTime = mConfBeginTime;
        this.mConfEndTime = mConfEndTime;
        this.mConfAddr = mConfAddr;
        this.mConfSponsor = mConfSponsor;
        this.mConfStat = mConfStat;
        this.mReservedInt = mReservedInt;
        this.mReservedText = mReservedText;
    }

    public int describeContents() {
        return 0;
    }

    public Integer getMId() {
        return this.mId;
    }

    public void setMId(Integer mId) {
        this.mId = mId;
        setValue();
    }

    public Date getMTimeStamp() {
        return this.mTimeStamp;
    }

    public void setMTimeStamp(Date mTimeStamp) {
        this.mTimeStamp = mTimeStamp;
        setValue();
    }

    public String getMConfTopic() {
        return this.mConfTopic;
    }

    public void setMConfTopic(String mConfTopic) {
        this.mConfTopic = mConfTopic;
        setValue();
    }

    public Date getMConfBeginTime() {
        return this.mConfBeginTime;
    }

    public void setMConfBeginTime(Date mConfBeginTime) {
        this.mConfBeginTime = mConfBeginTime;
        setValue();
    }

    public Date getMConfEndTime() {
        return this.mConfEndTime;
    }

    public void setMConfEndTime(Date mConfEndTime) {
        this.mConfEndTime = mConfEndTime;
        setValue();
    }

    public String getMConfAddr() {
        return this.mConfAddr;
    }

    public void setMConfAddr(String mConfAddr) {
        this.mConfAddr = mConfAddr;
        setValue();
    }

    public String getMConfSponsor() {
        return this.mConfSponsor;
    }

    public void setMConfSponsor(String mConfSponsor) {
        this.mConfSponsor = mConfSponsor;
        setValue();
    }

    public Integer getMConfStat() {
        return this.mConfStat;
    }

    public void setMConfStat(Integer mConfStat) {
        this.mConfStat = mConfStat;
        setValue();
    }

    public Integer getMReservedInt() {
        return this.mReservedInt;
    }

    public void setMReservedInt(Integer mReservedInt) {
        this.mReservedInt = mReservedInt;
        setValue();
    }

    public String getMReservedText() {
        return this.mReservedText;
    }

    public void setMReservedText(String mReservedText) {
        this.mReservedText = mReservedText;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.mId != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mId.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.mTimeStamp != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mTimeStamp.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfTopic != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mConfTopic);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfBeginTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mConfBeginTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfEndTime != null) {
            out.writeByte((byte) 1);
            out.writeLong(this.mConfEndTime.getTime());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfAddr != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mConfAddr);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfSponsor != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mConfSponsor);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mConfStat != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mConfStat.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedInt != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.mReservedInt.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.mReservedText != null) {
            out.writeByte((byte) 1);
            out.writeString(this.mReservedText);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<RawCalenderConf> getHelper() {
        return RawCalenderConfHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.collectencrypt.RawCalenderConf";
    }

    public String getDatabaseName() {
        return "dsCollectEncrypt";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("RawCalenderConf { mId: ").append(this.mId);
        sb.append(", mTimeStamp: ").append(this.mTimeStamp);
        sb.append(", mConfTopic: ").append(this.mConfTopic);
        sb.append(", mConfBeginTime: ").append(this.mConfBeginTime);
        sb.append(", mConfEndTime: ").append(this.mConfEndTime);
        sb.append(", mConfAddr: ").append(this.mConfAddr);
        sb.append(", mConfSponsor: ").append(this.mConfSponsor);
        sb.append(", mConfStat: ").append(this.mConfStat);
        sb.append(", mReservedInt: ").append(this.mReservedInt);
        sb.append(", mReservedText: ").append(this.mReservedText);
        sb.append(" }");
        return sb.toString();
    }

    public boolean equals(Object o) {
        return super.equals(o);
    }

    public int hashCode() {
        return super.hashCode();
    }

    public String getDatabaseVersion() {
        return "0.0.10";
    }

    public int getDatabaseVersionCode() {
        return 10;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
