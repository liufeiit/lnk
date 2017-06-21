package io.lnk.protocol.xml.xstream;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;

import com.thoughtworks.xstream.converters.ConversionException;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

public class DateConverter implements Converter {

    private static final DateFormat DEFAULT_DATEFORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    @Override
    public boolean canConvert(@SuppressWarnings("rawtypes") Class arg0) {
        return Date.class == arg0;
    }

    @Override
    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext arg2) {
        if (obj != null) {
            writer.setValue(DEFAULT_DATEFORMAT.format(obj));
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext arg1) {
        GregorianCalendar calendar = new GregorianCalendar();
        try {
            if (StringUtils.isBlank(reader.getValue())) {
                return null;
            }
            if (reader.getValue().length() > 14) {
                throw new ConversionException("unmarshal Error.");
            }
            calendar.setTime(DEFAULT_DATEFORMAT.parse(reader.getValue()));
        } catch (ParseException e) {
            throw new ConversionException(e.getMessage(), e);
        }
        return calendar.getTime();

    }

}
