package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.ByteArrayIe;
import com.nuevatel.common.appconn.ByteIe;
import com.nuevatel.common.appconn.CompositeIe;
import com.nuevatel.common.appconn.Ie;
import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.common.util.LongUtil;
import com.nuevatel.common.util.StringUtils;
import com.nuevatel.mc.appconn.Name;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/14/15.
 */
public class Fields {
    /* constants for appconn */
    private static final int NAME_NAME_IE = 0;
    private static final int NAME_TYPE_IE = 1;

    private static Logger logger = LogManager.getLogger(Fields.class);

    private Long messageId = null;
    private Byte ret = null;
    private String imsi = null;
    private String lmsi = null;
    private String toGtName = null;
    private Byte toGtNameType = null;
    private Integer serviceMsg = null;

    private String name;
    private Byte nameType;

    private Byte smRpPri;

    private byte[] tpdu;

    private String fromGtName;
    private Byte fromGtNameType;
    private String fromName;
    private Byte fromNameType;

    private String gtName;
    private Byte gtNameType;

    private String toName;
    private Byte toNameType;

    public Long getMessageId() {
        return messageId;
    }

    public Byte getRet() {
        return ret;
    }

    public String getImsi() {
        return imsi;
    }

    public String getLmsi() {
        return lmsi;
    }

    public String getToGtName() {
        return toGtName;
    }

    public Byte getToGtNameType() {
        return toGtNameType;
    }

    public Integer getServiceMsg() {
        return serviceMsg;
    }

    public String getName() {
        return name;
    }

    public Byte getNameType() {
        return nameType;
    }

    public Byte getSmRpPri() {
        return smRpPri;
    }

    public byte[] getTpdu() {
        return tpdu;
    }

    public String getFromGtName() {
        return fromGtName;
    }

    public Byte getFromGtNameType() {
        return fromGtNameType;
    }

    public String getFromName() {
        return fromName;
    }

    public Byte getFromNameType() {
        return fromNameType;
    }

    public String getGtName() {
        return gtName;
    }

    public Byte getGtNameType() {
        return gtNameType;
    }

    public String getToName() {
        return toName;
    }

    public Byte getToNameType() {
        return toNameType;
    }

    public Ie toNameIe(int code, String name, byte type) {
        CompositeIe ie = new CompositeIe(code);
        if (name != null) ie.putIe(new ByteArrayIe(NAME_NAME_IE, name));
        ie.putIe(new ByteIe(NAME_TYPE_IE, type));
        return ie;
    }

    private byte[] toTpduArray(String rawData) {
        if (StringUtils.isBlank(rawData)) {
            return null;
        }
        // split single space
        String[] arr = rawData.split(" ");
        byte[] tpdu = new byte[arr.length];
        // populate tpdu
        for (int i = 0; i < arr.length; i++) {
            tpdu[i] = (byte)Integer.parseInt(arr[i], 16);
        }

        return tpdu;
    }
    
    private Name makeName(String strMetadata) {
        if (StringUtils.isBlank(strMetadata) || !strMetadata.startsWith("[") || !strMetadata.endsWith("]")) {
            // no parse field
            return null;
        }
        strMetadata = strMetadata.substring(1, strMetadata.length()).substring(0, strMetadata.length() - 2);
        String[] metadata = strMetadata.split(",");
        if (metadata.length < 2) {
            // no parseable field
            return null;
        }
        Integer tmpType = IntegerUtil.tryParse(metadata[1].substring(" type=".length(), metadata[1].length()));
        return new Name(metadata[0].substring("name=".length(), metadata[0].length()), tmpType == null ? 0 : tmpType.byteValue());
    }
    
    public static Fields fromMetadata(String[] metadata) {
        Fields f = new Fields();
        for (String s : metadata) {
            // Split = to get field
            String[] kv = s.split("=");
            if (kv.length < 2)
            {
                continue;
            }

            try {
                // String kv1 = "NULL".equalsIgnoreCase(kv[1]) ? null : kv[1];
                switch (FieldName.fromName(kv[0])) {
                    case messageId:
                        f.messageId = LongUtil.tryParse(kv[1]);
                        break;
                    case ret:
                        f.ret = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case imsi:
                        f.imsi = kv[1];
                        break;
                    case lmsi:
                        f.lmsi = kv[1];
                        break;
                    case toGtName:
                        f.toGtName = kv[1];
                        break;
                    case toGtNameType:
                        f.toGtNameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case serviceMsg:
                        f.serviceMsg = IntegerUtil.tryParse(kv[1]);
                        break;
                    case name:
                        f.name = kv[1];
                        break;
                    case nameType:
                        f.nameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case smRpPri:
                        f.smRpPri = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case tpdu:
                        f.tpdu = f.toTpduArray(kv[1]);
                        break;
                    case fromGtName:
                        f.fromGtName = kv[1];
                        break;
                    case fromGtNameType:
                        f.fromGtNameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case fromName:
                        // TODO resolve from [name, type]
                        f.fromName = kv[1];
                        break;
                    case fromNameType:
                        f.fromNameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case gtName:
                        f.gtName = kv[1];
                        break;
                    case gtNameType:
                        f.gtNameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    case toName:
                        f.toName = kv[1];
                        break;
                    case toNameType:
                        f.toNameType = IntegerUtil.toByteValue(IntegerUtil.tryParse(kv[1]));
                        break;
                    default:
                        // NA
                }
            } catch (Throwable ex) {
                logger.warn("Unknown field[{}].", kv[0], ex);
            }
        }
        return f;
    }

    public enum FieldName {
        messageId,
        ret,
        imsi,
        lmsi,
        toGtName,
        toGtNameType,
        serviceMsg,
        name,
        nameType,
        smRpPri,
        tpdu, // hexadecimal
        fromGtName,
        fromGtNameType,
        fromName,
        fromNameType,
        gtName,
        gtNameType,
        toName,
        toNameType,
        unknown,
        ;

        public static FieldName fromName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ex) {
                return unknown;
            }
        }
    }
}
