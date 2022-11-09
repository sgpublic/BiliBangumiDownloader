package io.github.sgpublic.bilidownload.core.exsp;

import io.github.sgpublic.exsp.annotations.ExSharedPreference;
import io.github.sgpublic.exsp.annotations.ExValue;
import lombok.Data;

@Data
@ExSharedPreference(name = "update")
public class UpdatePreference {
    @ExValue(defVal = "")
    private String updated;
}
