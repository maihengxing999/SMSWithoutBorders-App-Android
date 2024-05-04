package com.example.sw0b_001.Models.EncryptedContent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class EncryptedContent {

    String encryptedContent;

    @ColumnInfo(name="platform_id")
    long platformId;

    @PrimaryKey(autoGenerate = true)
    long id;

    String platformName;

    String type;

    long date;


    public String getGatewayClientMSISDN() {
        return gatewayClientMSISDN;
    }
    public void setGatewayClientMSISDN(String gatewayClientMSISDN) {
        this.gatewayClientMSISDN = gatewayClientMSISDN;
    }

    @ColumnInfo(name="gateway_client_MSISDN")
    String gatewayClientMSISDN;

    public String getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(String encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public long getPlatformId() {
        return platformId;
    }

    public void setPlatformId(long platformId) {
        this.platformId = platformId;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public EncryptedContent() {
    }


    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj instanceof EncryptedContent) {
            EncryptedContent encryptedContent = (EncryptedContent) obj;
            return encryptedContent.id == this.id &&
                    encryptedContent.platformId == this.platformId &&
                    encryptedContent.platformName.equals(this.platformName) &&
                    encryptedContent.date == this.date &&
                    encryptedContent.type.equals(this.type) &&
                    encryptedContent.encryptedContent.equals(this.encryptedContent);
        }
        return false;
    }

    public static final DiffUtil.ItemCallback<EncryptedContent> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<EncryptedContent>() {
                @Override
                public boolean areItemsTheSame(@NonNull EncryptedContent oldItem, @NonNull EncryptedContent newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull EncryptedContent oldItem, @NonNull EncryptedContent newItem) {
                    return oldItem.equals(newItem);
                }
            };
}
