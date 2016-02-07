package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.ByteArrayIe;
import com.nuevatel.common.appconn.ByteIe;
import com.nuevatel.common.appconn.CompositeIe;
import com.nuevatel.common.appconn.Ie;
import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.common.util.LongUtil;
import com.nuevatel.common.util.StringUtils;
import com.nuevatel.mc.appconn.Name;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

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

    /**
     * 2011-06-15 03:19:13
     */
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    
    private Long messageId = null;
    private Byte ret = null;
    private String imsi = null;
    private String lmsi = null;
    
    private Name toGtName = null;
    
    private Integer serviceMsg = null;

    private Name name;

    private Byte smRpPri;

    private byte[] tpdu;

    private Name fromGtName;
    
    private Name fromName = null;
    
    private Name gtName = null;
    
    private Name toName = null;

    private String smppServiceType;
    private ZonedDateTime smppScheduleDeliveryTime;
    
    /**
     * 0: Don't replace
     * 1: Replace
     */
    private Byte smppReplaceIfPresentFlag = (byte)0x00;
    private Integer smppGwId;
    private Integer smppSessionId;
    private Integer appId;

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
    
    public Name getToGtName() {
        return toGtName;
    }
    
    public Integer getServiceMsg() {
        return serviceMsg;
    }
    
    public Name getName() {
        return name;
    }
    
    public Byte getSmRpPri() {
        return smRpPri;
    }

    public byte[] getTpdu() {
        return tpdu;
    }
    
    public Name getFromGtName() {
        return fromGtName;
    }
    
    public Name getFromName() {
        return fromName;
    }
    
    public Name getGtName() {
        return gtName;
    }

    public Name getToName() {
        return toName;
    }

    public Ie toNameIe(int code, Name name) {
        CompositeIe ie = new CompositeIe(code);
        if (name != null) {
            ie.putIe(new ByteArrayIe(NAME_NAME_IE, name.getName()));
        }
        ie.putIe(new ByteIe(NAME_TYPE_IE, name.getType()));
        return ie;
    }

    public Integer getAppId() {
        return appId;
    }

    public Integer getSmppSessionId() {
        return smppSessionId;
    }

    public Integer getSmppGwId() {
        return smppGwId;
    }

    public Byte getSmppReplaceIfPresentFlag() {
        return smppReplaceIfPresentFlag;
    }

    public ZonedDateTime getSmppScheduleDeliveryTime() {
        return smppScheduleDeliveryTime;
    }

    public String getSmppServiceType() {
        return smppServiceType;
    }

    private static byte[] toTpduArray(String rawData) {
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
    
    private static Name makeName(String strMetadata) {
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
        // getName
        Integer type = IntegerUtil.tryParse(Arrays.stream(metadata).filter(e -> e.startsWith("type=")).findFirst().get());
        String name = Arrays.stream(metadata).map(e -> e.trim()).filter(e -> e.startsWith("name=")).findFirst().get();
        return new Name(name, type == null ? 0 : type.byteValue());
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
                FieldName.fromName(kv[0]).setProperty(kv[1], f);
            } catch (Throwable ex) {
                logger.warn("Unknown field[{}].", kv[0], ex);
            }
        }
        return f;
    }

    public enum FieldName {
        messageId {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.messageId = LongUtil.tryParse(metadata);
            }
        },
        ret {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.ret = IntegerUtil.toByteValue(IntegerUtil.tryParse(metadata));
            }
        },
        imsi {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.imsi = metadata;
            }
        },
        lmsi {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.lmsi = metadata;
            }
        },
        toGtName {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.toGtName = makeName(metadata);
            }
        },
        serviceMsg {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.serviceMsg = IntegerUtil.tryParse(metadata);
            }
        },
        name {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.name = makeName(metadata);
            }
        },
        smRpPri {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smRpPri = IntegerUtil.toByteValue(IntegerUtil.tryParse(metadata));
            }
        },
        tpdu {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.tpdu = toTpduArray(metadata);
            }
        }, // hexadecimal
        fromGtName {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.fromGtName = makeName(metadata);
            }
        },
        fromName {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.fromName = makeName(metadata);
            }
        },
        gtName {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.gtName = makeName(metadata);
            }
        },
        toName {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.toName = makeName(metadata);
            }
        },
        
        smppServiceType {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smppServiceType = metadata;
            }
            
        }, // String
        smppScheduleDeliveryTime {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smppScheduleDeliveryTime = ZonedDateTime.parse(metadata, formatter);
            }
        }, // ZonedDateTime
        smppReplaceIfPresentFlag {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smppReplaceIfPresentFlag = IntegerUtil.tryParse(metadata, 0x00).byteValue();
            }
        }, // boolean
        smppGwId {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smppGwId = IntegerUtil.tryParse(metadata);
            }
        }, // int
        smppSessionId {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.smppSessionId = IntegerUtil.tryParse(metadata);
            }
        }, // int
        appId {
            @Override
            public void setProperty(String metadata, Fields fields) {
                fields.appId = IntegerUtil.tryParse(metadata);
            }
        }, // int
        
        unknown,
        ;
        
        public void setProperty(String metadata, Fields fields) {
            // Default. No op
        }
        
        public static FieldName fromName(String name) {
            try {
                return valueOf(name);
            } catch (IllegalArgumentException ex) {
                return unknown;
            }
        }
    }
}
