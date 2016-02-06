package com.nuevatel.mcsim.util;

import com.nuevatel.common.util.IntegerUtil;
import com.nuevatel.common.util.LongUtil;
import com.nuevatel.common.util.StrSubstitutor;
import com.nuevatel.common.util.StringUtils;
import com.nuevatel.mcsim.CsvColumn;

/**
 * Modify the strategy used to select the value to substitute
 *
 * @author Ariel D. Salazar H.
 *         Created by asalazar on 11/23/15.
 */
public class SimpleStrSubstitutor extends StrSubstitutor {

    public SimpleStrSubstitutor() {
        super();
    }

    public String getString(String name) {
        return super.getPropValue(name);
    }

    public Integer getInteger(String name) {
        return IntegerUtil.tryParse(super.getPropValue(name));
    }

    public Long getLong(String name) {
        return LongUtil.tryParse(super.getPropValue(name));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getPropValue(String name) {
        if (StringUtils.isBlank(name)) {
            return "";
        }

        // Check in CsvColumns for environment variables
        CsvColumn column = CsvColumn.fromName(name);
        if (CsvColumn.defaultValue == column) {
            // indicates the variable is not defined on CsvColumns
            return super.getPropValue(name);
        }
        // get odd value
        String oddVal = super.getPropValue(column.getKey());
        // get new value
        String value = column.getValue(oddVal);
        if (value == null || value.isEmpty()) {
            return oddVal;
        }

        if (!StringUtils.equals(oddVal, value)) {
            // Update value
            setProperty(column.getKey(), value);
        }

        return value;
    }
}
