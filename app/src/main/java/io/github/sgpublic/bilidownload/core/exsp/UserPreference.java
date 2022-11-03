package io.github.sgpublic.bilidownload.core.exsp;

import io.github.sgpublic.exsp.annotations.ExSharedPreference;
import io.github.sgpublic.exsp.annotations.ExValue;
import lombok.Data;

@Data
@ExSharedPreference(name = "user")
public class UserPreference {
    @ExValue(defVal = "-1")
    private int mid;

    @ExValue(defVal = "")
    private String name;

    @ExValue(defVal = "")
    private String sign;

    @ExValue(defVal = "")
    private String face;

    @ExValue(defVal = "0")
    private int sex;

    @ExValue(defVal = "0")
    private int level;

    @ExValue(defVal = "0")
    private int vipStatus;

    @ExValue(defVal = "0")
    private int vipType;

    @ExValue(defVal = "")
    private String vipLabel;
}
