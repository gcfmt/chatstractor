package com.gcfmt.chatstractor;

import com.gcfmt.chatstractor.data.Chat;
import com.gcfmt.chatstractor.data.GroupParticipant;
import com.gcfmt.chatstractor.data.Message;
import com.gcfmt.chatstractor.data.WaContact;
import static java.util.Arrays.stream;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import org.sqlite.SQLiteConnection;

public class Banco {
    static SQLiteConnection ConexaoChatStorage;
    static boolean PossuiBancoContatos = false;
    static SQLiteConnection ConexaoContatos;

    public static List<Chat> Chats;
    public static List<GroupParticipant> GroupParticipants;
    public static List<Message> Messages;
    public static List<WaContact> WaContacts;

    public static TiposDispositivo TipoDispositivo;

    /// <summary>
    /// Inicializa a conexão e efetua o carregamento do banco sqlite Android de endereço definido por parâmetro
    /// </summary>
    /// <returns>Status do processamento</returns>
    /// <param name="enderecoMsgStore">String contendo o endereço do arquivo msgstore.db a ser carregado</param>
    /// <param name="enderecoWaDb">String contendo o endereço do arquivo wa.db a ser carregado</param>
    public static String CarregarBancoAndroid(String enderecoMsgStore, String enderecoWaDb)
    {
        TipoDispositivo = TiposDispositivo.ANDROID;

        String resultado = "";
        try
        {
            ConexaoChatStorage = new SQLiteConnection("Data Source=" + enderecoMsgStore + ";Version=3;");

            PossuiBancoContatos = !enderecoWaDb.isEmpty();
            if (PossuiBancoContatos) ConexaoContatos = new SQLiteConnection("Data Source=" + enderecoWaDb + ";Version=3;");

            ConexaoChatStorage.Open();

            Chats = new LinkedList<Chat>();
            var chatListReader = new SQLiteCommand("select * from chat_list;", ConexaoChatStorage).ExecuteReader();
            while (chatListReader.Read())
            {
                Chat chat = new Chat();
                chat.Id = chatListReader.GetInt32(0);
                chat.KeyRemoteJid = chatListReader.IsDBNull(1) ? null : chatListReader.GetString(1);
                chat.MessageTableId = chatListReader.IsDBNull(2) ? 0 : chatListReader.GetInt32(2);
                chat.Subject = chatListReader.IsDBNull(3) ? null : chatListReader.GetString(3);
                chat.Creation = chatListReader.IsDBNull(4) ? Date.MinValue : chatListReader.GetInt64(4).TimeStampParaDateTime();
                chat.LastReadMessageTableId = chatListReader.IsDBNull(5) ? -1 : chatListReader.GetInt32(5);
                chat.LastReadReceiptSentMessageTableId = chatListReader.IsDBNull(6) ? -1 : chatListReader.GetInt32(6);
                chat.Archived = chatListReader.IsDBNull(7) ? -1 : chatListReader.GetInt32(7);
                chat.SortTimestamp = chatListReader.IsDBNull(8) ? -1 : chatListReader.GetInt32(8);
                chat.ModTag = chatListReader.IsDBNull(9) ? -1 : chatListReader.GetInt32(9);

                Chats.add(chat);
            }
            
            GroupParticipants = new LinkedList<GroupParticipant>();
            try
            {
                var groupParticipantReader = new SQLiteCommand("select * from group_participants;", ConexaoChatStorage).ExecuteReader();
                while (groupParticipantReader.Read())
                {
                    GroupParticipant groupParticipant = new GroupParticipant();
                    groupParticipant.Id = groupParticipantReader.GetInt32(0);
                    groupParticipant.Gjid = groupParticipantReader.GetString(1);
                    groupParticipant.Jid = groupParticipantReader.GetString(2);
                    groupParticipant.Admin = groupParticipantReader.IsDBNull(3) ? -1 : groupParticipantReader.GetInt32(3);
                    groupParticipant.Pending = groupParticipantReader.IsDBNull(4) ? -1 : groupParticipantReader.GetInt32(4);

                    GroupParticipants.Add(groupParticipant);

                }

            }
            catch (Exception ex)
            {
                if (!ex.getMessage().contains("no such table")) throw ex;
            }

            for(Chat chat : Chats)
            {
                if(!chat.KeyRemoteJid.contains("-")) continue;
                chat.ParticipantesGrupo = new LinkedList<GroupParticipant>();
                chat.ParticipantesGrupo.AddRange(GroupParticipants.Where(p => p.Gjid == chat.KeyRemoteJid));
            }
            
            
            Messages = new LinkedList<Message>();
            var messagesReader = new SQLiteCommand("select * from messages;", ConexaoChatStorage).ExecuteReader();
            while (messagesReader.Read())
            {
                Message message = new Message();
                message.Id = messagesReader.GetInt32(messagesReader.GetOrdinal("_id"));
                if (message.Id == 1) continue;
                message.KeyRemoteJid = messagesReader.GetString(messagesReader.GetOrdinal("key_remote_jid"));
                message.KeyFromMe = messagesReader.IsDBNull(messagesReader.GetOrdinal("key_from_me")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("key_from_me"));
                message.KeyId = messagesReader.GetString(messagesReader.GetOrdinal("key_id"));
                message.Status = messagesReader.IsDBNull(messagesReader.GetOrdinal("status")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("status"));
                message.NeedsPush = messagesReader.IsDBNull(messagesReader.GetOrdinal("needs_push")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("needs_push"));
                message.Data = messagesReader.IsDBNull(messagesReader.GetOrdinal("data")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("data"));
                message.Timestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("timestamp")).TimeStampParaDateTime();
                message.MediaUrl = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_url")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_url"));
                message.MediaMimeType = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_mime_type")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_mime_type"));
                message.MediaWaType = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_wa_type")) ? Message.MediaWhatsappType.MEDIA_WHATSAPP_TEXT :
                    (Message.MediaWhatsappType)Enum.Parse(typeof(Message.MediaWhatsappType), messagesReader.GetString(messagesReader.GetOrdinal("media_wa_type")));
                message.MediaSize = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_size")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("media_size"));
                message.MediaName = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_name")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_name"));
                message.MediaHash = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_hash")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_hash"));
                message.MediaCaption = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_caption")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_caption"));

                message.MediaDuration = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_duration")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("media_duration"));

                message.Origin = messagesReader.IsDBNull(messagesReader.GetOrdinal("origin")) ? -1 : messagesReader.GetDouble(messagesReader.GetOrdinal("origin"));
                message.Latitude = messagesReader.IsDBNull(messagesReader.GetOrdinal("latitude")) ? "" : messagesReader.GetValue(messagesReader.GetOrdinal("latitude")).ToString();
                message.Longitude = messagesReader.IsDBNull(messagesReader.GetOrdinal("longitude")) ? "" : messagesReader.GetValue(messagesReader.GetOrdinal("longitude")).ToString();
                message.ThumbImage = messagesReader.IsDBNull(messagesReader.GetOrdinal("thumb_image")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("thumb_image"));


                message.RemoteResource = messagesReader.IsDBNull(messagesReader.GetOrdinal("remote_resource")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("remote_resource"));
                message.ReceivedTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("received_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("received_timestamp")).TimeStampParaDateTime();
                message.SendTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("send_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("send_timestamp")).TimeStampParaDateTime();
                message.ReceiptServerTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("receipt_server_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("receipt_server_timestamp")).TimeStampParaDateTime();
                //   var val = messagesReader.GetValue(24);
                //  var v2 = val.GetType();

                message.ReceiptDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("receipt_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("receipt_device_timestamp")).TimeStampParaDateTime();
                message.ReadDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("read_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("read_device_timestamp")).TimeStampParaDateTime();
                message.PlayedDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("played_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("played_device_timestamp")).TimeStampParaDateTime();
                message.RecipientCount = messagesReader.IsDBNull(messagesReader.GetOrdinal("recipient_count")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("recipient_count"));
                message.ParticipantHash = messagesReader.IsDBNull(messagesReader.GetOrdinal("participant_hash")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("participant_hash"));


                if (!messagesReader.IsDBNull(messagesReader.GetOrdinal("raw_data")))
                {
                    const int CHUNK_SIZE = 2 * 1024;
                    byte[] buffer = new byte[CHUNK_SIZE];
                    long bytesRead;
                    long fieldOffset = 0;
                    using (MemoryStream stream = new MemoryStream())
                    {
                        while ((bytesRead = messagesReader.GetBytes(messagesReader.GetOrdinal("raw_data"), fieldOffset, buffer, 0, buffer.Length)) > 0)
                        {
                            stream.Write(buffer, 0, (int)bytesRead);
                            fieldOffset += bytesRead;
                        }
                        message.RawData = stream.ToArray();
                    }
                }

                Messages.Add(message);
            }

            for (Chat chat : Chats)
            {
                chat.Mensagens = new LinkedList<Message>();
                chat.Mensagens.addAll(Messages.Where(p => p.KeyRemoteJid == chat.KeyRemoteJid).OrderBy(p => p.Timestamp));
            }


           
            
            ConexaoChatStorage.Close();

            WaContacts = new LinkedList<WaContact>();
            if (PossuiBancoContatos)
            {
                ConexaoContatos.Open();

                var waContactsReader = new SQLiteCommand("select * from wa_contacts;", ConexaoContatos).ExecuteReader();
                while (waContactsReader.Read())
                {
                    WaContact waContact = new WaContact();
                    waContact.Id = waContactsReader.GetInt32(waContactsReader.GetOrdinal("_id"));
                    waContact.Jid = waContactsReader.GetString(waContactsReader.GetOrdinal("jid"));
                    waContact.IsWhatsappUser = waContactsReader.GetBoolean(waContactsReader.GetOrdinal("is_whatsapp_user"));
                    waContact.Status = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("status")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("status"));
                    waContact.StatusTimestamp = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("status_timestamp")) ? DateTime.MinValue : waContactsReader.GetInt64(waContactsReader.GetOrdinal("status_timestamp")).TimeStampParaDateTime();
                    waContact.Number = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("number")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("number"));
                    waContact.RawContactId = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("raw_contact_id")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("raw_contact_id"));
                        waContact.DisplayName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("display_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("display_name"));
                    waContact.PhoneType = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("phone_type")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("phone_type"));
                    waContact.PhoneLabel = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("phone_label")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("phone_label"));
                    waContact.UnseenMsgCount = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("unseen_msg_count")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("unseen_msg_count"));
                    waContact.PhotoTs = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("photo_ts")) ? -1 : waContactsReader.GetInt64(waContactsReader.GetOrdinal("photo_ts"));
                    waContact.ThumbTs = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("thumb_ts")) ? -1 : waContactsReader.GetInt64(waContactsReader.GetOrdinal("thumb_ts"));
                    waContact.PhotoIdTimestamp = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("photo_id_timestamp")) ? DateTime.MinValue : waContactsReader.GetInt64(waContactsReader.GetOrdinal("photo_id_timestamp")).TimeStampParaDateTime();
                    waContact.GivenName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("given_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("given_name"));
                    waContact.FamilyName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("family_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("family_name"));
                    waContact.WaName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("wa_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("wa_name"));
                    waContact.SortName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("sort_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("sort_name"));
                    waContact.Callability = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("callability")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("callability"));

                    boolean isGrupo = waContact.Jid.contains("-");
                    if (!isGrupo)
                    {
                        String nomeExibido = waContact.DisplayName;
                        String nomeWa = waContact.WaName;
                        String telefone = waContact.Jid;

                        waContact.NomeContato += !nomeExibido.isEmpty() && !nomeWa.isEmpty() ?
                                            nomeExibido + ", " + nomeWa : nomeExibido + nomeWa;
                        if (waContact.NomeContato.isEmpty()) waContact.NomeContato += waContact.Jid;
                    }
                    else waContact.NomeContato = waContact.DisplayName;

                    WaContacts.add(waContact);
                }

                ConexaoContatos.Close();
            }

            for(Chat chat : Chats) chat.Contato = WaContacts.FirstOrDefault(p => p.Jid == chat.KeyRemoteJid) ?? new WaContact { Jid = chat.KeyRemoteJid, NomeContato = chat.KeyRemoteJid.contains("-") ? chat.Subject : chat.KeyRemoteJid };

            for(GroupParticipan groupParticipant : GroupParticipants) groupParticipant.Contato = WaContacts.FirstOrDefault(p => p.Jid == groupParticipant.Jid) ?? new WaContact { Jid = groupParticipant.Jid, NomeContato = groupParticipant.Jid };

            for(Message message : Messages) message.Contato = WaContacts.FirstOrDefault(p => message.KeyRemoteJid.contains("-") ? p.Jid == message.RemoteResource : p.Jid == message.KeyRemoteJid ) ?? new WaContact { Jid = message.RemoteResource, NomeContato = message.RemoteResource };

            resultado = "SUCESSO: Bancos de Dados Carregados.";

        }
        catch (ArrayStoreException ex)
        {
            resultado = "ERRO: " + ex.getMessage();
        }

        return resultado;
    }

    public static String CarregarBancoIPhone(String enderecoChatStorage, String enderecoContacts)
    {
        TipoDispositivo = TiposDispositivo.IOS;

        String resultado = "";
        try
        {
            ConexaoChatStorage = new SQLiteConnection("Data Source=" + enderecoChatStorage + ";Version=3;");

            PossuiBancoContatos = !String.IsNullOrWhiteSpace(enderecoContacts);
            if (PossuiBancoContatos) ConexaoContatos = new SQLiteConnection("Data Source=" + enderecoContacts + ";Version=3;");

            ConexaoChatStorage.Open();

            Chats = new LinkedList<Chat>();
            var chatSessionReader = new SQLiteCommand("select * from ZWACHATSESSION;", ConexaoChatStorage).ExecuteReader();
            while (chatSessionReader.Read())
            {
                Chat chat = new Chat();
                chat.Id = chatSessionReader.GetInt32(chatSessionReader.GetOrdinal("Z_PK"));
                chat.KeyRemoteJid = chatSessionReader.IsDBNull(1) ? null : chatSessionReader.GetString(chatSessionReader.GetOrdinal("ZCONTACTJID"));
                chat.Subject = chatSessionReader.IsDBNull(3) ? null : chatSessionReader.GetString(chatSessionReader.GetOrdinal("ZPARTNERNAME"));
                chat.LastReadMessageTableId = chatSessionReader.GetInt32(chatSessionReader.GetOrdinal("ZLASTMESSAGE"));
              
                Chats.Add(chat);
            }
            

            GroupParticipants = new LinkedList<GroupParticipant>();
         
            var groupParticipantReader = new SQLiteCommand("select * from (select * from ZWAGROUPMEMBER LEFT JOIN ZWAGROUPINFO on "+
                "ZWAGROUPMEMBER.ZCHATSESSION = ZWAGROUPINFO.Z_PK) LEFT JOIN ZWACHATSESSION on ZCHATSESSION = ZWACHATSESSION.Z_PK;", ConexaoChatStorage).ExecuteReader();
            while (groupParticipantReader.Read())
            {
                var groupParticipant = new GroupParticipant();
                groupParticipant.Id = groupParticipantReader.GetInt32(groupParticipantReader.GetOrdinal("Z_PK"));
                groupParticipant.Gjid = groupParticipantReader.GetString(groupParticipantReader.GetOrdinal("ZCONTACTJID"));
                groupParticipant.Jid = groupParticipantReader.GetString(groupParticipantReader.GetOrdinal("ZMEMBERJID"));
                groupParticipant.Admin = groupParticipantReader.GetInt32(groupParticipantReader.GetOrdinal("ZISADMIN"));
                groupParticipant.ContactName = groupParticipantReader.GetString(groupParticipantReader.GetOrdinal("ZCONTACTNAME"));
                //groupParticipant.Pending = groupParticipantReader.IsDBNull(4) ? -1 : groupParticipantReader.GetInt32(4);

                GroupParticipants.Add(groupParticipant);
            }


            for(Chat chat : Chats)
            {
                if(!chat.KeyRemoteJid.contains("-")) continue;
                        
                chat.ParticipantesGrupo = new LinkedList<GroupParticipant>();
                chat.ParticipantesGrupo.AddRange(GroupParticipants.Where(p => p.Gjid == chat.KeyRemoteJid));
            }
            
            
            Messages = new LinkedList<Message>();
            var messagesReader = new SQLiteCommand("select * from (select * from ZWAMESSAGE left join ZWAMEDIAITEM on ZWAMESSAGE.Z_PK = ZWAMEDIAITEM.ZMESSAGE) left join ZWAGROUPMEMBER on ZGROUPMEMBER = ZWAGROUPMEMBER.Z_PK;", ConexaoChatStorage).ExecuteReader();
            while (messagesReader.Read())
            {
                var message = new Message();
                message.Id = messagesReader.GetInt32(messagesReader.GetOrdinal("Z_PK"));
                //if (message.Id == 1) continue;
                message.KeyFromMe = messagesReader.GetInt32(messagesReader.GetOrdinal("ZISFROMME"));
                message.KeyRemoteJid = message.KeyFromMe == 1 ?
                    messagesReader.GetString(messagesReader.GetOrdinal("ZTOJID")) :
                    messagesReader.GetString(messagesReader.GetOrdinal("ZFROMJID")) ;

                message.Status = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZMESSAGESTATUS")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("ZMESSAGESTATUS"));
                message.Data = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZTEXT")) ? "" :  messagesReader.GetString(messagesReader.GetOrdinal("ZTEXT"));
                message.MediaUrl = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZMEDIAURL")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("ZMEDIAURL"));
                message.MediaLocalPath = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZMEDIALOCALPATH")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("ZMEDIALOCALPATH"));


                message.Latitude = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZLATITUDE")) ? "" : messagesReader.GetValue(messagesReader.GetOrdinal("ZLATITUDE")).ToString();
                message.Longitude = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZLONGITUDE")) ? "" : messagesReader.GetValue(messagesReader.GetOrdinal("ZLONGITUDE")).ToString();
                message.ThumbImage = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZXMPPTHUMBPATH")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("ZXMPPTHUMBPATH"));

                if(message.KeyFromMe == 0 && message.KeyRemoteJid.contains("."))
                    message.RemoteResource = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZMEMBERJID")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("ZMEMBERJID"));

                message.MediaWaType = messagesReader.IsDBNull(messagesReader.GetOrdinal("ZMESSAGETYPE")) ? Message.MediaWhatsappType.MEDIA_WHATSAPP_TEXT :
                 (Message.MediaWhatsappType)Enum.Parse(typeof(Message.MediaWhatsappType), messagesReader.GetInt32(messagesReader.GetOrdinal("ZMESSAGETYPE")).ToString());

                if (message.MediaWaType == Message.MediaWhatsappType.MEDIA_WHATSAPP_VIDEO)
                    message.MediaWaType = Message.MediaWhatsappType.MEDIA_WHATSAPP_AUDIO;
                else if (message.MediaWaType == Message.MediaWhatsappType.MEDIA_WHATSAPP_AUDIO)
                    message.MediaWaType = Message.MediaWhatsappType.MEDIA_WHATSAPP_VIDEO;   

                //message.MediaDuration = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_duration")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("media_duration"));
                //message.Origin = messagesReader.IsDBNull(messagesReader.GetOrdinal("origin")) ? -1 : messagesReader.GetDouble(messagesReader.GetOrdinal("origin"));
                //message.NeedsPush = messagesReader.IsDBNull(messagesReader.GetOrdinal("needs_push")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("needs_push"));
                //message.KeyId = messagesReader.GetString(messagesReader.GetOrdinal("key_id"));
                //message.MediaMimeType = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_mime_type")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_mime_type"));
                //message.MediaWaType = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_wa_type")) ? MediaWhatsappType.MEDIA_WHATSAPP_TEXT :
                //    (MediaWhatsappType)Enum.Parse(typeof(MediaWhatsappType), messagesReader.GetString(messagesReader.GetOrdinal("media_wa_type")));
                //message.MediaSize = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_size")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("media_size"));
                //message.MediaName = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_name")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_name"));
                //message.MediaHash = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_hash")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_hash"));
                //message.MediaCaption = messagesReader.IsDBNull(messagesReader.GetOrdinal("media_caption")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("media_caption"));

                //message.ReceivedTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("received_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("received_timestamp")).TimeStampParaDateTime();
                //message.SendTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("send_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("send_timestamp")).TimeStampParaDateTime();
                //message.ReceiptServerTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("receipt_server_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("receipt_server_timestamp")).TimeStampParaDateTime();
                //   var val = messagesReader.GetValue(24);
                //  var v2 = val.GetType();

                //message.ReceiptDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("receipt_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("receipt_device_timestamp")).TimeStampParaDateTime();
                //message.ReadDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("read_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("read_device_timestamp")).TimeStampParaDateTime();
                //message.PlayedDeviceTimestamp = messagesReader.IsDBNull(messagesReader.GetOrdinal("played_device_timestamp")) ? DateTime.MinValue : messagesReader.GetInt64(messagesReader.GetOrdinal("played_device_timestamp")).TimeStampParaDateTime();
                //message.RecipientCount = messagesReader.IsDBNull(messagesReader.GetOrdinal("recipient_count")) ? -1 : messagesReader.GetInt32(messagesReader.GetOrdinal("recipient_count"));
                //message.ParticipantHash = messagesReader.IsDBNull(messagesReader.GetOrdinal("participant_hash")) ? "" : messagesReader.GetString(messagesReader.GetOrdinal("participant_hash"));


                Date dataBanco = messagesReader.GetDouble(messagesReader.GetOrdinal("ZMESSAGEDATE"));
                long d2 = (Convert.ToInt64(dataBanco));

                long d3 = (978307200 + d2) * 1000;

                message.Timestamp = (d3).TimeStampParaDateTime();

                Messages.Add(message);
            }

            for(Chat chat : Chats)
            {
                chat.Mensagens = new LinkedList<Message>();
                chat.Mensagens.AddRange(Messages.Where(p => p.KeyRemoteJid == chat.KeyRemoteJid).OrderBy(p => p.Timestamp));
            }

            
            ConexaoChatStorage.Close();

            WaContacts = new LinkedList<WaContact>();
            if (PossuiBancoContatos)
            {
                ConexaoContatos.Open();

                var waContactsReader = new SQLiteCommand("select * from (select ZWACONTACT.Z_PK as ID, ZWACONTACT.ZFIRSTNAME,  ZWACONTACT.ZFULLNAME, "+
                    "ZWAPHONE.Z_PK, ZWAPHONE.ZPHONE as NUMERO from ZWACONTACT LEFT JOIN ZWAPHONE on ZWACONTACT.Z_PK = ZWAPHONE.ZCONTACT) Z1 "+
                    "LEFT JOIN ZWASTATUS ON  Z1.Z_PK = ZWASTATUS.ZPHONE;", ConexaoContatos).ExecuteReader();
                while (waContactsReader.Read())
                {
                    var waContact = new WaContact();
                    waContact.Id = waContactsReader.GetInt32(waContactsReader.GetOrdinal("ID"));
                    waContact.Jid = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("ZWHATSAPPID")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("ZWHATSAPPID"));
                    //waContact.IsWhatsappUser = waContactsReader.GetBoolean(waContactsReader.GetOrdinal("is_whatsapp_user"));
                    waContact.Status = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("ZTEXT")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("ZTEXT"));
                    //waContact.StatusTimestamp = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("status_timestamp")) ? DateTime.MinValue : waContactsReader.GetInt64(waContactsReader.GetOrdinal("status_timestamp")).TimeStampParaDateTime();
                    waContact.Number = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("NUMERO")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("NUMERO"));
                    //waContact.RawContactId = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("raw_contact_id")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("raw_contact_id"));
                    waContact.DisplayName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("ZFIRSTNAME")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("ZFIRSTNAME"));
                    //waContact.PhoneType = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("phone_type")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("phone_type"));
                    //waContact.PhoneLabel = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("phone_label")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("phone_label"));
                    //waContact.UnseenMsgCount = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("unseen_msg_count")) ? -1 : waContactsReader.GetInt32(waContactsReader.GetOrdinal("unseen_msg_count"));
                    //waContact.PhotoTs = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("photo_ts")) ? -1 : waContactsReader.GetInt64(waContactsReader.GetOrdinal("photo_ts"));
                    //waContact.ThumbTs = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("thumb_ts")) ? -1 : waContactsReader.GetInt64(waContactsReader.GetOrdinal("thumb_ts"));
                    //waContact.PhotoIdTimestamp = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("photo_id_timestamp")) ? DateTime.MinValue : waContactsReader.GetInt64(waContactsReader.GetOrdinal("photo_id_timestamp")).TimeStampParaDateTime();
                    //waContact.GivenName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("given_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("given_name"));
                    //waContact.FamilyName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("family_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("family_name"));
                    waContact.WaName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("ZFULLNAME")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("ZFULLNAME"));
                    //waContact.SortName = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("sort_name")) ? "" : waContactsReader.GetString(waContactsReader.GetOrdinal("sort_name"));
                    waContact.Callability = waContactsReader.IsDBNull(waContactsReader.GetOrdinal("ZCALLABILITY")) ? "" : waContactsReader.GetInt32(waContactsReader.GetOrdinal("ZCALLABILITY")).ToString();

                    String nomeExibido = waContact.DisplayName;
                    String nomeWa = waContact.WaName;

                    waContact.NomeContato += !nomeExibido.isEmpty() && !nomeWa.isEmpty() ? nomeExibido + ", " + nomeWa : nomeExibido + nomeWa;
                    if (String.IsNullOrWhiteSpace(waContact.NomeContato)) waContact.NomeContato += waContact.Jid;

                    WaContacts.Add(waContact);
                }

                ConexaoContatos.Close();
            }

            for(Chat chat : Chats)
            {
                chat.Contato = WaContacts.FirstOrDefault(p => p.Jid == chat.KeyRemoteJid.Split('@')[0]) ??
                    new WaContact { Jid = chat.KeyRemoteJid, NomeContato = chat.KeyRemoteJid.contains("-") ? chat.Subject : chat.KeyRemoteJid };
            }

            for (GroupParticipant groupParticipant : GroupParticipants)
            {
                if (Banco.TipoDispositivo == TiposDispositivo.ANDROID)
                    groupParticipant.Contato = new WaContact { Jid = groupParticipant.Jid, NomeContato = groupParticipant.Jid };
                else if (Banco.TipoDispositivo == TiposDispositivo.IOS)
                    groupParticipant.Contato = new WaContact { Jid = groupParticipant.Jid, NomeContato = groupParticipant.ContactName };
                WaContacts.Add(groupParticipant.Contato);
            }

            for(Message message : Messages)
            {
                if (message.KeyRemoteJid.contains("-"))
                {
                    message.Contato = WaContacts.LastOrDefault(p => p.Jid == message.RemoteResource) ??
                        new WaContact { Jid = message.KeyRemoteJid, NomeContato = message.KeyRemoteJid+"_)" };
                }
                else
                    message.Contato = WaContacts.FirstOrDefault(p => p.Jid == message.KeyRemoteJid.Split('@')[0]) ??
                        new WaContact { Jid = message.KeyRemoteJid, NomeContato = message.KeyRemoteJid };
            }

            resultado = "SUCESSO: Bancos de Dados Carregados.";

        }
        catch (ArgumentNullException ex)
        {
            resultado = "ERRO: " + ex.Message;
        }

        return resultado;
    }
        
    public enum TiposDispositivo
    {
        ANDROID,
        IOS
    } 
}
