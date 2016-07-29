package io.github.cutedb.runner.utils;

/**
 * Created by barmi83 on 29/07/16.
 */
import flexjson.JSONException;
import flexjson.ObjectBinder;
import flexjson.ObjectFactory;
import flexjson.transformer.AbstractTransformer;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTransformer extends AbstractTransformer implements ObjectFactory {

    private static final String msDateFormat = "/[Dd][Aa][Tt][Ee]\\((\\-?[0-9]+)([\\+\\-]{1}[0-9]{4})*\\)/";
    private static final Pattern msDateFormatPattern = Pattern.compile(msDateFormat);

    @Override
    public void transform(Object arg0) {

        if (arg0 == null) {
            getContext().write(null);
            return;
        }

        DateTime dt = new DateTime(arg0);

        DateTimeFormatter formater = new DateTimeFormatterBuilder().appendLiteral("/Date(").appendLiteral(String.format("%s", dt.getMillis())).appendPattern("Z").appendLiteral(")/").toFormatter();

        getContext().writeQuoted(formater.print(dt));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Object instantiate(ObjectBinder context, Object value, Type targetType, Class targetClass) {

        Matcher matcher = msDateFormatPattern.matcher(value.toString());

        try {
            matcher.find();

            String timestamp = matcher.group(1);
            String timeZone = matcher.group(2);

            DateTime dt;

            if (timeZone != null)
                dt = new DateTime(Long.parseLong(timestamp), DateTimeZone.forID(timeZone));
            else
                dt = new DateTime(Long.parseLong(timestamp), DateTimeZone.UTC);

            return dt.toDate();
        } catch (Exception ex) {
            throw new JSONException(String.format("Unable to parse '%s' as a valid date time. Expected format is '%s'", value.toString(), msDateFormat), ex);
        }
    }

}