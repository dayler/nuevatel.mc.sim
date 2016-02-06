package com.nuevatel.mcsim;

import com.nuevatel.common.util.IntegerUtil;

/**
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/23/15.
 */
public enum CsvColumn {
    /**
     * Current id
     */
    id,
    /**
     * Next id, based on <code>id</code>
     */
    nextId("id") {
        @Override
        public String getValue(String oddVal) {
            Integer intOddVal = IntegerUtil.tryParse(oddVal);
            if (intOddVal == null) {
                return IntegerUtil.toString(0);
            }
            // Next val
            return IntegerUtil.toString(intOddVal + 1);
        }
    },
    /**
     * Current <code>seqId</code>
     */
    seqId,
    /**
     * Next <code>seqId</code>
     */
    nextSeqId("seqId") {
        @Override
        public String getValue(String oddVal) {
            Integer intOddVal = IntegerUtil.tryParse(oddVal);
            if (intOddVal == null) {
                return IntegerUtil.toString(0);
            }
            // Next val
            return IntegerUtil.toString(intOddVal + 1);
        }
    },
    /**
     * Set <code>seqId</code> to 0, to re-start numeration
     */
    resetSeqId("seqId") {
        @Override
        public String getValue(String oddVal) {
            return IntegerUtil.toString(0);
        }
    },
    /**
     * Current dialog id
     */
    dialogId,
    /**
     * Next dialog Id
     */
    nextDialogId("dialogId") {
        @Override
        public String getValue(String oddVal) {
            Integer intOddVal = IntegerUtil.tryParse(oddVal);
            if (intOddVal == null) {
                // Dialog begin at 1000
                return IntegerUtil.toString(1000);
            }
            // Next val
            return IntegerUtil.toString(intOddVal + 1);
        }
    },

    defaultValue {
        @Override
        public String getValue(String oddVal) {
            return null;
        }
    },
    ;

    private String key;

    private CsvColumn() {
        this.key = name();
    }

    private CsvColumn(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getValue(String oddVal) {
        return oddVal;
    }

    public static CsvColumn fromName(String name) {
        try {
            return valueOf(name);
        } catch (Throwable ex) {
            return defaultValue;
        }
    }

}
