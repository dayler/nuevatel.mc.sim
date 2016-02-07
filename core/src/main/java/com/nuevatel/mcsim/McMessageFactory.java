package com.nuevatel.mcsim;

import com.nuevatel.common.appconn.AppMessages;
import com.nuevatel.common.appconn.ByteArrayIe;
import com.nuevatel.common.appconn.ByteIe;
import com.nuevatel.common.appconn.IntIe;
import com.nuevatel.common.appconn.LongIe;
import com.nuevatel.common.appconn.Message;
import com.nuevatel.common.util.StringUtils;
import com.nuevatel.mc.appconn.AlertServiceCentreCall;
import com.nuevatel.mc.appconn.AlertServiceCentreRet;
import com.nuevatel.mc.appconn.ForwardSmICall;
import com.nuevatel.mc.appconn.ForwardSmIRet;
import com.nuevatel.mc.appconn.ForwardSmOCall;
import com.nuevatel.mc.appconn.ForwardSmORet;
import com.nuevatel.mc.appconn.ForwardSmORetAsyncCall;
import com.nuevatel.mc.appconn.McIe;
import com.nuevatel.mc.appconn.McMessage;
import com.nuevatel.mc.appconn.Name;
import com.nuevatel.mc.appconn.SrifsmCall;
import com.nuevatel.mc.appconn.SrifsmRet;
import com.nuevatel.mc.appconn.SrifsmRetAsyncCall;
import com.nuevatel.mcsim.domain.Action;

import java.lang.reflect.Field;
import java.util.StringJoiner;

import static com.nuevatel.mc.appconn.McMessage.*;
import static com.nuevatel.common.util.Util.*;

/**
 * Commands to available to execute.
 *
 * @author asalazar
 */
public enum McMessageFactory {
    
    // SRI
    /**
     * messageId;
     * ret;
     * imsi;
     * lmsi;
     * toGt;
     * serviceMsg;
     */
    srifsmRetAsyncCall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            Message msg = AppMessages.newAsyncCall(SRIFSM_RET_ASYNC_CALL, new ByteIe(AppMessages.RET_IE, fields.getRet()));
            ifNotNull(fields.getMessageId(), () -> msg.putIe(new LongIe(McIe.MESSAGE_ID_IE, fields.getMessageId())));
            ifNotNull(fields.getImsi(), ()->msg.putIe(new ByteArrayIe(McIe.IMSI_IE, fields.getImsi())));
            ifNotNull(fields.getLmsi(), ()->msg.putIe(new ByteArrayIe(McIe.LMSI_IE, fields.getLmsi())));
            ifNotNull(fields.getToGtName(), () -> msg.putIe(fields.toNameIe(McIe.GT_IE, fields.getToGtName())));
            ifNotNull(fields.getServiceMsg(), () -> msg.putIe(new IntIe(McIe.SERVICE_MSG_IE, fields.getServiceMsg())));
            McMessage sriAsyncCall = new SrifsmRetAsyncCall(msg);
            return sriAsyncCall;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                SrifsmRetAsyncCall sriMsg = new SrifsmRetAsyncCall(msg);
                return String.format("messageId=%s|ret=%s|imsi=%s|lmsi=%s|toGt=%s|serviceMsg=%s",
                                     sriMsg.getMessageId(), sriMsg.getRet(), sriMsg.getImsi(), sriMsg.getLmsi(), sriMsg.getToGt(), sriMsg.getServiceMsg());
            };
        }

        @Override
        public Action.ConcurrencyType getConcurrencyType() {
            return Action.ConcurrencyType.async;
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)-> {
                if (msg == null) {
                    return false;
                }
                SrifsmRetAsyncCall expectedMsg = castAs(SrifsmRetAsyncCall.class, mcMsg);
                SrifsmRetAsyncCall retMsg = new SrifsmRetAsyncCall(msg);
                // Ignore service message
                // messageId
                boolean result = (long)expectedMsg.getMessageId() == retMsg.getMessageId() &&
                                 // ret
                                 expectedMsg.getRet() == retMsg.getRet() &&
                                 // imsi
                                 StringUtils.equals(expectedMsg.getImsi(), retMsg.getImsi()) &&
                                 // lmsi
                                 StringUtils.equals(expectedMsg.getLmsi(), retMsg.getLmsi()) &&
                                 // toGt
                                 checkNames(expectedMsg.getToGt(), retMsg.getToGt());
                return result;
            };
        }
    },
    /**
     * private final Long messageId;
     * private final Name gt;
     * private final Name name;
     * private final byte smRpPri;
     */
    srifsmCall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            //long messageId, Name gt, Name name, byte smRpPri
            McMessage sriCall =
                    new SrifsmCall(fields.getMessageId(), fields.getToGtName(), fields.getName(), fields.getSmRpPri());
            return sriCall;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)-> {
                SrifsmCall sriCall = new SrifsmCall(msg);
                return String.format("messageId=%s|gt=%s|name=%s|smRpPri=%s",
                                     sriCall.getMessageId(), sriCall.getGt(), sriCall.getName(), sriCall.getSmRpPri());
            };
        }
    },
    /**
     * private final byte ret;
     */
    srifsmRet {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            return new SrifsmRet(fields.getRet());
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg) -> {
                SrifsmRet sriRet = new SrifsmRet(msg);
                return String.format("ret=%s", sriRet.getRet());
            };
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)-> {
                if (msg == null) {
                    return false;
                }

                SrifsmRet expectedMsg = castAs(SrifsmRet.class, mcMsg);
                SrifsmRet retMsg = new SrifsmRet(msg);
                return expectedMsg.getRet() == retMsg.getRet();
            };
        }
    },
    // FWMT
    /**
     * private final Long messageId;
     * private final byte ret;
     * private final Integer serviceMsg;
     */
    forwardSmORetAsyncCall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            McMessage fwmtAsync = new ForwardSmORetAsyncCall(fields.getMessageId(),
                                                             fields.getRet(),
                                                             fields.getServiceMsg());
            return fwmtAsync;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                ForwardSmORetAsyncCall fwAsyncCall = new ForwardSmORetAsyncCall(msg);
                return String.format("messageId=%s|ret=%s|serviceMsg=%s",
                                     fwAsyncCall.getMessageId(), fwAsyncCall.getRet(), fwAsyncCall.getServiceMsg());
            };
        }

        @Override
        public Action.ConcurrencyType getConcurrencyType() {
            return Action.ConcurrencyType.async;
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)->{
                if (msg == null) {
                    return false;
                }
                ForwardSmORetAsyncCall expected = castAs(ForwardSmORetAsyncCall.class, mcMsg);
                ForwardSmORetAsyncCall ret = new ForwardSmORetAsyncCall(msg);
                // messageId
                boolean result = (long)expected.getMessageId() == ret.getMessageId() &&
                                 // ret
                                 expected.getRet() == ret.getRet() &&
                                 // serviceMsg
                                 (int)expected.getServiceMsg() == ret.getServiceMsg();
                return result;
            };
        }
    },
    /**
     * private final byte ret;
     */
    forwardSmORet {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            return new ForwardSmORet(fields.getRet());
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                ForwardSmORet fwRet = new ForwardSmORet(msg);
                return String.format("ret=%s", fwRet.getRet());
            };
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)-> {
                if (msg == null) {
                    return false;
                }
                ForwardSmORet expected = castAs(ForwardSmORet.class, mcMsg);
                ForwardSmORet ret = new ForwardSmORet(msg);
                return expected.getRet() == ret.getRet();
            };
        }
    },
    /**
     * private final Long messageId;
     * private final String imsi;
     * private final String lmsi;
     * private final Name toGt;
     * private final byte[] tpdu;
     * private final Integer smppGwId;
     * private final Integer smppSessionId;
     * private final Name fromName;
     * private final Name toName;
     */
    forwardSmOCall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            // long messageId, String imsi, String lmsi, Name toGt, byte[] tpdu
            McMessage fwmtCall =
                    new ForwardSmOCall(fields.getMessageId(), fields.getImsi(), fields.getLmsi(), fields.getToGtName(), fields.getTpdu());
            return fwmtCall;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                ForwardSmOCall fwCall = new ForwardSmOCall(msg);
                return String.format("messageId=%s|imsi=%s|lmsi=%s|toGt=%s|tpdu=%s|smppGwId=%s|smppSessionId=%s|fromName=%s|toName=%s",
                                     fwCall.getMessageId(), fwCall.getImsi(), fwCall.getLmsi(), fwCall.getToGt(),
                                     tpduToString(fwCall.getTpdu()), fwCall.getSmppGwId(), fwCall.getSmppSessionId(),
                                     fwCall.getFromName(), fwCall.getToName());
            };
        }
    },
    // ASC
    /**
     * private final Name name;
     */
    alertServiceCentreCall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            AlertServiceCentreCall ascCall = new AlertServiceCentreCall(fields.getName());
            return ascCall;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                AlertServiceCentreCall ascCall = new AlertServiceCentreCall(msg);
                return String.format("name=%s", ascCall.getName());
            };
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)->{
                if (msg == null) {
                    return false;
                }
                AlertServiceCentreCall expected = castAs(AlertServiceCentreCall.class, mcMsg);
                AlertServiceCentreCall ret = new AlertServiceCentreCall(msg);
                // name
                boolean result = checkNames(expected.getName(), ret.getName());
                return result;
            };
        }
    },
    /**
     * private final byte ret;
     */
    alertServiceCentreRet {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            return new AlertServiceCentreRet(fields.getRet());
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                AlertServiceCentreRet ascRet = new AlertServiceCentreRet(msg);
                return String.format("ret=%s", ascRet.getRet());
            };
        }
    },
    // FWMO
    /**
     * private final Name fromGt;
     * private final Name fromName;
     * private final byte[] tpdu;
     * private final String smppServiceType;
     * private final ZonedDateTime smppScheduleDeliveryTime;
     * private final Byte smppReplaceIfPresentFlag;
     * private final Integer smppGwId;
     * private final Integer smppSessionId;
     * private final Integer appId;
     */
    forwardSmICall {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields  fields = Fields.fromMetadata(metadata);
            McMessage fwsmiCall = new ForwardSmICall(fields.getSmppServiceType(),
                                                     fields.getSmppScheduleDeliveryTime(),
                                                     fields.getSmppReplaceIfPresentFlag(),
                                                     fields.getSmppGwId(),
                                                     fields.getSmppSessionId(),
                                                     fields.getFromName(),
                                                     fields.getTpdu());
            return fwsmiCall;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            // TODO
            // String smppServiceType, ZonedDateTime smppScheduleDeliveryTime, Byte smppReplaceIfPresentFlag, Integer smppGwId,
            // Integer smppSessionId, Name fromName, byte[] tpdu
            return msg -> {
                ForwardSmICall fwsmiCall = new ForwardSmICall(msg);
                return String.format("smppServiceType=%s|smppScheduleDeliveryTime=%s|smppReplaceIfPresentFlag=%s|smppGwId=%s|smppSessionId=%s|fromName=%s|tpud=%s",
                                     fwsmiCall.getSmppServiceType(),
                                     fwsmiCall.getSmppScheduleDeliveryTime(),
                                     fwsmiCall.getSmppReplaceIfPresentFlag(),
                                     fwsmiCall.getSmppGwId(),
                                     fwsmiCall.getSmppSessionId(), 
                                     fwsmiCall.getFromName(),
                                     fwsmiCall.getTpdu());
            };
        }

        @Override
        public AssertMsgPredicate getPredicate() {
            return (mcMsg, msg)->{
                if (msg == null) {
                    return false;
                }
                ForwardSmICall expected = castAs(ForwardSmICall.class, mcMsg);
                ForwardSmICall ret = new ForwardSmICall(msg);
                // fromGt
                boolean result = checkNames(expected.getFromGt(), ret.getFromGt()) &&
                                 // toGt
                                 checkNames(expected.getFromName(), ret.getFromName()) &&
                                 // tpdu
                                 checkTpdu(expected.getTpdu(), ret.getTpdu());
                return result;
            };
        }
    },
    /**
     * private final byte ret;
     * private final Long messageId;
     */
    forwardSmIRet {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            Fields fields = Fields.fromMetadata(metadata);
            McMessage fwmoRet = new ForwardSmIRet(fields.getRet(), fields.getMessageId());
            return fwmoRet;
        }

        @Override
        public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
            return (msg)->{
                ForwardSmIRet fwmoRet = new ForwardSmIRet(msg);
                return String.format("ret=%s|messageId=%s", fwmoRet.getRet(), fwmoRet.getMessageId());
            };
        }
    },

    defaultMessage {
        @Override
        public McMessage fromMetadata(String[] metadata) {
            return null;
        }
    }
    ;

    private static String tpduToString(byte[] tpdu) {
        
        if (tpdu == null || tpdu.length == 0) {
            return null;
        }

        StringJoiner strJoiner = new StringJoiner(" ");
        for (byte b : tpdu) {
            strJoiner.add(Byte.toString(b));
        }
        return strJoiner.toString();
    }

    /**
     * Make McMessage from metadata.
     *
     * @param metadata
     * @return
     */
    public McMessage fromMetadata(String[] metadata) {
        return null;
    }

    public AssertMsgPredicate getPredicate() {
        return null;
    }

    public Action.ConcurrencyType getConcurrencyType() {
        return Action.ConcurrencyType.sync;
    }

    public McMsgToMetadataPredicate getMcMsgToMetadataPredicate() {
        return null;
    }

    /**
     *
     * @param expected
     * @param toCheck
     * @return <code>true</code> if expected and toCheck are equivalents (name and type).
     */
    public boolean checkNames(Name expected, Name toCheck) {
        if (expected == toCheck) {
            return true;
        }

        if (expected == null || toCheck == null) {
            return false;
        }

        return StringUtils.equals(expected.getName(), toCheck.getName()) && expected.getType() == toCheck.getType();
    }

    public boolean checkTpdu(byte[] expected, byte[] toCheck) {
        if (expected == toCheck) {
            return true;
        }
        if (expected.length != toCheck.length) {
            return false;
        }
        // Check element by element
        for (int i = 0; i < expected.length; i++) {
            if (expected[i] != toCheck[i]) {
                // if the element are not equals
                return false;
            }
        }
        // All elements are equals
        return true;
    }

    public static McMessageFactory fromName(String name) {
        try {
            return valueOf(name);
        } catch (Throwable ex) {
            return defaultMessage;
        }
    }

    /**
     * <code>ok</code> assertion was succeeded. <code>failed</code> assertion was failed.
     */
    public enum AssertResult
    {
        ok,
        failed,
        ;
    }
}
