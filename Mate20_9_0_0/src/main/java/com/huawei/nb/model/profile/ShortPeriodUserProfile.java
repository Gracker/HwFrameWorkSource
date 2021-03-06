package com.huawei.nb.model.profile;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable.Creator;
import com.huawei.odmf.core.AManagedObject;
import com.huawei.odmf.model.AEntityHelper;

public class ShortPeriodUserProfile extends AManagedObject {
    public static final Creator<ShortPeriodUserProfile> CREATOR = new Creator<ShortPeriodUserProfile>() {
        public ShortPeriodUserProfile createFromParcel(Parcel in) {
            return new ShortPeriodUserProfile(in);
        }

        public ShortPeriodUserProfile[] newArray(int size) {
            return new ShortPeriodUserProfile[size];
        }
    };
    private String accomCellId;
    private String accomCellLac;
    private String accomLatitude;
    private String accomLongtitude;
    private Double age;
    private String alarmTime_Workday;
    private String arriveHomeTime;
    private String arriveWorkplaceTime;
    private String callingDuration;
    private String callingTimes;
    private String city;
    private String contactNumber;
    private String deviceToken;
    private String gameTime;
    private String genDate;
    private String hwId;
    private Integer id;
    private String imei;
    private String leaveHomeTime;
    private String leaveWorkplaceTime;
    private String musicNumber;
    private String musicNumber_Before2000;
    private String offTime;
    private String onTime;
    private String photoNumber;
    private String photoNumber_Self;
    private String photoNumber_Turism;
    private String poi1CellId;
    private String poi1CellLac;
    private String poi1Latitude;
    private String poi1Longtitude;
    private String poi2CellId;
    private String poi2CellLac;
    private String poi2Latitude;
    private String poi2Longtitude;
    private String poi3CellId;
    private String poi3CellLac;
    private String poi3Latitude;
    private String poi3Longtitude;
    private String poi4CellId;
    private String poi4CellLac;
    private String poi4Latitude;
    private String poi4Longtitude;
    private String poi5CellId;
    private String poi5CellLac;
    private String poi5Latitude;
    private String poi5Longtitude;
    private String roamingRadius;
    private Double sexuality;
    private Integer sn;
    private String top10Tag;
    private String top1Tag;
    private String top2Tag;
    private String top3Tag;
    private String top4Tag;
    private String top5Tag;
    private String top6Tag;
    private String top7Tag;
    private String top8Tag;
    private String top9Tag;
    private String topMode;
    private String videoTime;
    private String workCellId;
    private String workCellLac;
    private String workLatitude;
    private String workLongtitude;

    public ShortPeriodUserProfile(Cursor cursor) {
        Double d = null;
        setRowId(Long.valueOf(cursor.getLong(0)));
        this.id = cursor.isNull(1) ? null : Integer.valueOf(cursor.getInt(1));
        this.sn = cursor.isNull(2) ? null : Integer.valueOf(cursor.getInt(2));
        this.genDate = cursor.getString(3);
        this.deviceToken = cursor.getString(4);
        this.hwId = cursor.getString(5);
        this.imei = cursor.getString(6);
        this.sexuality = cursor.isNull(7) ? null : Double.valueOf(cursor.getDouble(7));
        if (!cursor.isNull(8)) {
            d = Double.valueOf(cursor.getDouble(8));
        }
        this.age = d;
        this.contactNumber = cursor.getString(9);
        this.accomLongtitude = cursor.getString(10);
        this.accomLatitude = cursor.getString(11);
        this.accomCellId = cursor.getString(12);
        this.accomCellLac = cursor.getString(13);
        this.workLongtitude = cursor.getString(14);
        this.workLatitude = cursor.getString(15);
        this.workCellId = cursor.getString(16);
        this.workCellLac = cursor.getString(17);
        this.poi1Longtitude = cursor.getString(18);
        this.poi1Latitude = cursor.getString(19);
        this.poi1CellId = cursor.getString(20);
        this.poi1CellLac = cursor.getString(21);
        this.poi2Longtitude = cursor.getString(22);
        this.poi2Latitude = cursor.getString(23);
        this.poi2CellId = cursor.getString(24);
        this.poi2CellLac = cursor.getString(25);
        this.poi3Longtitude = cursor.getString(26);
        this.poi3Latitude = cursor.getString(27);
        this.poi3CellId = cursor.getString(28);
        this.poi3CellLac = cursor.getString(29);
        this.poi4Longtitude = cursor.getString(30);
        this.poi4Latitude = cursor.getString(31);
        this.poi4CellId = cursor.getString(32);
        this.poi4CellLac = cursor.getString(33);
        this.poi5Longtitude = cursor.getString(34);
        this.poi5Latitude = cursor.getString(35);
        this.poi5CellId = cursor.getString(36);
        this.poi5CellLac = cursor.getString(37);
        this.roamingRadius = cursor.getString(38);
        this.city = cursor.getString(39);
        this.photoNumber = cursor.getString(40);
        this.photoNumber_Self = cursor.getString(41);
        this.gameTime = cursor.getString(42);
        this.videoTime = cursor.getString(43);
        this.alarmTime_Workday = cursor.getString(44);
        this.callingDuration = cursor.getString(45);
        this.callingTimes = cursor.getString(46);
        this.musicNumber = cursor.getString(47);
        this.musicNumber_Before2000 = cursor.getString(48);
        this.photoNumber_Turism = cursor.getString(49);
        this.topMode = cursor.getString(50);
        this.top1Tag = cursor.getString(51);
        this.top2Tag = cursor.getString(52);
        this.top3Tag = cursor.getString(53);
        this.top4Tag = cursor.getString(54);
        this.top5Tag = cursor.getString(55);
        this.top6Tag = cursor.getString(56);
        this.top7Tag = cursor.getString(57);
        this.top8Tag = cursor.getString(58);
        this.top9Tag = cursor.getString(59);
        this.top10Tag = cursor.getString(60);
        this.onTime = cursor.getString(61);
        this.leaveHomeTime = cursor.getString(62);
        this.arriveWorkplaceTime = cursor.getString(63);
        this.leaveWorkplaceTime = cursor.getString(64);
        this.arriveHomeTime = cursor.getString(65);
        this.offTime = cursor.getString(66);
    }

    public ShortPeriodUserProfile(Parcel in) {
        String str = null;
        super(in);
        if (in.readByte() == (byte) 0) {
            this.id = null;
            in.readInt();
        } else {
            this.id = Integer.valueOf(in.readInt());
        }
        this.sn = in.readByte() == (byte) 0 ? null : Integer.valueOf(in.readInt());
        this.genDate = in.readByte() == (byte) 0 ? null : in.readString();
        this.deviceToken = in.readByte() == (byte) 0 ? null : in.readString();
        this.hwId = in.readByte() == (byte) 0 ? null : in.readString();
        this.imei = in.readByte() == (byte) 0 ? null : in.readString();
        this.sexuality = in.readByte() == (byte) 0 ? null : Double.valueOf(in.readDouble());
        this.age = in.readByte() == (byte) 0 ? null : Double.valueOf(in.readDouble());
        this.contactNumber = in.readByte() == (byte) 0 ? null : in.readString();
        this.accomLongtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.accomLatitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.accomCellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.accomCellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.workLongtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.workLatitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.workCellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.workCellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi1Longtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi1Latitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi1CellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi1CellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi2Longtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi2Latitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi2CellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi2CellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi3Longtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi3Latitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi3CellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi3CellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi4Longtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi4Latitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi4CellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi4CellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi5Longtitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi5Latitude = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi5CellId = in.readByte() == (byte) 0 ? null : in.readString();
        this.poi5CellLac = in.readByte() == (byte) 0 ? null : in.readString();
        this.roamingRadius = in.readByte() == (byte) 0 ? null : in.readString();
        this.city = in.readByte() == (byte) 0 ? null : in.readString();
        this.photoNumber = in.readByte() == (byte) 0 ? null : in.readString();
        this.photoNumber_Self = in.readByte() == (byte) 0 ? null : in.readString();
        this.gameTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.videoTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.alarmTime_Workday = in.readByte() == (byte) 0 ? null : in.readString();
        this.callingDuration = in.readByte() == (byte) 0 ? null : in.readString();
        this.callingTimes = in.readByte() == (byte) 0 ? null : in.readString();
        this.musicNumber = in.readByte() == (byte) 0 ? null : in.readString();
        this.musicNumber_Before2000 = in.readByte() == (byte) 0 ? null : in.readString();
        this.photoNumber_Turism = in.readByte() == (byte) 0 ? null : in.readString();
        this.topMode = in.readByte() == (byte) 0 ? null : in.readString();
        this.top1Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top2Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top3Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top4Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top5Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top6Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top7Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top8Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top9Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.top10Tag = in.readByte() == (byte) 0 ? null : in.readString();
        this.onTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.leaveHomeTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.arriveWorkplaceTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.leaveWorkplaceTime = in.readByte() == (byte) 0 ? null : in.readString();
        this.arriveHomeTime = in.readByte() == (byte) 0 ? null : in.readString();
        if (in.readByte() != (byte) 0) {
            str = in.readString();
        }
        this.offTime = str;
    }

    private ShortPeriodUserProfile(Integer id, Integer sn, String genDate, String deviceToken, String hwId, String imei, Double sexuality, Double age, String contactNumber, String accomLongtitude, String accomLatitude, String accomCellId, String accomCellLac, String workLongtitude, String workLatitude, String workCellId, String workCellLac, String poi1Longtitude, String poi1Latitude, String poi1CellId, String poi1CellLac, String poi2Longtitude, String poi2Latitude, String poi2CellId, String poi2CellLac, String poi3Longtitude, String poi3Latitude, String poi3CellId, String poi3CellLac, String poi4Longtitude, String poi4Latitude, String poi4CellId, String poi4CellLac, String poi5Longtitude, String poi5Latitude, String poi5CellId, String poi5CellLac, String roamingRadius, String city, String photoNumber, String photoNumber_Self, String gameTime, String videoTime, String alarmTime_Workday, String callingDuration, String callingTimes, String musicNumber, String musicNumber_Before2000, String photoNumber_Turism, String topMode, String top1Tag, String top2Tag, String top3Tag, String top4Tag, String top5Tag, String top6Tag, String top7Tag, String top8Tag, String top9Tag, String top10Tag, String onTime, String leaveHomeTime, String arriveWorkplaceTime, String leaveWorkplaceTime, String arriveHomeTime, String offTime) {
        this.id = id;
        this.sn = sn;
        this.genDate = genDate;
        this.deviceToken = deviceToken;
        this.hwId = hwId;
        this.imei = imei;
        this.sexuality = sexuality;
        this.age = age;
        this.contactNumber = contactNumber;
        this.accomLongtitude = accomLongtitude;
        this.accomLatitude = accomLatitude;
        this.accomCellId = accomCellId;
        this.accomCellLac = accomCellLac;
        this.workLongtitude = workLongtitude;
        this.workLatitude = workLatitude;
        this.workCellId = workCellId;
        this.workCellLac = workCellLac;
        this.poi1Longtitude = poi1Longtitude;
        this.poi1Latitude = poi1Latitude;
        this.poi1CellId = poi1CellId;
        this.poi1CellLac = poi1CellLac;
        this.poi2Longtitude = poi2Longtitude;
        this.poi2Latitude = poi2Latitude;
        this.poi2CellId = poi2CellId;
        this.poi2CellLac = poi2CellLac;
        this.poi3Longtitude = poi3Longtitude;
        this.poi3Latitude = poi3Latitude;
        this.poi3CellId = poi3CellId;
        this.poi3CellLac = poi3CellLac;
        this.poi4Longtitude = poi4Longtitude;
        this.poi4Latitude = poi4Latitude;
        this.poi4CellId = poi4CellId;
        this.poi4CellLac = poi4CellLac;
        this.poi5Longtitude = poi5Longtitude;
        this.poi5Latitude = poi5Latitude;
        this.poi5CellId = poi5CellId;
        this.poi5CellLac = poi5CellLac;
        this.roamingRadius = roamingRadius;
        this.city = city;
        this.photoNumber = photoNumber;
        this.photoNumber_Self = photoNumber_Self;
        this.gameTime = gameTime;
        this.videoTime = videoTime;
        this.alarmTime_Workday = alarmTime_Workday;
        this.callingDuration = callingDuration;
        this.callingTimes = callingTimes;
        this.musicNumber = musicNumber;
        this.musicNumber_Before2000 = musicNumber_Before2000;
        this.photoNumber_Turism = photoNumber_Turism;
        this.topMode = topMode;
        this.top1Tag = top1Tag;
        this.top2Tag = top2Tag;
        this.top3Tag = top3Tag;
        this.top4Tag = top4Tag;
        this.top5Tag = top5Tag;
        this.top6Tag = top6Tag;
        this.top7Tag = top7Tag;
        this.top8Tag = top8Tag;
        this.top9Tag = top9Tag;
        this.top10Tag = top10Tag;
        this.onTime = onTime;
        this.leaveHomeTime = leaveHomeTime;
        this.arriveWorkplaceTime = arriveWorkplaceTime;
        this.leaveWorkplaceTime = leaveWorkplaceTime;
        this.arriveHomeTime = arriveHomeTime;
        this.offTime = offTime;
    }

    public int describeContents() {
        return 0;
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
        setValue();
    }

    public Integer getSn() {
        return this.sn;
    }

    public void setSn(Integer sn) {
        this.sn = sn;
        setValue();
    }

    public String getGenDate() {
        return this.genDate;
    }

    public void setGenDate(String genDate) {
        this.genDate = genDate;
        setValue();
    }

    public String getDeviceToken() {
        return this.deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
        setValue();
    }

    public String getHwId() {
        return this.hwId;
    }

    public void setHwId(String hwId) {
        this.hwId = hwId;
        setValue();
    }

    public String getImei() {
        return this.imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
        setValue();
    }

    public Double getSexuality() {
        return this.sexuality;
    }

    public void setSexuality(Double sexuality) {
        this.sexuality = sexuality;
        setValue();
    }

    public Double getAge() {
        return this.age;
    }

    public void setAge(Double age) {
        this.age = age;
        setValue();
    }

    public String getContactNumber() {
        return this.contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
        setValue();
    }

    public String getAccomLongtitude() {
        return this.accomLongtitude;
    }

    public void setAccomLongtitude(String accomLongtitude) {
        this.accomLongtitude = accomLongtitude;
        setValue();
    }

    public String getAccomLatitude() {
        return this.accomLatitude;
    }

    public void setAccomLatitude(String accomLatitude) {
        this.accomLatitude = accomLatitude;
        setValue();
    }

    public String getAccomCellId() {
        return this.accomCellId;
    }

    public void setAccomCellId(String accomCellId) {
        this.accomCellId = accomCellId;
        setValue();
    }

    public String getAccomCellLac() {
        return this.accomCellLac;
    }

    public void setAccomCellLac(String accomCellLac) {
        this.accomCellLac = accomCellLac;
        setValue();
    }

    public String getWorkLongtitude() {
        return this.workLongtitude;
    }

    public void setWorkLongtitude(String workLongtitude) {
        this.workLongtitude = workLongtitude;
        setValue();
    }

    public String getWorkLatitude() {
        return this.workLatitude;
    }

    public void setWorkLatitude(String workLatitude) {
        this.workLatitude = workLatitude;
        setValue();
    }

    public String getWorkCellId() {
        return this.workCellId;
    }

    public void setWorkCellId(String workCellId) {
        this.workCellId = workCellId;
        setValue();
    }

    public String getWorkCellLac() {
        return this.workCellLac;
    }

    public void setWorkCellLac(String workCellLac) {
        this.workCellLac = workCellLac;
        setValue();
    }

    public String getPoi1Longtitude() {
        return this.poi1Longtitude;
    }

    public void setPoi1Longtitude(String poi1Longtitude) {
        this.poi1Longtitude = poi1Longtitude;
        setValue();
    }

    public String getPoi1Latitude() {
        return this.poi1Latitude;
    }

    public void setPoi1Latitude(String poi1Latitude) {
        this.poi1Latitude = poi1Latitude;
        setValue();
    }

    public String getPoi1CellId() {
        return this.poi1CellId;
    }

    public void setPoi1CellId(String poi1CellId) {
        this.poi1CellId = poi1CellId;
        setValue();
    }

    public String getPoi1CellLac() {
        return this.poi1CellLac;
    }

    public void setPoi1CellLac(String poi1CellLac) {
        this.poi1CellLac = poi1CellLac;
        setValue();
    }

    public String getPoi2Longtitude() {
        return this.poi2Longtitude;
    }

    public void setPoi2Longtitude(String poi2Longtitude) {
        this.poi2Longtitude = poi2Longtitude;
        setValue();
    }

    public String getPoi2Latitude() {
        return this.poi2Latitude;
    }

    public void setPoi2Latitude(String poi2Latitude) {
        this.poi2Latitude = poi2Latitude;
        setValue();
    }

    public String getPoi2CellId() {
        return this.poi2CellId;
    }

    public void setPoi2CellId(String poi2CellId) {
        this.poi2CellId = poi2CellId;
        setValue();
    }

    public String getPoi2CellLac() {
        return this.poi2CellLac;
    }

    public void setPoi2CellLac(String poi2CellLac) {
        this.poi2CellLac = poi2CellLac;
        setValue();
    }

    public String getPoi3Longtitude() {
        return this.poi3Longtitude;
    }

    public void setPoi3Longtitude(String poi3Longtitude) {
        this.poi3Longtitude = poi3Longtitude;
        setValue();
    }

    public String getPoi3Latitude() {
        return this.poi3Latitude;
    }

    public void setPoi3Latitude(String poi3Latitude) {
        this.poi3Latitude = poi3Latitude;
        setValue();
    }

    public String getPoi3CellId() {
        return this.poi3CellId;
    }

    public void setPoi3CellId(String poi3CellId) {
        this.poi3CellId = poi3CellId;
        setValue();
    }

    public String getPoi3CellLac() {
        return this.poi3CellLac;
    }

    public void setPoi3CellLac(String poi3CellLac) {
        this.poi3CellLac = poi3CellLac;
        setValue();
    }

    public String getPoi4Longtitude() {
        return this.poi4Longtitude;
    }

    public void setPoi4Longtitude(String poi4Longtitude) {
        this.poi4Longtitude = poi4Longtitude;
        setValue();
    }

    public String getPoi4Latitude() {
        return this.poi4Latitude;
    }

    public void setPoi4Latitude(String poi4Latitude) {
        this.poi4Latitude = poi4Latitude;
        setValue();
    }

    public String getPoi4CellId() {
        return this.poi4CellId;
    }

    public void setPoi4CellId(String poi4CellId) {
        this.poi4CellId = poi4CellId;
        setValue();
    }

    public String getPoi4CellLac() {
        return this.poi4CellLac;
    }

    public void setPoi4CellLac(String poi4CellLac) {
        this.poi4CellLac = poi4CellLac;
        setValue();
    }

    public String getPoi5Longtitude() {
        return this.poi5Longtitude;
    }

    public void setPoi5Longtitude(String poi5Longtitude) {
        this.poi5Longtitude = poi5Longtitude;
        setValue();
    }

    public String getPoi5Latitude() {
        return this.poi5Latitude;
    }

    public void setPoi5Latitude(String poi5Latitude) {
        this.poi5Latitude = poi5Latitude;
        setValue();
    }

    public String getPoi5CellId() {
        return this.poi5CellId;
    }

    public void setPoi5CellId(String poi5CellId) {
        this.poi5CellId = poi5CellId;
        setValue();
    }

    public String getPoi5CellLac() {
        return this.poi5CellLac;
    }

    public void setPoi5CellLac(String poi5CellLac) {
        this.poi5CellLac = poi5CellLac;
        setValue();
    }

    public String getRoamingRadius() {
        return this.roamingRadius;
    }

    public void setRoamingRadius(String roamingRadius) {
        this.roamingRadius = roamingRadius;
        setValue();
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
        setValue();
    }

    public String getPhotoNumber() {
        return this.photoNumber;
    }

    public void setPhotoNumber(String photoNumber) {
        this.photoNumber = photoNumber;
        setValue();
    }

    public String getPhotoNumber_Self() {
        return this.photoNumber_Self;
    }

    public void setPhotoNumber_Self(String photoNumber_Self) {
        this.photoNumber_Self = photoNumber_Self;
        setValue();
    }

    public String getGameTime() {
        return this.gameTime;
    }

    public void setGameTime(String gameTime) {
        this.gameTime = gameTime;
        setValue();
    }

    public String getVideoTime() {
        return this.videoTime;
    }

    public void setVideoTime(String videoTime) {
        this.videoTime = videoTime;
        setValue();
    }

    public String getAlarmTime_Workday() {
        return this.alarmTime_Workday;
    }

    public void setAlarmTime_Workday(String alarmTime_Workday) {
        this.alarmTime_Workday = alarmTime_Workday;
        setValue();
    }

    public String getCallingDuration() {
        return this.callingDuration;
    }

    public void setCallingDuration(String callingDuration) {
        this.callingDuration = callingDuration;
        setValue();
    }

    public String getCallingTimes() {
        return this.callingTimes;
    }

    public void setCallingTimes(String callingTimes) {
        this.callingTimes = callingTimes;
        setValue();
    }

    public String getMusicNumber() {
        return this.musicNumber;
    }

    public void setMusicNumber(String musicNumber) {
        this.musicNumber = musicNumber;
        setValue();
    }

    public String getMusicNumber_Before2000() {
        return this.musicNumber_Before2000;
    }

    public void setMusicNumber_Before2000(String musicNumber_Before2000) {
        this.musicNumber_Before2000 = musicNumber_Before2000;
        setValue();
    }

    public String getPhotoNumber_Turism() {
        return this.photoNumber_Turism;
    }

    public void setPhotoNumber_Turism(String photoNumber_Turism) {
        this.photoNumber_Turism = photoNumber_Turism;
        setValue();
    }

    public String getTopMode() {
        return this.topMode;
    }

    public void setTopMode(String topMode) {
        this.topMode = topMode;
        setValue();
    }

    public String getTop1Tag() {
        return this.top1Tag;
    }

    public void setTop1Tag(String top1Tag) {
        this.top1Tag = top1Tag;
        setValue();
    }

    public String getTop2Tag() {
        return this.top2Tag;
    }

    public void setTop2Tag(String top2Tag) {
        this.top2Tag = top2Tag;
        setValue();
    }

    public String getTop3Tag() {
        return this.top3Tag;
    }

    public void setTop3Tag(String top3Tag) {
        this.top3Tag = top3Tag;
        setValue();
    }

    public String getTop4Tag() {
        return this.top4Tag;
    }

    public void setTop4Tag(String top4Tag) {
        this.top4Tag = top4Tag;
        setValue();
    }

    public String getTop5Tag() {
        return this.top5Tag;
    }

    public void setTop5Tag(String top5Tag) {
        this.top5Tag = top5Tag;
        setValue();
    }

    public String getTop6Tag() {
        return this.top6Tag;
    }

    public void setTop6Tag(String top6Tag) {
        this.top6Tag = top6Tag;
        setValue();
    }

    public String getTop7Tag() {
        return this.top7Tag;
    }

    public void setTop7Tag(String top7Tag) {
        this.top7Tag = top7Tag;
        setValue();
    }

    public String getTop8Tag() {
        return this.top8Tag;
    }

    public void setTop8Tag(String top8Tag) {
        this.top8Tag = top8Tag;
        setValue();
    }

    public String getTop9Tag() {
        return this.top9Tag;
    }

    public void setTop9Tag(String top9Tag) {
        this.top9Tag = top9Tag;
        setValue();
    }

    public String getTop10Tag() {
        return this.top10Tag;
    }

    public void setTop10Tag(String top10Tag) {
        this.top10Tag = top10Tag;
        setValue();
    }

    public String getOnTime() {
        return this.onTime;
    }

    public void setOnTime(String onTime) {
        this.onTime = onTime;
        setValue();
    }

    public String getLeaveHomeTime() {
        return this.leaveHomeTime;
    }

    public void setLeaveHomeTime(String leaveHomeTime) {
        this.leaveHomeTime = leaveHomeTime;
        setValue();
    }

    public String getArriveWorkplaceTime() {
        return this.arriveWorkplaceTime;
    }

    public void setArriveWorkplaceTime(String arriveWorkplaceTime) {
        this.arriveWorkplaceTime = arriveWorkplaceTime;
        setValue();
    }

    public String getLeaveWorkplaceTime() {
        return this.leaveWorkplaceTime;
    }

    public void setLeaveWorkplaceTime(String leaveWorkplaceTime) {
        this.leaveWorkplaceTime = leaveWorkplaceTime;
        setValue();
    }

    public String getArriveHomeTime() {
        return this.arriveHomeTime;
    }

    public void setArriveHomeTime(String arriveHomeTime) {
        this.arriveHomeTime = arriveHomeTime;
        setValue();
    }

    public String getOffTime() {
        return this.offTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
        setValue();
    }

    public void writeToParcel(Parcel out, int ignored) {
        super.writeToParcel(out, ignored);
        if (this.id != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.id.intValue());
        } else {
            out.writeByte((byte) 0);
            out.writeInt(1);
        }
        if (this.sn != null) {
            out.writeByte((byte) 1);
            out.writeInt(this.sn.intValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.genDate != null) {
            out.writeByte((byte) 1);
            out.writeString(this.genDate);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.deviceToken != null) {
            out.writeByte((byte) 1);
            out.writeString(this.deviceToken);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.hwId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.hwId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.imei != null) {
            out.writeByte((byte) 1);
            out.writeString(this.imei);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.sexuality != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.sexuality.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.age != null) {
            out.writeByte((byte) 1);
            out.writeDouble(this.age.doubleValue());
        } else {
            out.writeByte((byte) 0);
        }
        if (this.contactNumber != null) {
            out.writeByte((byte) 1);
            out.writeString(this.contactNumber);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomLongtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.accomLongtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomLatitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.accomLatitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomCellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.accomCellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.accomCellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.accomCellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workLongtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.workLongtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workLatitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.workLatitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workCellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.workCellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.workCellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.workCellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1Longtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi1Longtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1Latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi1Latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1CellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi1CellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi1CellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi1CellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2Longtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi2Longtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2Latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi2Latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2CellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi2CellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi2CellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi2CellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3Longtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi3Longtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3Latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi3Latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3CellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi3CellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi3CellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi3CellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4Longtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi4Longtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4Latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi4Latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4CellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi4CellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi4CellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi4CellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5Longtitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi5Longtitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5Latitude != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi5Latitude);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5CellId != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi5CellId);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.poi5CellLac != null) {
            out.writeByte((byte) 1);
            out.writeString(this.poi5CellLac);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.roamingRadius != null) {
            out.writeByte((byte) 1);
            out.writeString(this.roamingRadius);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.city != null) {
            out.writeByte((byte) 1);
            out.writeString(this.city);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoNumber != null) {
            out.writeByte((byte) 1);
            out.writeString(this.photoNumber);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoNumber_Self != null) {
            out.writeByte((byte) 1);
            out.writeString(this.photoNumber_Self);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.gameTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.gameTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.videoTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.videoTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.alarmTime_Workday != null) {
            out.writeByte((byte) 1);
            out.writeString(this.alarmTime_Workday);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callingDuration != null) {
            out.writeByte((byte) 1);
            out.writeString(this.callingDuration);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.callingTimes != null) {
            out.writeByte((byte) 1);
            out.writeString(this.callingTimes);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.musicNumber != null) {
            out.writeByte((byte) 1);
            out.writeString(this.musicNumber);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.musicNumber_Before2000 != null) {
            out.writeByte((byte) 1);
            out.writeString(this.musicNumber_Before2000);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.photoNumber_Turism != null) {
            out.writeByte((byte) 1);
            out.writeString(this.photoNumber_Turism);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.topMode != null) {
            out.writeByte((byte) 1);
            out.writeString(this.topMode);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top1Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top1Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top2Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top2Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top3Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top3Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top4Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top4Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top5Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top5Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top6Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top6Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top7Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top7Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top8Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top8Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top9Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top9Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.top10Tag != null) {
            out.writeByte((byte) 1);
            out.writeString(this.top10Tag);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.onTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.onTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveHomeTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.leaveHomeTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveWorkplaceTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.arriveWorkplaceTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.leaveWorkplaceTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.leaveWorkplaceTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.arriveHomeTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.arriveHomeTime);
        } else {
            out.writeByte((byte) 0);
        }
        if (this.offTime != null) {
            out.writeByte((byte) 1);
            out.writeString(this.offTime);
            return;
        }
        out.writeByte((byte) 0);
    }

    public AEntityHelper<ShortPeriodUserProfile> getHelper() {
        return ShortPeriodUserProfileHelper.getInstance();
    }

    public String getEntityName() {
        return "com.huawei.nb.model.profile.ShortPeriodUserProfile";
    }

    public String getDatabaseName() {
        return "dsServiceMetaData";
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("ShortPeriodUserProfile { id: ").append(this.id);
        sb.append(", sn: ").append(this.sn);
        sb.append(", genDate: ").append(this.genDate);
        sb.append(", deviceToken: ").append(this.deviceToken);
        sb.append(", hwId: ").append(this.hwId);
        sb.append(", imei: ").append(this.imei);
        sb.append(", sexuality: ").append(this.sexuality);
        sb.append(", age: ").append(this.age);
        sb.append(", contactNumber: ").append(this.contactNumber);
        sb.append(", accomLongtitude: ").append(this.accomLongtitude);
        sb.append(", accomLatitude: ").append(this.accomLatitude);
        sb.append(", accomCellId: ").append(this.accomCellId);
        sb.append(", accomCellLac: ").append(this.accomCellLac);
        sb.append(", workLongtitude: ").append(this.workLongtitude);
        sb.append(", workLatitude: ").append(this.workLatitude);
        sb.append(", workCellId: ").append(this.workCellId);
        sb.append(", workCellLac: ").append(this.workCellLac);
        sb.append(", poi1Longtitude: ").append(this.poi1Longtitude);
        sb.append(", poi1Latitude: ").append(this.poi1Latitude);
        sb.append(", poi1CellId: ").append(this.poi1CellId);
        sb.append(", poi1CellLac: ").append(this.poi1CellLac);
        sb.append(", poi2Longtitude: ").append(this.poi2Longtitude);
        sb.append(", poi2Latitude: ").append(this.poi2Latitude);
        sb.append(", poi2CellId: ").append(this.poi2CellId);
        sb.append(", poi2CellLac: ").append(this.poi2CellLac);
        sb.append(", poi3Longtitude: ").append(this.poi3Longtitude);
        sb.append(", poi3Latitude: ").append(this.poi3Latitude);
        sb.append(", poi3CellId: ").append(this.poi3CellId);
        sb.append(", poi3CellLac: ").append(this.poi3CellLac);
        sb.append(", poi4Longtitude: ").append(this.poi4Longtitude);
        sb.append(", poi4Latitude: ").append(this.poi4Latitude);
        sb.append(", poi4CellId: ").append(this.poi4CellId);
        sb.append(", poi4CellLac: ").append(this.poi4CellLac);
        sb.append(", poi5Longtitude: ").append(this.poi5Longtitude);
        sb.append(", poi5Latitude: ").append(this.poi5Latitude);
        sb.append(", poi5CellId: ").append(this.poi5CellId);
        sb.append(", poi5CellLac: ").append(this.poi5CellLac);
        sb.append(", roamingRadius: ").append(this.roamingRadius);
        sb.append(", city: ").append(this.city);
        sb.append(", photoNumber: ").append(this.photoNumber);
        sb.append(", photoNumber_Self: ").append(this.photoNumber_Self);
        sb.append(", gameTime: ").append(this.gameTime);
        sb.append(", videoTime: ").append(this.videoTime);
        sb.append(", alarmTime_Workday: ").append(this.alarmTime_Workday);
        sb.append(", callingDuration: ").append(this.callingDuration);
        sb.append(", callingTimes: ").append(this.callingTimes);
        sb.append(", musicNumber: ").append(this.musicNumber);
        sb.append(", musicNumber_Before2000: ").append(this.musicNumber_Before2000);
        sb.append(", photoNumber_Turism: ").append(this.photoNumber_Turism);
        sb.append(", topMode: ").append(this.topMode);
        sb.append(", top1Tag: ").append(this.top1Tag);
        sb.append(", top2Tag: ").append(this.top2Tag);
        sb.append(", top3Tag: ").append(this.top3Tag);
        sb.append(", top4Tag: ").append(this.top4Tag);
        sb.append(", top5Tag: ").append(this.top5Tag);
        sb.append(", top6Tag: ").append(this.top6Tag);
        sb.append(", top7Tag: ").append(this.top7Tag);
        sb.append(", top8Tag: ").append(this.top8Tag);
        sb.append(", top9Tag: ").append(this.top9Tag);
        sb.append(", top10Tag: ").append(this.top10Tag);
        sb.append(", onTime: ").append(this.onTime);
        sb.append(", leaveHomeTime: ").append(this.leaveHomeTime);
        sb.append(", arriveWorkplaceTime: ").append(this.arriveWorkplaceTime);
        sb.append(", leaveWorkplaceTime: ").append(this.leaveWorkplaceTime);
        sb.append(", arriveHomeTime: ").append(this.arriveHomeTime);
        sb.append(", offTime: ").append(this.offTime);
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
        return "0.0.11";
    }

    public int getDatabaseVersionCode() {
        return 11;
    }

    public String getEntityVersion() {
        return "0.0.1";
    }

    public int getEntityVersionCode() {
        return 1;
    }
}
