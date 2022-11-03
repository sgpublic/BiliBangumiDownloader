package io.github.sgpublic.bilidownload.core.exsp;

import io.github.sgpublic.exsp.annotations.ExSharedPreference;
import io.github.sgpublic.exsp.annotations.ExValue;
import lombok.Data;

@Data
@ExSharedPreference(name = "token")
public class BangumiPreference {
    @ExValue(defVal = "false")
    private boolean playerAutoNext;

    @ExValue(defVal = "false")
    private boolean taskAutoStart;

    @ExValue(defVal = "80")
    private int quality;
}
