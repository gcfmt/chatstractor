/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gcfmt.chatstractor.data;

import java.util.Date;

/**
 *
 * @author WILLY
 */
public class Message {
    public int Id;
    public String KeyRemoteJid;
    public int KeyFromMe;
    public String KeyId;
    public int Status;
    public int NeedsPush;
    public String Data;
    public Date Timestamp;
    public String MediaUrl;
    public String MediaMimeType;
    public MediaWhatsappType MediaWaType;
    public int MediaSize;
    public String MediaName;
    public String MediaCaption;
    public String MediaHash;
    public String MediaLocalPath;
    public int MediaDuration;
    public double Origin;
    public String Latitude;
    public String Longitude;
    public String ThumbImage;
    public String RemoteResource;
    public Date ReceivedTimestamp;
    public Date SendTimestamp;
    public Date ReceiptServerTimestamp;
    public Date ReceiptDeviceTimestamp;
    public Date ReadDeviceTimestamp;
    public Date PlayedDeviceTimestamp;
    public byte[] RawData;
    public int RecipientCount;
    public String ParticipantHash;
    public WaContact Contato; 
        
        
    public enum MediaWhatsappType
    {
        MEDIA_WHATSAPP_TEXT(0),
        MEDIA_WHATSAPP_IMAGE(1),
        MEDIA_WHATSAPP_AUDIO(2),
        MEDIA_WHATSAPP_VIDEO(3),
        MEDIA_WHATSAPP_CONTACT(4),
        MEDIA_WHATSAPP_LOCATION(5);

        private int value;

        private MediaWhatsappType(int value)
        {
            this.value = value;
        }
    }
}
