package com.lidroid.xutils.db.converter;

import android.database.Cursor;
import android.text.TextUtils;
import com.lidroid.xutils.db.sqlite.ColumnDbType;

/**
 * 数据库列的数据转换器（字节型）
 * 
 * <pre>
 * Author: wyouflf
 * Date: 13-11-4
 * Time: 下午10:51
 * </pre>
 * 
 * @author wyouflf
 */
public class ByteColumnConverter implements ColumnConverter<Byte> {
    @Override
    public Byte getFieldValue(final Cursor cursor, int index) {
        return cursor.isNull(index) ? null : (byte) cursor.getInt(index);
    }

    @Override
    public Byte getFieldValue(String fieldStringValue) {
        if (TextUtils.isEmpty(fieldStringValue)) return null;
        return Byte.valueOf(fieldStringValue);
    }

    @Override
    public Object fieldValue2ColumnValue(Byte fieldValue) {
        return fieldValue;
    }

    @Override
    public ColumnDbType getColumnDbType() {
        return ColumnDbType.INTEGER;
    }
}
