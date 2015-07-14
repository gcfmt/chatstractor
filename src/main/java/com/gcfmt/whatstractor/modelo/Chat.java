/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gcfmt.whatstractor.modelo;

import java.util.Date;
import java.util.LinkedList;

public class Chat {
        public int Id;
        public String KeyRemoteJid;
        public int MessageTableId;
        public String Subject;
        public Date Creation;
        public int LastReadMessageTableId;
        public int LastReadReceiptSentMessageTableId;
        public int Archived;
        public int SortTimestamp;
        public int ModTag;

        public WaContact Contato;
        public LinkedList<Message> Mensagens;
        public LinkedList<GroupParticipant> ParticipantesGrupo;
}
