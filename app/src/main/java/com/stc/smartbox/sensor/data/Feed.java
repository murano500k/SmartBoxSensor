
package com.stc.smartbox.sensor.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Feed {

    @SerializedName("created_at")
    @Expose
    private Date createdAt;
    @SerializedName("entry_id")
    @Expose
    private int entryId;
    @SerializedName("field1")
    @Expose
    private Float field1;
    @SerializedName("field2")
    @Expose
    private Float field2;

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public Float getField1() {
        return field1;
    }

    public void setField1(Float field1) {
        this.field1 = field1;
    }

    public Float getField2() {
        return field2;
    }

    public void setField2(Float field2) {
        this.field2 = field2;
    }


}
