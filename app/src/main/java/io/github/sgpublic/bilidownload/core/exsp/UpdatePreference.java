package io.github.sgpublic.bilidownload.core.exsp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import io.github.sgpublic.exsp.annotations.ExSharedPreference;
import io.github.sgpublic.exsp.annotations.ExValue;
import lombok.Data;

@Data
@ExSharedPreference(name = "update")
public class UpdatePreference {
    @ExValue(defVal = "")
    private String updated;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
    public boolean needUpdate() {
        return sdf.format(new Date()).equals(getUpdated());
    }
    public void doUpdate() {
        setUpdated(sdf.format(new Date()));
    }
}
